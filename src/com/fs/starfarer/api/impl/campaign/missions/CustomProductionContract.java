package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomProductionPickerDelegateImpl;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ItemInProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ProductionItemType;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel.ProductionData;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CustomProductionContract extends HubMissionWithBarEvent {

	public static float ARMS_DEALER_PROB_PATROL_AFTER = 0.5f;
	
	public static float PROD_DAYS = 60f;
	
	public static float PROB_ARMS_DEALER_BAR = 0.25f;
	public static float PROB_MILITARY_BAR = 0.33f;
	public static float PROB_INDEPENDENT_BAR = 0.5f;
	
	public static float PROB_ARMS_DEALER_IS_CONTACT = 0.05f;
	
	public static float MIN_CAPACITY = 50000;
	public static float MAX_CAPACITY = 500000;
	public static int BAR_CAPACITY_BONUS_MIN = 50000;
	public static int BAR_CAPACITY_BONUS_MAX = 150000;
	
	
	public static float MAX_PROD_CAPACITY_AT_SHIP_UNITS = 10;
	public static float MAX_PROD_CAPACITY_MULT = 0.25f;
	
	public static float DEALER_MIN_CAPACITY = 1000000;
	public static float DEALER_MAX_CAPACITY = 2000000;
	public static Map<PersonImportance, Float> DEALER_MULT = new LinkedHashMap<PersonImportance, Float>();
	static {
		DEALER_MULT.put(PersonImportance.VERY_LOW, 0.1f);
		DEALER_MULT.put(PersonImportance.LOW, 0.2f);
		DEALER_MULT.put(PersonImportance.MEDIUM, 0.3f);
		DEALER_MULT.put(PersonImportance.HIGH, 0.6f);
		DEALER_MULT.put(PersonImportance.VERY_HIGH, 1f);
	}
	
	public static float MILITARY_CAP_MULT = 0.6f;
	
	public static float MILITARY_MAX_COST_DECREASE = 0.3f;
	public static float TRADE_MAX_COST_INCREASE = 0.3f;
	public static float DEALER_FIXED_COST_INCREASE = 0.5f;
	public static float DEALER_VARIABLE_COST_INCREASE = 0.5f;
	
	public static enum Stage {
		WAITING,
		DELIVERED,
		COMPLETED, // unused, left in for save compat
		FAILED,
	}

	protected Set<String> ships = new LinkedHashSet<String>();
	protected Set<String> weapons = new LinkedHashSet<String>();
	protected Set<String> fighters = new LinkedHashSet<String>();
	
	protected boolean armsDealer = false;
	protected int maxCapacity;
	protected float costMult;
	protected ProductionData data;
	protected int cost;
	protected FactionAPI faction;
	protected MarketAPI market;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		boolean allowArmsDealer = true; // anywhere is fine
		boolean allowTrader = createdAt != null && createdAt.getCommodityData(Commodities.SHIPS).getMaxSupply() > 0;
		boolean allowMilitary = allowTrader && createdAt != null && Misc.isMilitary(createdAt);
		if (createdAt.isPlayerOwned()) {
			allowTrader = false;
			allowMilitary = false;
		}
		if (Factions.PIRATES.equals(createdAt.getFaction().getId())) {
			allowMilitary = false;
		}
		
		if (barEvent) {
			String post = null;
			if (rollProbability(PROB_ARMS_DEALER_BAR) && allowArmsDealer) {
				setGiverRank(Ranks.CITIZEN);
				post = Ranks.POST_ARMS_DEALER;
				setGiverTags(Tags.CONTACT_UNDERWORLD);
				setGiverFaction(Factions.PIRATES);
			} else if (rollProbability(PROB_MILITARY_BAR) && allowMilitary) {
				List<String> posts = new ArrayList<String>();
				posts.add(Ranks.POST_SUPPLY_OFFICER);
				if (Misc.isMilitary(createdAt)) {
					posts.add(Ranks.POST_BASE_COMMANDER);
				}
				if (Misc.hasOrbitalStation(createdAt)) {
					posts.add(Ranks.POST_STATION_COMMANDER);
				}
				post = pickOne(posts);
				setGiverRank(pickOne(Ranks.GROUND_CAPTAIN, Ranks.GROUND_COLONEL, Ranks.GROUND_MAJOR,
							 Ranks.SPACE_COMMANDER, Ranks.SPACE_CAPTAIN, Ranks.SPACE_ADMIRAL));
				setGiverTags(Tags.CONTACT_MILITARY);
			} else if (allowTrader) {
				setGiverRank(Ranks.CITIZEN);
				post = pickOne(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, Ranks.POST_PORTMASTER,
				 			   Ranks.POST_MERCHANT, Ranks.POST_INVESTOR, Ranks.POST_EXECUTIVE,
				 		 	   Ranks.POST_SENIOR_EXECUTIVE, Ranks.POST_ADMINISTRATOR);
				setGiverTags(Tags.CONTACT_TRADE);
				if (rollProbability(PROB_INDEPENDENT_BAR)) {
					setGiverFaction(Factions.INDEPENDENT);
				}
			}
			if (post == null && allowArmsDealer) {
				setGiverRank(Ranks.CITIZEN);
				post = Ranks.POST_ARMS_DEALER;
				setGiverTags(Tags.CONTACT_UNDERWORLD);
				setGiverFaction(Factions.PIRATES);
			}
			if (post == null) return false;
			
			setGiverPost(post);
			if (post.equals(Ranks.POST_SENIOR_EXECUTIVE) ||
					post.equals(Ranks.POST_BASE_COMMANDER) ||
					post.equals(Ranks.POST_ADMINISTRATOR)) {
				setGiverImportance(pickHighImportance());
			} else if (post.equals(Ranks.POST_ARMS_DEALER)) {
				setGiverImportance(pickArmsDealerImportance());
			} else {
				setGiverImportance(pickImportance());
				
			}
			findOrCreateGiver(createdAt, false, false);
			setGiverIsPotentialContactOnSuccess();
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (!setPersonMissionRef(person, "$cpc_ref")) {
			return false;
		}
		
		market = getPerson().getMarket();
		if (market == null) return false;
		if (Misc.getStorage(market) == null) return false;
		
		faction = person.getFaction();
		
//		armsDealer = Ranks.POST_ARMS_DEALER.equals(person.getPostId());
//		if (!armsDealer) allowArmsDealer = false;
		armsDealer = getPerson().hasTag(Tags.CONTACT_UNDERWORLD);

		maxCapacity = getRoundNumber(MIN_CAPACITY + (MAX_CAPACITY - MIN_CAPACITY) * getQuality());
		if (barEvent) {
			maxCapacity += genRoundNumber(BAR_CAPACITY_BONUS_MIN, BAR_CAPACITY_BONUS_MAX);
		}
		float capMult = market.getCommodityData(Commodities.SHIPS).getMaxSupply() / MAX_PROD_CAPACITY_AT_SHIP_UNITS;
		if (capMult > 1) capMult = 1f;
		if (capMult < MAX_PROD_CAPACITY_MULT) capMult = MAX_PROD_CAPACITY_MULT;
		maxCapacity *= capMult;
		if (person.hasTag(Tags.CONTACT_MILITARY) && allowMilitary) {
			maxCapacity *= MILITARY_CAP_MULT;
		}
		maxCapacity = getRoundNumber(maxCapacity);
		
		if (armsDealer && allowArmsDealer) { // don't care about ship production, since it's just acquisition from wherever
			PersonImportance imp = getPerson().getImportance();
			float mult = DEALER_MULT.get(imp);
			maxCapacity = getRoundNumber(mult * 
						(DEALER_MIN_CAPACITY + (DEALER_MAX_CAPACITY - DEALER_MIN_CAPACITY) * getQuality()));
		}
		
		if (armsDealer && allowArmsDealer) {
			costMult = 1f + DEALER_FIXED_COST_INCREASE + DEALER_VARIABLE_COST_INCREASE * (1f - getRewardMultFraction());
			addArmsDealerBlueprints();
			if (ships.isEmpty() && weapons.isEmpty() && fighters.isEmpty()) return false;
		} else if (person.hasTag(Tags.CONTACT_MILITARY) && allowMilitary) {
			costMult = 1f - MILITARY_MAX_COST_DECREASE * getRewardMultFraction();
			addMilitaryBlueprints();
			if (ships.isEmpty() && weapons.isEmpty() && fighters.isEmpty()) return false;
		} else if (person.hasTag(Tags.CONTACT_TRADE) && allowTrader) {
			costMult = 1f + TRADE_MAX_COST_INCREASE * (1f - getRewardMultFraction());
		} else {
			return false;
		}

		setStartingStage(Stage.WAITING);
		setSuccessStage(Stage.DELIVERED);
		setFailureStage(Stage.FAILED);
		setNoAbandon();
		
		connectWithDaysElapsed(Stage.WAITING, Stage.DELIVERED, PROD_DAYS);
		//connectWithDaysElapsed(Stage.WAITING, Stage.DELIVERED, 1f);
		setStageOnMarketDecivilized(Stage.FAILED, market);
		
		return true;
	}
	

	protected void addArmsDealerBlueprints() {
		boolean [] add = new boolean[3];
		add[genRandom.nextInt(add.length)] = true;
		add[genRandom.nextInt(add.length)] = true;
		add[genRandom.nextInt(add.length)] = true;
		
		PersonImportance imp = getPerson().getImportance();
		if (imp == PersonImportance.VERY_HIGH) {
			add[0] = true;
			add[1] = true;
			add[2] = true;
		}

		Set<WeaponType> wTypes = new LinkedHashSet<WeaponAPI.WeaponType>();
		Set<WeaponSize> wSizes = new LinkedHashSet<WeaponAPI.WeaponSize>();
		Set<HullSize> hullSizes = new LinkedHashSet<HullSize>();

		WeightedRandomPicker<WeaponType> wTypePicker = new WeightedRandomPicker<WeaponType>(genRandom);
		wTypePicker.add(WeaponType.BALLISTIC);
		wTypePicker.add(WeaponType.ENERGY);
		wTypePicker.add(WeaponType.MISSILE);
		WeightedRandomPicker<WeaponSize> wSizePicker = new WeightedRandomPicker<WeaponSize>(genRandom);
		wSizePicker.add(WeaponSize.SMALL);
		wSizePicker.add(WeaponSize.MEDIUM);
		wSizePicker.add(WeaponSize.LARGE);
		
		int nWeapons = 0;
		int nShips = 0;
		int nFighters = 0;
		
		switch (imp) {
		case VERY_LOW:
			add[1] = true;
			wSizes.add(WeaponSize.SMALL);
			wTypes.add(wTypePicker.pickAndRemove());
			nWeapons = 5 + genRandom.nextInt(6);
			nFighters = 1 + genRandom.nextInt(3);
			break;
		case LOW:
			add[1] = true;
			wSizePicker.remove(WeaponSize.LARGE);
			wSizes.add(wSizePicker.pickAndRemove());
			wTypes.add(wTypePicker.pickAndRemove());
			hullSizes.add(HullSize.FRIGATE);
			nWeapons = 10 + genRandom.nextInt(6);
			nShips = 5 + genRandom.nextInt(3);
			nFighters = 3 + genRandom.nextInt(3);
			break;
		case MEDIUM:
			add[1] = true;
			wSizes.add(wSizePicker.pickAndRemove());
			wSizes.add(wSizePicker.pickAndRemove());
			wTypes.add(wTypePicker.pickAndRemove());
			hullSizes.add(HullSize.FRIGATE);
			hullSizes.add(HullSize.DESTROYER);
			nWeapons = 20 + genRandom.nextInt(6);
			nShips = 10 + genRandom.nextInt(3);
			nFighters = 5 + genRandom.nextInt(3);
			break;
		case HIGH:
			add[1] = true;
			wSizes.add(wSizePicker.pickAndRemove());
			wSizes.add(wSizePicker.pickAndRemove());
			wTypes.add(wTypePicker.pickAndRemove());
			wTypes.add(wTypePicker.pickAndRemove());
			hullSizes.add(HullSize.FRIGATE);
			hullSizes.add(HullSize.DESTROYER);
			hullSizes.add(HullSize.CRUISER);
			nWeapons = 20 + genRandom.nextInt(6);
			nShips = 10 + genRandom.nextInt(3);
			nFighters = 7 + genRandom.nextInt(3);
			break;
		case VERY_HIGH:
			wSizes.add(WeaponSize.SMALL);
			wSizes.add(WeaponSize.MEDIUM);
			wSizes.add(WeaponSize.LARGE);
			
			hullSizes.add(HullSize.FRIGATE);
			hullSizes.add(HullSize.DESTROYER);
			hullSizes.add(HullSize.CRUISER);
			hullSizes.add(HullSize.CAPITAL_SHIP);
			
			wTypes.add(WeaponType.BALLISTIC);
			wTypes.add(WeaponType.ENERGY);
			wTypes.add(WeaponType.MISSILE);
			nWeapons = 1000;
			nShips = 1000;
			nFighters = 1000;
			break;
		}
		

		FactionProductionAPI prod = Global.getSector().getPlayerFaction().getProduction().clone();
		prod.clear();
		
		if (add[0]) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(genRandom);
			for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
				//if (!spec.hasTag(Items.TAG_RARE_BP) && !spec.hasTag(Items.TAG_DEALER)) continue;
				if (spec.hasTag(Items.TAG_NO_DEALER)) continue;
				if (spec.hasTag(Tags.NO_SELL) && !spec.hasTag(Items.TAG_DEALER)) continue;
				if (spec.hasTag(Tags.RESTRICTED)) continue;
				if (spec.getHints().contains(ShipTypeHints.HIDE_IN_CODEX)) continue;
				if (spec.getHints().contains(ShipTypeHints.UNBOARDABLE)) continue;
				if (spec.isDefaultDHull() || spec.isDHull()) continue;
				if ("shuttlepod".equals(spec.getHullId())) continue;
				if (ships.contains(spec.getHullId())) continue;
				if (!hullSizes.contains(spec.getHullSize())) continue;
				float cost = prod.createSampleItem(ProductionItemType.SHIP, spec.getHullId(), 1).getBaseCost();
				cost = (int)Math.round(cost * costMult);
				if (cost > maxCapacity) continue;
				picker.add(spec.getHullId(), 10f);
			}
//			int num = 2 + (int)Math.round(genRandom.nextInt(5) * getQuality());
//			num += imp.ordinal() * 2;
//			if (imp == PersonImportance.VERY_HIGH) num = 1000;
			int num = nShips;
			for (int i = 0; i < num && !picker.isEmpty(); i++) {
				ships.add(picker.pickAndRemove());
			}
		}
		
		if (add[1]) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(genRandom);
			for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
				//if (!spec.hasTag(Items.TAG_RARE_BP) && !spec.hasTag(Items.TAG_DEALER)) continue;
				if (spec.hasTag(Items.TAG_NO_DEALER)) continue;
				if (spec.hasTag(Tags.NO_SELL) && !spec.hasTag(Items.TAG_DEALER)) continue;
				if (spec.hasTag(Tags.RESTRICTED)) continue;
				if (spec.getAIHints().contains(AIHints.SYSTEM)) continue;
				if (weapons.contains(spec.getWeaponId())) continue;
				if (!wTypes.contains(spec.getType())) continue;
				if (!wSizes.contains(spec.getSize())) continue;
				float cost = prod.createSampleItem(ProductionItemType.WEAPON, spec.getWeaponId(), 1).getBaseCost();
				cost = (int)Math.round(cost * costMult);
				if (cost > maxCapacity) continue;
				picker.add(spec.getWeaponId(), 10f);
			}
