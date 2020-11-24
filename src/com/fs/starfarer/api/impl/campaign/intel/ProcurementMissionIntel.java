package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.util.Misc.Token;


public class ProcurementMissionIntel extends BaseMissionIntel {
	public static final String PERSON_CHECKOUT_REASON = "MPM_mission_contact";
	//public static final Float POSTING_RANGE_LY = 0f;
	
	public static String BUTTON_COMMODITY_INFO = "Show commodity info";
	
	public static Logger log = Global.getLogger(ProcurementMissionIntel.class);
	
	protected MarketAPI market;
	protected PersonAPI contact;
	protected float quantity;
	protected float pricePerUnit;
	
	protected boolean contactWillInitiateComms = false;
	protected CommodityOnMarketAPI commodity;

	protected float baseReward;
	
	public ProcurementMissionIntel() {
		String commodityId = pickCommodity();
		if (commodityId == null) {
			endMission();
			endImmediately();
			return;
		}
		
		quantity = getQuantity(commodityId);
		float illegalMult = 0.2f;
		float min = 10f;
		market = pickMarket(commodityId, quantity, illegalMult, min);
		if (market == null) {
			endMission();
			endImmediately();
			return;
		}
		
		commodity = market.getCommodityData(commodityId);

//		commodityId = Commodities.ORE;
//		market = Global.getSector().getEconomy().getMarket("sphinx");
		quantity = (int) getQuantityAdjustedForMarket(commodityId, quantity, illegalMult, min, market);
		
		if (quantity <= 0) {
			endMission();
			endImmediately();
			return;
		}
		
		WeightedRandomPicker<Float> durationPicker = new WeightedRandomPicker<Float>();
		durationPicker.add(20f);
		durationPicker.add(30f);
		durationPicker.add(40f);
		durationPicker.add(60f);
		
		setDuration(durationPicker.pick());
		
		baseReward = market.getDemandPrice(commodityId, quantity, false);
		
		float minReward = 7500 + (float) Math.random() * 5000f;
		if (baseReward < minReward) {
			quantity = quantity * (minReward / baseReward);
			if (quantity > 5000) quantity = 5000;
			quantity = (int)(((int)quantity / 10) * 10);
			if (quantity <= 0) {
				endMission();
				endImmediately();
				return;
			}
			baseReward = market.getDemandPrice(commodityId, quantity, false);
		}

		float basePerUnit = market.getCommodityData(commodityId).getCommodity().getBasePrice();
		if (baseReward < basePerUnit * quantity) baseReward = basePerUnit * quantity;
		
		float maxQuantity = 10000f;
		float maxDuration = 60f;

		float durationBonus = Math.min(baseReward/quantity * (0.5f * (2f - Math.min(1f, duration / maxDuration))), 300);
		float quantityBonus = Math.min(baseReward/quantity * (0.5f * (2f - Math.min(1f, quantity / maxQuantity))), 300);
		if (durationBonus < 10) durationBonus = 10;
		if (quantityBonus < 10) quantityBonus = 10;
		
		//baseReward += durationBonus * quantity;
		//baseReward += quantityBonus * quantity;

		contact = pickContact(market, market.getCommodityData(commodityId));
		Global.getSector().getImportantPeople().checkOutPerson(contact, PERSON_CHECKOUT_REASON);
		
		boolean illegal = contact.getFaction().isHostileTo(market.getFaction());
		
		if (illegal) {
			baseReward *= 3f;
		} else {
			baseReward *= 2f;
		}
		
		
		pricePerUnit = (int) (baseReward / quantity);
		
//		if (commodityId.equals("drugs")) {
//			System.out.println("sdflhwefwe");
//		}
		if (pricePerUnit < 10) pricePerUnit = 10;
		
		
		log.info("Created ProcurementMissionIntel: " + commodityId + " to " + market.getName());

		initRandomCancel();
		setPostingLocation(market.getPrimaryEntity());
		
		Global.getSector().getIntelManager().queueIntel(this);
	}
	
