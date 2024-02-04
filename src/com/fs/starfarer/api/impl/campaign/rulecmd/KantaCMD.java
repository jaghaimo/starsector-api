package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DelayedBlueprintLearnScript;
import com.fs.starfarer.api.impl.campaign.FusionLampEntityPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel.ContactState;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.KantasProtectionOneTimeFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.PiracyRespiteScript;
import com.fs.starfarer.api.impl.campaign.intel.events.PirateHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 *	KantaCMD <action> <parameters>
 */
public class KantaCMD extends BaseCommandPlugin {
	
	public static final String KANTA_PROTECTION = "$kantaProtection";
	public static final String EVER_HAD_KANTA_PROTECTION = "$everHadKantaProtection";
	
	public static boolean playerHasProtection() {
		//if (true) return false;
		return Global.getSector().getCharacterData().getMemoryWithoutUpdate().getBoolean(KANTA_PROTECTION);
	}
	public static boolean playerEverHadProtection() {
		return Global.getSector().getCharacterData().getMemoryWithoutUpdate().getBoolean(EVER_HAD_KANTA_PROTECTION);
	}
	
	public static class KantaFavorTrigger implements EveryFrameScript {

		public static float MIN_ELAPSED = 720f;
		
		// around 50ish percent after 5 cycles
		// want this to feel unpredictable and like it might not happen
		public static float PROB_PER_MONTH = 0.01f;
		
		public static float MIN_DELAY_BETWEEN_COURIERS = 60f;
		public static float MAX_DELAY_BETWEEN_COURIERS = 90f;
		public static int MAX_ATTEMPTS = 5;
		
		
		protected IntervalUtil interval = new IntervalUtil(20f, 40f);
		protected float elapsed = 0f;
		protected float resendTimeout = 0f;
		protected int attempts = 0;
		protected Random random = new Random();
		protected boolean done = false;

		public void advance(float amount) {
			if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$affk_inProgress")) {
				done = true;
				return;
			}
			
			//amount *= 1000f;
			
			float days = Global.getSector().getClock().convertToDays(amount);
			elapsed += days;
			
			boolean send = false;
			
			interval.advance(days);
			if (interval.intervalElapsed()) {
				if (random.nextFloat() < PROB_PER_MONTH) {
					send = true;
				}
			}
			
//			if (true) {
//				loseProtection(null);
//				Global.getSector().getCampaignUI().addMessage("Kanta's Protection lost", Misc.getNegativeHighlightColor());
//				send = false;
//				done = true;
//				return;
//			}
			
			if (attempts <= 0 && elapsed < MIN_ELAPSED) {
				send = false; // initial timeout
			}
			if (attempts > 0 && elapsed < resendTimeout) {
				send = false; // sent a courier recently
			} else if (attempts > 0) {
				if (attempts >= MAX_ATTEMPTS) { // too many failed attempts
					loseProtection(null);
					Global.getSector().getCampaignUI().addMessage("Kanta's Protection lost", Misc.getNegativeHighlightColor());
					send = false;
					done = true;
					return;
				} else {
					send = true; // sent a courier already, and not recently - always send another one
				}
			}
			
			//send = true;
			if (send) {
				elapsed = 0;
				sendCourier();
				attempts++;
				resendTimeout = MIN_DELAY_BETWEEN_COURIERS + 
							   (MAX_DELAY_BETWEEN_COURIERS - MIN_DELAY_BETWEEN_COURIERS) * random.nextFloat();
			}
		}
		
		public boolean isDone() {
			return done;
		}

		public boolean runWhilePaused() {
			return false;
		}
		