//			int num = 3 + (int)Math.round(genRandom.nextInt(7) * getQuality());
//			num += imp.ordinal() * 2;
//			if (imp == PersonImportance.VERY_HIGH) num = 1000;
			int num = nWeapons;
			for (int i = 0; i < num && !picker.isEmpty(); i++) {
				weapons.add(picker.pickAndRemove());
			}
		}
		
		if (add[2]) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(genRandom);
			for (FighterWingSpecAPI spec : Global.getSettings().getAllFighterWingSpecs()) {
				//if (!spec.hasTag(Items.TAG_RARE_BP) && !spec.hasTag(Items.TAG_DEALER)) continue;
//				if (spec.hasTag(Tags.NO_DROP)) continue;
//				if (spec.hasTag(Tags.NO_SELL)) continue;
				if (spec.hasTag(Items.TAG_NO_DEALER)) continue;
				if (spec.hasTag(Tags.NO_SELL) && !spec.hasTag(Items.TAG_DEALER)) continue;
				if (spec.hasTag(Tags.RESTRICTED)) continue;
				if (fighters.contains(spec.getId())) continue;
				float cost = prod.createSampleItem(ProductionItemType.FIGHTER, spec.getId(), 1).getBaseCost();
				cost = (int)Math.round(cost * costMult);
				if (cost > maxCapacity) continue;
				picker.add(spec.getId(), 10f);
			}