	public void missionAccepted() {
		market.getCommDirectory().addPerson(contact);

		contact.getMemoryWithoutUpdate().set("$mpm_isPlayerContact", true, duration);
		contact.getMemoryWithoutUpdate().set("$mpm_eventRef", this, duration);
		contact.getMemoryWithoutUpdate().set("$mpm_commodityName", commodity.getCommodity().getName().toLowerCase(), duration);
		contact.getMemoryWithoutUpdate().set("$mpm_quantity", Misc.getWithDGS((int)quantity), duration);
		Misc.setFlagWithReason(contact.getMemoryWithoutUpdate(), 
							MemFlags.MEMORY_KEY_REQUIRES_DISCRETION, "mpm_" + commodity.getId(),
							true, duration);
		
		Misc.setFlagWithReason(contact.getMemoryWithoutUpdate(),
							  MemFlags.MEMORY_KEY_MISSION_IMPORTANT,
							  "mpm", true, duration);
		
		contactWillInitiateComms = (float) Math.random() > 0.5f;

		boolean illegal = contact.getFaction().isHostileTo(market.getFaction());
		if (illegal) {
			contactWillInitiateComms = false;
		}
		
		if (contactWillInitiateComms) {
			contact.incrWantsToContactReasons();
		}

	}
	
	
	protected PersonAPI pickContact(MarketAPI market, CommodityOnMarketAPI com) {
		Global.getSettings().profilerBegin(this.getClass().getSimpleName() + ".pickContact()");
		PersonAPI contact = null;
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		
		String comId = com.getId();
		
		if (market.getFaction().isPlayerFaction()) {
			contact = getCriminal(market, PERSON_CHECKOUT_REASON, Factions.PIRATES).getPerson();
		}
		
		if (contact == null && com.getCommodity().hasTag(Commodities.TAG_MILITARY)) {
			if ((float) Math.random() > 0.1f) {
				contact = ip.getPerson(market.getFaction(), market,
							PERSON_CHECKOUT_REASON, Ranks.GROUND_LIEUTENANT, 
							Ranks.POST_SUPPLY_OFFICER,
							Ranks.POST_BASE_COMMANDER,
							Ranks.POST_OUTPOST_COMMANDER,
							Ranks.POST_PORTMASTER).getPerson();
			} else {
				contact = getCriminal(market, PERSON_CHECKOUT_REASON, Factions.PIRATES).getPerson();
			}
		}
		
		if (contact == null && com.getCommodity().hasTag(Commodities.TAG_MEDICAL)) {
			if ((float) Math.random() > 0.25f) {
				contact = ip.getPerson((float) Math.random() > 0.5f ? Factions.INDEPENDENT : market.getFactionId(),
						market,
						PERSON_CHECKOUT_REASON, Ranks.CITIZEN, 
						Ranks.POST_MEDICAL_SUPPLIER).getPerson();
			} else {
				contact = getCriminal(market, PERSON_CHECKOUT_REASON, Factions.PIRATES).getPerson();
			}
		}
		
		if (contact == null && com.getCommodity().hasTag(Commodities.TAG_LUXURY)) {
			if ((float) Math.random() > 0.1f && !market.isIllegal(comId)) {
				contact = getLegitTrader(market, PERSON_CHECKOUT_REASON, market.getFactionId()).getPerson();
			} else {
				contact = getCriminal(market, PERSON_CHECKOUT_REASON, Factions.PIRATES).getPerson();
			}
		}
		
		if (contact == null) {
			if ((float) Math.random() > 0.05f && !market.isIllegal(comId)) {
				contact = getLegitTrader(market, PERSON_CHECKOUT_REASON, market.getFactionId()).getPerson();
			} else {
				contact = getCriminal(market, PERSON_CHECKOUT_REASON, Factions.PIRATES).getPerson();
			}
		}
		
		if (contact == null) {
			// shouldn't happen, but just in case
			contact = market.getFaction().createRandomPerson();
			contact.setPostId(Ranks.POST_CITIZEN);
			contact.setRankId(null);
			market.addPerson(contact);
			ip.addPerson(contact);
			ip.getData(contact).getLocation().setMarket(market);
		}
		Global.getSettings().profilerEnd();
		return contact;
	}
	
	
	public static PersonDataAPI getLegitTrader(MarketAPI market, String checkoutReason, String factionId) {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		return ip.getPerson(factionId,
				market,
				checkoutReason, Ranks.CITIZEN, 
				Ranks.POST_MERCHANT,
				Ranks.POST_COMMODITIES_AGENT,
				Ranks.POST_INVESTOR,
				Ranks.POST_TRADER);
	}
	