		public void sendCourier() {
			DelayedFleetEncounter e = new DelayedFleetEncounter(random, "kantaFavorCourier");
			e.setDelayNone();
			e.setLocationInnerSector(false, null);
			e.setEncounterFromSomewhereInSystem();
			e.setDoNotAbortWhenPlayerFleetTooStrong();
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.VERY_SMALL, FleetQuality.HIGHER, 
								Factions.PIRATES, FleetTypes.PATROL_SMALL, new Vector2f());
			e.triggerFleetSetName("Courier");
			e.triggerMakeNonHostile();
			e.triggerFleetMakeImportantPermanent(null);
			e.triggerFleetMakeFaster(true, 0, true);
			e.triggerOrderFleetInterceptPlayer();
			e.triggerOrderFleetEBurn(1f);
			e.triggerSetFleetGenericHailPermanent("KantaFavorCourierHail");
			e.endCreate();
		}
		
	}
	
	
	public static class TakingBackTheNanoforgeChecker implements ColonyPlayerHostileActListener {
		protected MarketAPI market;
		public TakingBackTheNanoforgeChecker(MarketAPI market) {
			this.market = market;
		}

		public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market,
				TempData actionData, CargoAPI cargo) {
			if (this.market.hasCondition(Conditions.DECIVILIZED)) {
				Global.getSector().getListenerManager().removeListener(this);
			}
			if (actionData.secret) {
				return;
			}
			if (market == this.market) {
				if (cargo.getQuantity(CargoItemType.SPECIAL, new SpecialItemData(Items.PRISTINE_NANOFORGE, null)) > 0) {
					PersonAPI kanta = People.getPerson(People.KANTA);
					if (kanta != null) {
						TextPanelAPI text = dialog.getTextPanel();
						text.addPara("Word of this is bound to get back to Kanta. "
								+ "You can't imagine she would react well to being made a fool of.");
						loseProtection(dialog);
					}
					Global.getSector().getListenerManager().removeListener(this);
				}
			}
		}

		public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData, Industry industry) {
		}

		public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		}

		public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		}
		
	}
	
	public static class FusionLampColorChanger implements EveryFrameScript {
		public boolean isDone() {
			return market.getContainingLocation() == null;
		}

		public boolean runWhilePaused() {
			return false;
		}
		
		protected MarketAPI market;
		protected IntervalUtil interval = new IntervalUtil(0.5f, 1f);
		public FusionLampColorChanger(MarketAPI market) {
			this.market = market;
		}

		public void advance(float amount) {
			float days = Global.getSector().getClock().convertToDays(amount);
			if (Global.getSector().getClock().getMonth() == 11 &&
					Global.getSector().getClock().getDay() == 28) {
				days *= 100f;
			}
				
			interval.advance(days);
			if (interval.intervalElapsed()) {
				for (CustomCampaignEntityAPI curr : market.getContainingLocation().getCustomEntities()) {
					if (curr.getCustomEntityType().equals(Entities.FUSION_LAMP) &&
							curr.getOrbitFocus() == market.getPrimaryEntity()) {
						
						WeightedRandomPicker<Pair<Color, Color>> picker = new WeightedRandomPicker<Pair<Color, Color>>();
						
						// orange
						picker.add(new Pair<Color, Color>(FusionLampEntityPlugin.GLOW_COLOR, FusionLampEntityPlugin.LIGHT_COLOR));
						
						// red
						picker.add(new Pair<Color, Color>(new Color(255,50,50,255), new Color(255,50,50,255)));
						
						// blue
						picker.add(new Pair<Color, Color>(new Color(210,230,255,255), new Color(210,230,255,255)));
						
						// white
						picker.add(new Pair<Color, Color>(new Color(245,250,255,255), new Color(245,250,255,255)));
						
						// yellow
						picker.add(new Pair<Color, Color>(new Color(255,225,125,255), new Color(255,225,125,255)));
						
						// green
						picker.add(new Pair<Color, Color>(new Color(100,255,100,255), new Color(100,255,100,255)));
						
						Pair<Color, Color> pick = picker.pick();
						
						FusionLampEntityPlugin plugin = (FusionLampEntityPlugin) curr.getCustomPlugin();
						plugin.setGlowColor(pick.one);
						plugin.setLightColor(pick.two);
						
						break;
					}
				}
				
			}
		}
	}
	
	public static class DelayedInstallItemScript extends DelayedActionScript {
		protected MarketAPI market;
		protected String industryId;
		protected String itemId;

		public DelayedInstallItemScript(float daysLeft, MarketAPI market, String industryId, String itemId) {
			super(daysLeft);
			this.market = market;
			this.industryId = industryId;
			this.itemId = itemId;
		}

		@Override
		public void doAction() {
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(itemId);
			Industry ind = market.getIndustry(industryId);
			if (ind != null && effect != null && !market.hasCondition(Conditions.DECIVILIZED)) {
				List<String> unmet = effect.getUnmetRequirements(ind);
				if (unmet == null || unmet.isEmpty()) {
					ind.setSpecialItem(new SpecialItemData(itemId, null));
				}
			}
		}
	}
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
		
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		
		if ("findLargeOmega".equals(action)) {
			for (CargoStackAPI stack : cargo.getStacksCopy()) {
				if (!stack.isWeaponStack()) continue;
				
				WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
				if (spec.getSize() != WeaponSize.LARGE) continue;
				
				if (!spec.hasTag(Tags.OMEGA)) continue;
				
				memory.set("$foundWeaponId", spec.getWeaponId());				
				memory.set("$foundWeaponName", spec.getWeaponName());				
				memory.set("$foundWeaponQuantity", (int) Math.round(stack.getSize()));
				return true;
			}
		} else if ("findContactToBetray".equals(action)) {
			ContactIntel best = null;
			int bestImportance = 0;
			float bestRel = 0f;
			for (IntelInfoPlugin curr : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
				ContactIntel intel = (ContactIntel) curr;
				if (intel.isEnding() || intel.isEnded() || intel.getState() == ContactState.POTENTIAL) continue;
				
				int importance = intel.getPerson().getImportance().ordinal();
				float rel = intel.getPerson().getRelToPlayer().getRel();
				
				if (intel.getPerson().getImportance().ordinal() <= PersonImportance.MEDIUM.ordinal()) continue;
				//if (intel.getPerson().getRelToPlayer().isAtBest(RepLevel.WELCOMING)) continue;
				
				if (importance > bestImportance || (importance >= bestImportance && rel > bestRel)) {
					best = intel;
					bestImportance = importance;
					bestRel = rel;
				}
			}
				
			if (best == null) return false;
			
			PersonAPI contact = best.getPerson();
			memory.set("$foundContactName", contact.getNameString());				
			memory.set("$foundContactRank", contact.getRank().toLowerCase());				
			memory.set("$foundContactPost", contact.getPost().toLowerCase());
			memory.set("$foundContactFaction", contact.getFaction().getPersonNamePrefixAOrAn() + " " + contact.getFaction().getPersonNamePrefix());				
			memory.set("$foundContactIntel", best);				
			
			return true;
		} else if ("betrayContact".equals(action)) {
			ContactIntel intel = (ContactIntel) memory.get("$foundContactIntel");
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = -0.75f;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, 
							impact, null, text, true, true),
							intel.getPerson());
			intel.loseContact(dialog);
		} else if ("gaveHamatsu".equals(action)) {
			PersonAPI ibrahim = People.getPerson(People.IBRAHIM);
			if (ibrahim != null) {
				CustomRepImpact impact = new CustomRepImpact();
				impact.delta = -2f;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, 
								impact, null, text, true, true),
								ibrahim);
				ContactIntel intel = ContactIntel.getContactIntel(ibrahim);
				if (intel !=  null) {
					intel.loseContact(dialog);
				}
			}
		} else if ("findRemnantCapital".equals(action)) {
			for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
				if (member.getHullSpec().hasTag(Tags.SHIP_REMNANTS) && member.isCapital()) {
					memory.set("$foundShipId", member.getId());				
					memory.set("$foundShipClass", member.getHullSpec().getNameWithDesignationWithDashClass());				
					memory.set("$foundShipName", member.getShipName());				
					return true;
				}
			}
		} else if ("findShieldBlueprint".equals(action)) {
			for (CargoStackAPI stack : cargo.getStacksCopy()) {
				if (!stack.isSpecialStack()) continue;
				
				SpecialItemData data = stack.getSpecialDataIfSpecial();
				if (data.getData() == null) continue;
				
				if (!data.getId().equals(Items.INDUSTRY_BP)) continue;
				
				IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(data.getData());
				
				if (spec.getId().equals(Industries.PLANETARYSHIELD)) {
					memory.set("$foundIndustryId", spec.getId());
					String firstWord = stack.getDisplayName().split(" ")[0];
					memory.set("$foundIndustryName", Misc.ucFirst(Misc.getAOrAnFor(firstWord)) + " " + stack.getDisplayName().replace("Blueprint", "blueprint"));				
					return true;
				}
			}
		} else if ("findShipBlueprint".equals(action)) {
			FactionAPI pirates = Global.getSector().getFaction(Factions.PIRATES);
			for (CargoStackAPI stack : cargo.getStacksCopy()) {
				if (!stack.isSpecialStack()) continue;
				
				SpecialItemData data = stack.getSpecialDataIfSpecial();
				if (data.getData() == null) continue;
				
				if (!data.getId().equals(Items.SHIP_BP)) continue;
				
				if (pirates.knowsShip(data.getData())) continue;
				
				ShipHullSpecAPI spec = Global.getSettings().getHullSpec(data.getData());
				
				boolean match = spec.hasTag(Tags.KANTA_GIFT);
				match |= !spec.isCivilianNonCarrier() && spec.getHullSize() == HullSize.CAPITAL_SHIP;
				
				if (match) {
					memory.set("$foundShipBPId", data.getData());
					String firstWord = stack.getDisplayName().split(" ")[0];
					memory.set("$foundShipBPName", Misc.ucFirst(Misc.getAOrAnFor(firstWord)) + " " + stack.getDisplayName().replace("Blueprint", "blueprint"));				
					return true;
				}
			}
		} else if ("learnShipBP".equals(action)) {
			String hullId = params.get(1).getString(memoryMap);
			float daysDelay = params.get(2).getFloat(memoryMap);
			DelayedBlueprintLearnScript script = new DelayedBlueprintLearnScript(Factions.PIRATES, daysDelay);
			script.getShips().add(hullId);
			Global.getSector().addScript(script);
			return true;
		} else if ("oweKantaAFavor".equals(action)) {
			Global.getSector().addScript(new KantaFavorTrigger());
		} else if ("abortFavor".equals(action)) {
			Global.getSector().removeScriptsOfClass(KantaFavorTrigger.class);
		} else if ("alreadyDidFavor".equals(action)) {
			return !Global.getSector().hasScript(KantaFavorTrigger.class) && playerHasProtection();
					//!Global.getSector().getMemoryWithoutUpdate().getBoolean("$affk_inProgress");
		} else if ("installFusionLamp".equals(action)) {
			SpecialItemData data = new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null);
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(data.getId());
			Industry ind = market.getIndustry(Industries.POPULATION);
			if (ind != null && effect != null) {
				List<String> unmet = effect.getUnmetRequirements(ind);
				if (unmet == null || unmet.isEmpty()) {
					ind.setSpecialItem(data);
					market.getPrimaryEntity().addScript(new FusionLampColorChanger(market));
				}
			}
			return true;
		} else if ("buildPlanetaryShield".equals(action)) {
			Industry ind = market.getIndustry(Industries.PLANETARYSHIELD);
			if (ind == null) {
				market.addIndustry(Industries.PLANETARYSHIELD);
				
				// not very promising - shield is under the station etc
//				PlanetAPI planet = market.getContainingLocation().addPlanet(market.getId() + "_fakeForShield", market.getPrimaryEntity(), "", Planets.TUNDRA, 0, 100f, 0f, 1f);
//				market.getConnectedEntities().add(planet);
//				ind = market.getIndustry(Industries.PLANETARYSHIELD);
//				if (ind != null) {
//					ind.startBuilding();
//				}
			}
			return true;
		} else if ("gavePristineNanoforge".equals(action)) {
			float daysDelay = params.get(1).getFloat(memoryMap);
			//daysDelay = 0;
			MarketAPI kapteyn = Global.getSector().getEconomy().getMarket("station_kapteyn");
			if (kapteyn != null) {
				Global.getSector().addScript(
						new DelayedInstallItemScript(daysDelay, 
								kapteyn, Industries.ORBITALWORKS, Items.PRISTINE_NANOFORGE));
				Global.getSector().getListenerManager().addListener(new TakingBackTheNanoforgeChecker(kapteyn));
			}
			return true;
		} else if ("kapteynHasForge".equals(action)) {
			MarketAPI kapteyn = Global.getSector().getEconomy().getMarket("station_kapteyn");
			if (kapteyn == null || kapteyn.hasCondition(Conditions.DECIVILIZED)) return false;
			
			Industry ind = market.getIndustry(Industries.ORBITALWORKS);
			if (ind == null) return false;
			
			return ind.getSpecialItem() != null && Items.PRISTINE_NANOFORGE.equals(ind.getSpecialItem().getId()); 
		} else if ("gainProtection".equals(action)) {
			Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(KANTA_PROTECTION, true);
			Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(EVER_HAD_KANTA_PROTECTION, true);
			text.setFontSmallInsignia();
			text.addPara("Kanta's Protection gained", Misc.getPositiveHighlightColor());
			text.setFontInsignia();
			
			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
			
			PirateHostileActivityFactor.avertOrAbortRaid();
			
			HostileActivityEventIntel intel = HostileActivityEventIntel.get();
			if (intel != null) {
				intel.addFactor(new KantasProtectionOneTimeFactor(-Global.getSettings().getInt("HA_kantaProtection")), dialog);
			}
			
			new PiracyRespiteScript();
			
		} else if ("loseProtection".equals(action)) {
			loseProtection(dialog);
		}
		
		
		
		return false;
	}

	public static void loseProtection(InteractionDialogAPI dialog) {
		if (!playerHasProtection()) return;
		
		PersonAPI kanta = People.getPerson(People.KANTA);
		if (kanta != null) {
			Misc.incrUntrustwortyCount();
			
			TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = -0.5f;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, 
							impact, null, text, true, true),
							kanta);
			Global.getSector().getCharacterData().getMemoryWithoutUpdate().unset(KANTA_PROTECTION);
			if (text != null) {
				text.setFontSmallInsignia();
				text.addPara("Kanta's Protection lost", Misc.getNegativeHighlightColor());
				text.setFontInsignia();
			}
			Global.getSoundPlayer().playUISound("ui_rep_drop", 1f, 1f);
		}
	}
	
}