//			int num = 1 + (int)Math.round(genRandom.nextInt(3) * getQuality());
//			num += imp.ordinal() * 2;
//			if (imp == PersonImportance.VERY_HIGH) num = 1000;
			int num = nFighters;
			for (int i = 0; i < num && !picker.isEmpty(); i++) {
				fighters.add(picker.pickAndRemove());
			}
		}
	}
	
	protected void addMilitaryBlueprints() {
		for (String id : faction.getKnownShips()) {
			ShipHullSpecAPI spec = Global.getSettings().getHullSpec(id);
			if (spec.hasTag(Tags.NO_SELL)) continue;
			ships.add(id);
		}
		for (String id : faction.getKnownWeapons()) {
			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
			if (spec.hasTag(Tags.NO_DROP)) continue;
			if (spec.hasTag(Tags.NO_SELL)) continue;
			weapons.add(id);
		}
		for (String id : faction.getKnownFighters()) {
			FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(id);
			if (spec.hasTag(Tags.NO_DROP)) continue;
			if (spec.hasTag(Tags.NO_SELL)) continue;
			fighters.add(id);
		}
	}


	protected void updateInteractionDataImpl() {
		set("$cpc_military", getPerson().hasTag(Tags.CONTACT_MILITARY));
		set("$cpc_trade", getPerson().hasTag(Tags.CONTACT_TRADE));
		set("$cpc_armsDealer", armsDealer);
		
		set("$cpc_barEvent", isBarEvent());
		set("$cpc_manOrWoman", getPerson().getManOrWoman());
		set("$cpc_maxCapacity", Misc.getWithDGS(maxCapacity));
		set("$cpc_costPercent", (int)Math.round(costMult * 100f) + "%");
		set("$cpc_days", "" + (int) PROD_DAYS);
	}
	
	@Override
	public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.WAITING) {
			float elapsed = getElapsedInCurrentStage();
			int d = (int) Math.round(PROD_DAYS - elapsed);
			PersonAPI person = getPerson();
			
			LabelAPI label = info.addPara("The order will be delivered to storage " + market.getOnOrAt() + " " + market.getName() + 
					" in %s " + getDayOrDays(d) + ".", opad,
					Misc.getHighlightColor(), "" + d);
			label.setHighlight(market.getName(), "" + d);
			label.setHighlightColors(market.getFaction().getBaseUIColor(), h);
			
			//intel.createSmallDescription(info, width, height);
			showCargoContents(info, width, height);
			
			
		} else if (currentStage == Stage.DELIVERED) {
			float elapsed = getElapsedInCurrentStage();
			int d = (int) Math.round(elapsed);
			LabelAPI label = info.addPara("The order was delivered to storage " + market.getOnOrAt() + " " + market.getName() + 
					" %s " + getDayOrDays(d) + " ago.", opad,
					Misc.getHighlightColor(), "" + d);
			label.setHighlight(market.getName(), "" + d);
			label.setHighlightColors(market.getFaction().getBaseUIColor(), h);
			
			showCargoContents(info, width, height);
			addDeleteButton(info, width);
		} else if (currentStage == Stage.FAILED) {
			if (market.hasCondition(Conditions.DECIVILIZED)) {
				info.addPara("This order will not be completed because %s" + 
						" has decivilized.", opad,
						faction.getBaseUIColor(), market.getName());
			} else {
				info.addPara("You've learned that this order will not be completed.", opad);
			}
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.WAITING) {
			float elapsed = getElapsedInCurrentStage();
			addDays(info, "until delivery", PROD_DAYS - elapsed, tc, pad);
			return true;
		} else if (currentStage == Stage.DELIVERED) {
			info.addPara("Delivered to %s", pad, tc, market.getFaction().getBaseUIColor(), market.getName());
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Custom Production Order";
	}
	
	protected String getMissionTypeNoun() {
		return "contract";
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return market.getPrimaryEntity();
	}
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		float f = (float) cost / (float) maxCapacity;
		float p = ContactIntel.DEFAULT_POTENTIAL_CONTACT_PROB * f;
		if (armsDealer) {
			p = PROB_ARMS_DEALER_IS_CONTACT * f;
		}
		if (potentialContactsOnMissionSuccess != null) {
			for (PotentialContactData data : potentialContactsOnMissionSuccess) {
				data.probability = p;
			}
		}
		
		AddRemoveCommodity.addCreditsLossText(cost, dialog.getTextPanel());
		Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(cost);
		adjustRep(dialog.getTextPanel(), null, RepActions.MISSION_SUCCESS);
		addPotentialContacts(dialog);
		
		ships = null;
		fighters = null;
		weapons = null;
	}
	

	@Override
	public void setCurrentStage(Object next, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.setCurrentStage(next, dialog, memoryMap);
		
		if (currentStage == Stage.DELIVERED) {
			StoragePlugin plugin = (StoragePlugin) Misc.getStorage(getPerson().getMarket());
			if (plugin == null) return;
			plugin.setPlayerPaidToUnlock(true);
			
			CargoAPI cargo = plugin.getCargo();
			for (CargoAPI curr : data.data.values()) {
				cargo.addAll(curr, true);
			}
			
			//endSuccess(dialog, memoryMap);
			
			if (armsDealer && rollProbability(ARMS_DEALER_PROB_PATROL_AFTER)) {
				PersonAPI person = getPerson();
				if (person == null || person.getMarket() == null) return;
				String patrolFaction = person.getMarket().getFactionId();
				if (patrolFaction.equals(person.getFaction().getId()) || 
						Misc.isPirateFaction(person.getMarket().getFaction()) ||
						Misc.isDecentralized(person.getMarket().getFaction()) ||
						patrolFaction.equals(Factions.PLAYER)) {
					return;
				}
				
				DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
				e.setDelayMedium();
				e.setLocationCoreOnly(true, patrolFaction);
				e.beginCreate();
				e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, patrolFaction, FleetTypes.PATROL_LARGE, new Vector2f());
				e.setFleetWantsThing(patrolFaction, 
						"information regarding the arms dealer", "it",
						"information concerning the activities of known arms dealer, " + person.getNameString(),
						getRoundNumber(cost / 2),
						false, ComplicationRepImpact.FULL,
						DelayedFleetEncounter.TRIGGER_REP_LOSS_HIGH, getPerson());
				e.triggerSetAdjustStrengthBasedOnQuality(true, getQuality());
				e.triggerSetPatrol();
				e.triggerSetStandardAggroInterceptFlags();
				e.endCreate();
			}
		}
	}
	
	
	@Override
	protected boolean callAction(final String action, final String ruleId, final InteractionDialogAPI dialog, 
								 final List<Token> params,
								 final Map<String, MemoryAPI> memoryMap) {
		if ("pickPlayerBP".equals(action)) {
			dialog.showCustomProductionPicker(new BaseCustomProductionPickerDelegateImpl() {
				@Override
				public float getCostMult() {
					return costMult;
				}
				@Override
				public float getMaximumValue() {
					return maxCapacity;
				}
				@Override
				public void notifyProductionSelected(FactionProductionAPI production) {
					convertProdToCargo(production);
					FireBest.fire(null, dialog, memoryMap, "CPCBlueprintsPicked");
				}
			});
			return true;
		}
		if ("pickContactBP".equals(action)) {
			dialog.showCustomProductionPicker(new BaseCustomProductionPickerDelegateImpl() {
				@Override
				public Set<String> getAvailableFighters() {
					return fighters;
				}
				@Override
				public Set<String> getAvailableShipHulls() {
					return ships;
				}
				@Override
				public Set<String> getAvailableWeapons() {
					return weapons;
				}
				@Override
				public float getCostMult() {
					return costMult;
				}
				@Override
				public float getMaximumValue() {
					return maxCapacity;
				}
				@Override
				public void notifyProductionSelected(FactionProductionAPI production) {
					convertProdToCargo(production);
					FireBest.fire(null, dialog, memoryMap, "CPCBlueprintsPicked");
				}
			});
			return true;
		}
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}
	
	
	protected void convertProdToCargo(FactionProductionAPI prod) {
		cost = prod.getTotalCurrentCost();
		data = new ProductionData();
		CargoAPI cargo = data.getCargo("Order manifest");
		
		float quality = ShipQuality.getShipQuality(market, market.getFactionId());
		if (armsDealer) {
			quality = Math.max(quality, 1f);
		}

		CampaignFleetAPI ships = Global.getFactory().createEmptyFleet(market.getFactionId(), "temp", true);
		ships.setCommander(Global.getSector().getPlayerPerson());
		ships.getFleetData().setShipNameRandom(genRandom);
		DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
		p.quality = quality;
		p.mode = ShipPickMode.PRIORITY_THEN_ALL;
		p.persistent = false;
		p.seed = genRandom.nextLong();
		p.timestamp = null;
		
		FleetInflater inflater = Misc.getInflater(ships, p);
		ships.setInflater(inflater);
		
		for (ItemInProductionAPI item : prod.getCurrent()) {
			int count = item.getQuantity();
				
			if (item.getType() == ProductionItemType.SHIP) {
				for (int i = 0; i < count; i++) {
					ships.getFleetData().addFleetMember(item.getSpecId() + "_Hull");
				}
			} else if (item.getType() == ProductionItemType.FIGHTER) {
				cargo.addFighters(item.getSpecId(), count);
			} else if (item.getType() == ProductionItemType.WEAPON) {
				cargo.addWeapons(item.getSpecId(), count);
			}
		}
		
		// so that it adds d-mods
		ships.inflateIfNeeded();
		for (FleetMemberAPI member : ships.getFleetData().getMembersListCopy()) {
			// it should be due to the inflateIfNeeded() call, this is just a safety check
			if (member.getVariant().getSource() == VariantSource.REFIT) {
				member.getVariant().clear();
			}
			cargo.getMothballedShips().addFleetMember(member);
		}
	}
	
	public void showCargoContents(TooltipMakerAPI info, float width, float height) {
		if (data == null) return;
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float small = 3f;
		float opad = 10f;

		List<String> keys = new ArrayList<String>(data.data.keySet());
		Collections.sort(keys, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		for (String key : keys) {
			CargoAPI cargo = data.data.get(key);
			if (cargo.isEmpty() && 
					((cargo.getMothballedShips() == null || 
					  cargo.getMothballedShips().getMembersListCopy().isEmpty()))) {
				continue;
			}
		
			info.addSectionHeading(key, faction.getBaseUIColor(), faction.getDarkUIColor(), 
								   Alignment.MID, opad);
			
			if (!cargo.getStacksCopy().isEmpty()) {
				info.addPara("Ship weapons and fighters:", opad);
				info.showCargo(cargo, 20, true, opad);
			}
			
			if (!cargo.getMothballedShips().getMembersListCopy().isEmpty()) {
				CountingMap<String> counts = new CountingMap<String>();
				for (FleetMemberAPI member : cargo.getMothballedShips().getMembersListCopy()) {
					counts.add(member.getVariant().getHullSpec().getHullName() + " " + member.getVariant().getDesignation());
				}
				
				info.addPara("Ship hulls:", opad);
				info.showShips(cargo.getMothballedShips().getMembersListCopy(), 20, true,
							   getCurrentStage() == Stage.WAITING, opad);
			}
		}
	}
	
	public PersonImportance pickArmsDealerImportance() {
		WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(genRandom);
		
		picker.add(PersonImportance.VERY_LOW, 10f);
		picker.add(PersonImportance.LOW, 10f);
		
//		int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
//		if (credits >= 200000) {
//			picker.add(PersonImportance.MEDIUM, 10f);
//		}
//		if (credits >= 1000000) {
//			picker.add(PersonImportance.HIGH, 10f);
//		}
//		if (credits >= 200000) {
//			picker.add(PersonImportance.VERY_HIGH, 10f);
//		}
		
		float cycles = PirateBaseManager.getInstance().getDaysSinceStart() / 365f;
		if (cycles > 1f) {
			picker.remove(PersonImportance.VERY_LOW);
			picker.add(PersonImportance.MEDIUM, 20f);
		}
		if (cycles > 3f) {
			picker.remove(PersonImportance.LOW);
			picker.add(PersonImportance.HIGH, 10f);
		}
		if (cycles > 5f) {
			//picker.add(PersonImportance.VERY_HIGH, 10f);
			// always very high importance past a certain point, since the goal is to allow easier procurement
			// of almost any "generally available" hull
			return PersonImportance.VERY_HIGH;
		}
		
		return picker.pick();
	}
	
}