	public static PersonDataAPI getCriminal(MarketAPI market, String checkoutReason, String factionId) {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		return ip.getPerson(factionId,
				market,
				checkoutReason, Ranks.CITIZEN, 
				Ranks.POST_GANGSTER,
				Ranks.POST_SMUGGLER,
				Ranks.POST_FENCE);
	}
	
	
	
	
	protected String pickCommodity() {
		Global.getSettings().profilerBegin(this.getClass().getSimpleName() + ".pickCommodity()");
		EconomyAPI economy = Global.getSector().getEconomy();
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
		
		for (String curr : economy.getAllCommodityIds()) {
			CommoditySpecAPI spec = economy.getCommoditySpec(curr);
			if (spec.isMeta()) continue;
			if (spec.hasTag(Commodities.TAG_CREW)) continue;
			if (spec.hasTag(Commodities.TAG_MARINES)) continue;
			if (spec.hasTag(Commodities.TAG_NON_ECONOMIC)) continue;
//			if (spec.getId().equals(Commodities.SUPPLIES)) continue;
//			if (spec.getId().equals(Commodities.FUEL)) continue;
			
			//float weight = spec.getBasePrice();
			float weight = 1f;
			picker.add(curr, weight);
		}
		Global.getSettings().profilerEnd();
		return picker.pick();
	}
	
	protected int getQuantity(String commodityId) {
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
		float cargoCapacity = 30;
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player != null) {
			if (Commodities.FUEL.equals(commodityId)) {
				cargoCapacity = Math.max(cargoCapacity, cargoCapacity = player.getCargo().getMaxFuel());
			} else {
				cargoCapacity = Math.max(cargoCapacity, player.getCargo().getMaxCapacity());
			}
		}
		
		// using fuel as a mid-price commodity
		CommoditySpecAPI fuel = Global.getSettings().getCommoditySpec(Commodities.FUEL);
		float targetValue = cargoCapacity * Math.max(5f, fuel.getBasePrice());
		
		float units = targetValue / Math.max(5f, spec.getBasePrice());
		
		units *= 0.5f + (float) Math.random();
		
		//return (int) Misc.getRounded(units);
		return (int) units / 10 * 10;
	}
	
	protected float getQuantityAdjustedForMarket(String commodityId, float quantity, float illegalMult, float min, MarketAPI market) {
		if (market.getSize() <= 4) {
			quantity = Math.min(quantity, 200);
		} else if (market.getSize() == 5) {
			quantity = Math.min(quantity, 500);
		} else if (market.getSize() == 6) {
			quantity = Math.min(quantity, 1000);
		} else if (market.getSize() == 7) {
			quantity = Math.min(quantity, 2000);
		} else if (market.getSize() >= 8) {
			quantity = Math.min(quantity, 10000);
		}
		CommodityOnMarketAPI com = market.getCommodityData(commodityId);
		if (com.getUtilityOnMarket() > 0) {
			quantity /= com.getUtilityOnMarket();
		}
		boolean illegal = market.isIllegal(commodityId);
		if (illegal) {
			quantity *= illegalMult;
			if (quantity < min) quantity = min;
		}
		return (int)quantity;
	}
	
	protected MarketAPI pickMarket(String commodityId, float quantity, float illegalMult, float min) {
//		if (true) {
//			return Global.getSector().getEconomy().getMarket("jangala");
//		}
		Global.getSettings().profilerBegin(this.getClass().getSimpleName() + ".pickMarket()");
		EconomyAPI economy = Global.getSector().getEconomy();
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
		
		for (MarketAPI market : economy.getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.isPlayerOwned()) continue;
			
			//CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			
			boolean illegal = market.isIllegal(commodityId);
			float test = getQuantityAdjustedForMarket(commodityId, quantity, illegalMult, min, market);
			if (illegal) {
				test *= illegalMult;
				if (test < min) test = min;
			}
			//if (com.getAverageStockpileAfterDemand() >= minQty) continue;
			// don't filter on demand - open up more possibilities; may be needed for non-market-condition reasons
			//if (com.getDemand().getDemandValue() < minQty) continue;
			
			if (doNearbyMarketsHave(market, commodityId, test * 0.5f)) continue;
			
			float weight = market.getSize();
			
			if (market.getFaction().isPlayerFaction()) {
				weight *= 0.1f;
			}
			
			picker.add(market, weight);
		}
		Global.getSettings().profilerEnd();
		return picker.pick();
	}
	
	protected boolean doNearbyMarketsHave(MarketAPI from, String commodityId, float minQty) {
		if (from.getContainingLocation() == null) return false;
		//if (from.getContainingLocation() == null || from.getContainingLocation().isHyperspace()) return false;
		
		//Global.getSettings().profilerBegin(this.getClass().getSimpleName() + ".doNearbyMarketsHave()");
		for (MarketAPI curr : Misc.getMarketsInLocation(from.getContainingLocation())) {
			//if (curr == from) continue;
			if (curr.getPrimaryEntity() != null && from.getPrimaryEntity() != null) {
				float dist = Misc.getDistance(curr.getPrimaryEntity().getLocation(), from.getPrimaryEntity().getLocation());
				if (dist > 10000) continue;
			}
			
			CommodityOnMarketAPI com = curr.getCommodityData(commodityId);
			//if (com.getAvailable() >= 2 && com.getAvailable() >= com.getMaxDemand() - 1) return true;
			
			int a = com.getAvailable();
			if (a <= 0) continue;
			
			float limit = BaseIndustry.getSizeMult(a);
			limit *= com.getCommodity().getEconUnit();
			
			if (limit * 0.5f >= minQty) return true;
			
//			if (com.getAboveDemandStockpile() >= minQty) {
//				//Global.getSettings().profilerEnd();
//				return true;
//			}
			
			//if (com.getSupplyValue() >= minQty) return true;
			for (SubmarketAPI submarket : curr.getSubmarketsCopy()) {
				//if (!submarket.getPlugin().isParticipatesInEconomy()) continue;
				CargoAPI cargo = submarket.getCargoNullOk();
				if (cargo == null) continue;
				if (cargo.getQuantity(CargoItemType.RESOURCES, commodityId) >= minQty * 0.5f) return true;
			}
		}
		//Global.getSettings().profilerEnd();
		return false;
	}	
	
	
	


	@Override
	public void advanceMission(float amount) {
		if (!market.isInEconomy()) {
			setMissionResult(new MissionResult(0, null, null));
			setMissionState(MissionState.FAILED);
			endMission();
		}
	}
	
	
	@Override
	public boolean callEvent(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		String commodityId = commodity.getId();
		if (action.equals("performDelivery")) {
			cargo.removeItems(CargoItemType.RESOURCES, commodityId, quantity);
			int reward = (int) baseReward;
			cargo.getCredits().add(reward);
			
			AddRemoveCommodity.addCommodityLossText(commodityId, (int) quantity, dialog.getTextPanel());
			AddRemoveCommodity.addCreditsGainText(reward, dialog.getTextPanel());
			
			applyTradeValueImpact(reward);
			
			float repAmount = 0.01f * baseReward / 100000f;
			if (repAmount < 0.01f) repAmount = 0.01f;
			if (repAmount > 0.05f) repAmount = 0.05f;
			
			MissionCompletionRep completionRep = new MissionCompletionRep(repAmount, RepLevel.WELCOMING, -repAmount, RepLevel.INHOSPITABLE);
			
			ReputationAdjustmentResult repF = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.MISSION_SUCCESS, completionRep,
										  null, dialog.getTextPanel(), true, true), 
										  contact.getFaction().getId());
			ReputationAdjustmentResult repC = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.MISSION_SUCCESS, completionRep,
										  null, dialog.getTextPanel(), true, true), 
										  contact);
			setMissionResult(new MissionResult(reward, repF, repC));
			setMissionState(MissionState.COMPLETED);
			
			//sendUpdateIfPlayerHasIntel(missionResult, false);
		} else if (action.equals("hasEnough")) {
			return cargo.getCommodityQuantity(commodityId) >= quantity;
		} else if (action.equals("endEvent")) {
			endMission();
//		} else if (action.equals("handOver")) {
//			CargoAPI pirateCargo = dialog.getInteractionTarget().getCargo();
//			cargo.removeItems(CargoItemType.RESOURCES, commodityId, quantity);
//			pirateCargo.addItems(CargoItemType.RESOURCES, commodityId, quantity);
//			
//			CampaignFleetAPI pirateFleet = (CampaignFleetAPI) dialog.getInteractionTarget();
//			pirateFleet.getAI().removeFirstAssignment();
//			pirateFleet.getAI().addAssignmentAtStart(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, market.getPrimaryEntity(), 1000f, null);
//			
//			pirateFleet.getMemoryWithoutUpdate().unset("$mpm_isSpawnedByMPM");
//			pirateFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, false, 10);
		} else if (action.equals("hasBonus")) {
			return hasBonus();
		}
		
		return true;
	}
	
	protected boolean hasBonus() {
		return false;
		//return (bonusDuration - elapsedDays) > 0;
	}
	
	protected void applyTradeValueImpact(float totalReward) {
		boolean illegal = contact.getFaction().isHostileTo(market.getFaction());
		
		SubmarketAPI submarket = null;
		for (SubmarketAPI curr : market.getSubmarketsCopy()) {
			if (!curr.getPlugin().isParticipatesInEconomy()) continue;
			if (contact.getFaction() == curr.getFaction()) {
				submarket = curr;
				break;
			}
		}
		
		if (submarket == null) {
			for (SubmarketAPI curr : market.getSubmarketsCopy()) {
				if (!curr.getPlugin().isParticipatesInEconomy()) continue;
				
				if (illegal && curr.getPlugin().isBlackMarket()) {
					submarket = curr;
					break;
				}
				if (!illegal && curr.getPlugin().isOpenMarket()) {
					submarket = curr;
					break;
				}
			}
		}
		
		if (submarket == null) return;
		
		PlayerTradeDataForSubmarket tradeData = SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket);
		CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, commodity.getId(), null);
		stack.setSize(quantity);
		tradeData.addToTrackedPlayerSold(stack, totalReward);
	}
	

	public void endMission() {
		if (contact != null) {
			contact.getMemoryWithoutUpdate().unset("$mpm_isPlayerContact");
			contact.getMemoryWithoutUpdate().unset("$mpm_eventRef");
			contact.getMemoryWithoutUpdate().unset("$mpm_commodityName");
			contact.getMemoryWithoutUpdate().unset("$mpm_quantity");
			Misc.setFlagWithReason(contact.getMemoryWithoutUpdate(), 
								   MemFlags.MEMORY_KEY_REQUIRES_DISCRETION, "mpm_" + commodity.getId(),
								   false, 0f);
			Misc.setFlagWithReason(contact.getMemoryWithoutUpdate(),
								   MemFlags.MEMORY_KEY_MISSION_IMPORTANT,
								   "mpm", false, 0f);
			
			if (contactWillInitiateComms) {
				contact.decrWantsToContactReasons();
			}
			
			Global.getSector().getImportantPeople().returnPerson(contact, PERSON_CHECKOUT_REASON);
			if (!Global.getSector().getImportantPeople().isCheckedOutForAnything(contact)) {
				market.getCommDirectory().removePerson(contact);
				//mission.getMarket().removePerson(mission.getContact());
			}
		}
		
		endAfterDelay();
	}
	
	public boolean runWhilePaused() {
		return false;
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		if (isUpdate) {
			// 3 possible updates: de-posted/expired, failed, completed
			if (isFailed() || isCancelled()) {
				return;
			} else if (isCompleted()) {
				if (missionResult.payment > 0) {
					info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
				}
				CoreReputationPlugin.addAdjustmentMessage(missionResult.rep1.delta, contact.getFaction(), null, 
														  null, null, info, tc, isUpdate, 0f);
				CoreReputationPlugin.addAdjustmentMessage(missionResult.rep2.delta, null, contact, 
														  null, null, info, tc, isUpdate, 0f);
			}
		} else {
			// either in small description, or in tooltip/intel list
			if (missionResult != null) {
				if (missionResult.payment > 0) {
					info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(missionResult.payment));
					initPad = 0f;
				}
				
				if (missionResult.rep1 != null) {
					CoreReputationPlugin.addAdjustmentMessage(missionResult.rep1.delta, contact.getFaction(), null, 
													  null, null, info, tc, isUpdate, initPad);
					initPad = 0f;
				}
				if (missionResult.rep2!= null) {
					CoreReputationPlugin.addAdjustmentMessage(missionResult.rep2.delta, null, contact, 
													  null, null, info, tc, isUpdate, initPad);
					initPad = 0f;
				}
			} else {
				if (mode != ListInfoMode.IN_DESC) {
					FactionAPI faction = contact.getFaction();
					info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
												 faction.getBaseUIColor(),
												 faction.getDisplayName());
					initPad = 0f;
				}
				
				LabelAPI label = info.addPara("%s units required at " + market.getName(), 
							 initPad, tc, h, "" + (int) quantity);
				label.setHighlight("" + (int)quantity, market.getName());
				label.setHighlightColors(h, market.getFaction().getBaseUIColor());
				info.addPara("%s reward", 0f, tc, h, Misc.getDGSCredits(baseReward));
				addDays(info, "to complete", duration - elapsedDays, tc, 0f);
			}
		}
		
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	public String getSortString() {
		return "Procurement";
	}
	
	public String getName() {
		if (isAccepted() || isPosted()) {
			return "Procurement - " + commodity.getCommodity().getName();
		}
		
		return "Procurement Contract" + getPostfixForState();
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return contact.getFaction();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		FactionAPI faction = contact.getFaction();
		boolean illegal = contact.getFaction().isHostileTo(market.getFaction());
		
		//info.addImage(commodity.getCommodity().getIconName(), width, 80, opad);
		
		info.addImages(width, 80, opad, opad * 2f,
					   commodity.getCommodity().getIconName(),
					   contact.getPortraitSprite());
					   //faction.getCrest());

		String prefix = faction.getPersonNamePrefix();
		if (Factions.PIRATES.equals(faction.getId())) {
			prefix += "-affiliated";
		}
		
		String desc = contact.getPost();
		if (desc == null) desc = contact.getRank();
		if (desc == null) desc = "supplier";
		desc = desc.toLowerCase();
		
		
		info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " " + prefix + " " + desc + " at " + 
				market.getName() + " " +  
				"has posted a procurement contract for a quantity of " + 
				commodity.getCommodity().getLowerCaseName() + ".",
				opad, tc, faction.getBaseUIColor(), prefix);
		
		
		
		if (isPosted() || isAccepted()) {
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
			
			info.addPara("Contact " + contact.getNameString() + 
						 " at " + market.getName() + " to complete the delivery.", opad);
			
			if (illegal) {
				info.addPara(contact.getNameString() + 
						" is affiliated with the local underworld and will require clandestine delivery, " +
						"which may attract the interest of local authorities.", opad);
			} else {
				if (market.getFaction().isHostileTo(Factions.PLAYER)) {
					info.addPara(contact.getNameString() + " is " +
							"operating with the knowledge of the local authorities. " +
							"However, " + faction.getDisplayNameWithArticle() + " is hostile towards you, and you'll need " +
							"to sneak into port without attracting notice in order to complete the delivery.", opad);
				} else {
					info.addPara(contact.getNameString() + " is " +
							"operating with the knowledge of the local authorities and the delivery may be made openly.", opad);
				}
			}
			
			addGenericMissionState(info);
			
			addAcceptOrAbandonButton(info, width);
			
			ButtonAPI button = info.addButton("View commodity info", BUTTON_COMMODITY_INFO, 
					  	getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
					  (int)(width), 20f, opad * 3f);
			if (!Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay()) {
				button.setEnabled(false);
				info.addPara("Seeing remote price data requires being within range of a functional comm relay.", g, opad);
			}
			
			
		} else {
			if (isFailed() && !market.isInEconomy()) {
				info.addPara("You have failed this contract because " + market.getName() + 
						     " no longer exists as a functional polity.", opad);	
			} else {
				addGenericMissionState(info);
			}
			
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}

	}
	
	public String getIcon() {
		return commodity.getCommodity().getIconName();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_TRADE);
		tags.add(contact.getFaction().getId());
		return tags;
	}
	

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return market.getPrimaryEntity();
	}
	


	@Override
	protected String getMissionTypeNoun() {
		return "contract";
	}
	

	@Override
	protected MissionResult createAbandonedResult(boolean withPenalty) {
		if (withPenalty) {
			float repAmount = 0.01f * baseReward / 100000f;
			if (repAmount < 0.01f) repAmount = 0.01f;
			if (repAmount > 0.05f) repAmount = 0.05f;
			
			MissionCompletionRep completionRep = new MissionCompletionRep(repAmount, RepLevel.WELCOMING, -repAmount, RepLevel.INHOSPITABLE);
			
			ReputationAdjustmentResult repF = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.MISSION_FAILURE, completionRep,
										  null, null, true, false),
										  contact.getFaction().getId());
			ReputationAdjustmentResult repC = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.MISSION_FAILURE, completionRep,
							null, null, true, false),
							contact);
			
			return new MissionResult(0, repF, repC);
		}
		
		return new MissionResult();
	}

	@Override
	protected MissionResult createTimeRanOutFailedResult() {
		return createAbandonedResult(true);
	}
	
	
	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_COMMODITY_INFO) {
			return;
		}
		super.buttonPressConfirmed(buttonId, ui);
	}


	@Override
	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
		if (buttonId != BUTTON_COMMODITY_INFO) {
			super.createConfirmationPrompt(buttonId, prompt);
			return;
		}
		
		prompt.setParaFontDefault();
		
		prompt.addPara("This procurement contract offers a price of %s per unit of " + commodity.getCommodity().getName() + ".",
				0f, Misc.getHighlightColor(), Misc.getDGSCredits(pricePerUnit));
		
		Global.getSettings().addCommodityInfoToTooltip(prompt, 10f, commodity.getCommodity(), true, false, true);
	}
	
	@Override
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		if (buttonId == BUTTON_COMMODITY_INFO) {
			return true;
		}
		return super.doesButtonHaveConfirmDialog(buttonId);
	}
	
	public float getConfirmationPromptWidth(Object buttonId) {
		if (buttonId == BUTTON_COMMODITY_INFO) {
			return 664f + 43f + 10f;
		}
		return super.getConfirmationPromptWidth(buttonId);
	}
	
	public String getConfirmText(Object buttonId) {
		if (buttonId == BUTTON_COMMODITY_INFO) {
			return "Dismiss";
		}
		return super.getConfirmText(buttonId);
	}

	public String getCancelText(Object buttonId) {
		if (buttonId == BUTTON_COMMODITY_INFO) {
			return null;
		}
		return super.getCancelText(buttonId);
	}
	
	
}


