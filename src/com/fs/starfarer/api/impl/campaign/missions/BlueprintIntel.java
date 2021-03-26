package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BlueprintIntel extends HubMissionWithBarEvent implements ShowLootListener {

	public static float PROB_PATHER = 0.5f;
	public static float PROB_MERC = 0.5f;
	
	public static float PROB_TRY_TO_FIND_RUINS = 0.5f;
	public static float FREQ_WEAPON = 10f;
	public static float FREQ_FIGHTER = 10f;
	public static float FREQ_SHIP = 10f;
	
	public static enum Stage {
		GET_ITEM,
		COMPLETED,
	}
	
	protected StarSystemAPI system;
	
	protected SpecialItemData item;
	protected int price;
	protected SectorEntityToken entity;
	
	protected float getQualityMultForTier(int tier) {
		float q = getQuality();
		float qWeight = 1f;
		if (tier <= 0) qWeight = 0;
		else if (tier == 1) qWeight = 1f;
		else if (tier == 2) qWeight = 2f;
		else if (tier >= 3) qWeight = 4f;

		return (1f - qWeight) + qWeight * q;
	}
	
	protected void pickItem() {
		
		WeightedRandomPicker<String> typePicker = new WeightedRandomPicker<String>(genRandom);
		typePicker.add(Items.FIGHTER_BP, FREQ_FIGHTER);
		typePicker.add(Items.WEAPON_BP, FREQ_WEAPON);
		typePicker.add(Items.SHIP_BP, FREQ_SHIP);
		
		while (item == null && !typePicker.isEmpty()) {
			String type = typePicker.pick();
			
			if (type.equals(Items.FIGHTER_BP)) {
				WeightedRandomPicker<FighterWingSpecAPI> picker = new WeightedRandomPicker<FighterWingSpecAPI>(genRandom);
				for (FighterWingSpecAPI spec : Global.getSettings().getAllFighterWingSpecs()) {
					if (!spec.hasTag(Items.TAG_RARE_BP)) continue;
					if (spec.hasTag(Tags.NO_DROP)) continue;
					if (Global.getSector().getPlayerFaction().knowsFighter(spec.getId())) continue;
					float w = spec.getRarity() * getQualityMultForTier(spec.getTier());
					picker.add(spec, w);
				}
				FighterWingSpecAPI pick = picker.pick();
				if (pick != null) {
					item = new SpecialItemData(type, pick.getId());
					price = (int) pick.getBaseValue();
				}
			} else if (type.equals(Items.WEAPON_BP)) {
				WeightedRandomPicker<WeaponSpecAPI> picker = new WeightedRandomPicker<WeaponSpecAPI>(genRandom);
				for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
					if (!spec.hasTag(Items.TAG_RARE_BP)) continue;
					if (spec.hasTag(Tags.NO_DROP)) continue;
					if (Global.getSector().getPlayerFaction().knowsWeapon(spec.getWeaponId())) continue;
					float w = spec.getRarity() * getQualityMultForTier(spec.getTier());;
					picker.add(spec, w);
				}
				WeaponSpecAPI pick = picker.pick();
				if (pick != null) {
					item = new SpecialItemData(type, pick.getWeaponId());
					price = (int) pick.getBaseValue();
				}
			} else if (type.equals(Items.SHIP_BP)) {
				WeightedRandomPicker<ShipHullSpecAPI> picker = new WeightedRandomPicker<ShipHullSpecAPI>(genRandom);
				for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
					if (!spec.hasTag(Items.TAG_RARE_BP)) continue;
					if (spec.hasTag(Tags.NO_DROP)) continue;
					if (Global.getSector().getPlayerFaction().knowsWeapon(spec.getHullId())) continue;
					float w = spec.getRarity() * getQualityMultForTier(spec.getFleetPoints() / 6);;
					picker.add(spec, w);
				}
				ShipHullSpecAPI pick = picker.pick();
				if (pick != null) {
					item = new SpecialItemData(type, pick.getHullId());
					price = (int) pick.getBaseValue();
				}
			}
		}
		
		if (price != 0) {
			price = getRoundNumber(price * (1.5f + (1f - getRewardMultFraction())));
		}
	}
	
	protected String getItemNameLowercaseItem() {
		String item = getItemName();
		item = item.replaceFirst(" Item", " item");
		item = item.replaceFirst(" Blueprint", " blueprint");
		return item;
	}
	protected String getItemName() {
		if (item == null) return "an Invalid Item";
		
		String name = "Invalid Item";
		if (item.getId().equals(Items.FIGHTER_BP)) {
			FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(item.getData());
			name = spec.getWingName() + " Blueprint";
		} else if (item.getId().equals(Items.WEAPON_BP)) {
			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(item.getData());
			name = spec.getWeaponName() + " Blueprint";
		} else if (item.getId().equals(Items.SHIP_BP)) {
			ShipHullSpecAPI spec = Global.getSettings().getHullSpec(item.getData());
			name = spec.getHullName() + " Blueprint";
		}
		return Misc.getAOrAnFor(name) + " " + name;
	}
	
	protected boolean isVeryValuable() {
		if (item.getId().equals(Items.FIGHTER_BP)) {
			//FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(item.getData());
			return false;
		} else if (item.getId().equals(Items.WEAPON_BP)) {
			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(item.getData());
			return spec.getSize() == WeaponSize.LARGE;
		} else if (item.getId().equals(Items.SHIP_BP)) {
			ShipHullSpecAPI spec = Global.getSettings().getHullSpec(item.getData());
			return spec.getHullSize().ordinal() >= HullSize.DESTROYER.ordinal();
		}
		return false;
	}
	
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(pickOne(Ranks.POST_AGENT, Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
						 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
			setGiverImportance(pickImportance());
			setGiverFaction(Factions.PIRATES);
			setGiverTags(Tags.CONTACT_UNDERWORLD);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		pickItem();
		if (item == null) return false;
		
		
		if (!setPersonMissionRef(person, "$sitm_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		requireSystemInterestingAndNotCore();
		preferSystemUnexplored();
		preferSystemTags(ReqMode.NOT_ANY, Tags.THEME_DERELICT);

		entity = null;
		if (rollProbability(PROB_TRY_TO_FIND_RUINS)) {
			requirePlanetUnpopulated();
			requirePlanetWithRuins();
			requirePlanetNotFullySurveyed();
			requirePlanetUnexploredRuins();
			preferPlanetInDirectionOfOtherMissions();
			entity = pickPlanet();
		}
		
		if (entity == null) {
			requireEntityTags(ReqMode.ANY, Tags.SALVAGEABLE);
			preferEntityInDirectionOfOtherMissions();
			entity = pickEntity();
		}
		
		if (entity == null) {
			return false;
		}
		
		system = entity.getStarSystem();
		if (system == null) return false;
		
		setStartingStage(Stage.GET_ITEM);
		setSuccessStage(Stage.COMPLETED);
		
		addTag(Tags.INTEL_EXPLORATION);
		
		boolean veryValuable = isVeryValuable();
		
		int numScav = genRandom.nextInt(3);
		if (veryValuable) numScav += 2;
		
		for (int i = 0; i < numScav; i++) {
			beginWithinHyperspaceRangeTrigger(system, 3f, false, Stage.GET_ITEM);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.SCAVENGERS, FleetTypes.SCAVENGER_MEDIUM, system);
			triggerAutoAdjustFleetStrengthMajor();
			triggerSetFleetFaction(Factions.INDEPENDENT);
			
			triggerMakeLowRepImpact();
			
			triggerSpawnFleetNear(system.getCenter(), null, "$sitm_ref");
			triggerFleetSetTravelActionText("exploring system");
			triggerFleetSetPatrolActionText("scanning local volume");
			triggerOrderFleetPatrol(system, true, Tags.SALVAGEABLE, Tags.PLANET);
			triggerFleetSetWarnAttack("SITMScavWarning", "SITMScavAttack", Stage.GET_ITEM);
			endTrigger();
		}
		
		
		if (veryValuable && rollProbability(PROB_MERC)) {
			beginWithinHyperspaceRangeTrigger(system, 3f, false, Stage.GET_ITEM);
			triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.VERY_HIGH, Factions.MERCENARY, FleetTypes.PATROL_LARGE, system);
			triggerSetFleetFaction(Factions.INDEPENDENT);
			triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			triggerAutoAdjustFleetStrengthMajor();
			triggerMakeLowRepImpact();
			triggerFleetAllowLongPursuit();
			triggerSetFleetAlwaysPursue();
			triggerSpawnFleetNear(system.getCenter(), null, "$sitm_ref");
			triggerOrderFleetPatrol(system, true, Tags.JUMP_POINT);
			triggerFleetSetWarnAttack("SITMMercWarning", "SITMMercAttack", Stage.GET_ITEM);
			endTrigger();
		}
		
		
		return true;
	}
	

	public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
		for (CargoStackAPI stack : loot.getStacksCopy()) {
			if (item.equals(stack.getData())) {
				Global.getSector().getListenerManager().removeListener(this);
				setCurrentStage(Stage.COMPLETED, dialog, ((RuleBasedInteractionDialogPluginImpl)dialog.getPlugin()).getMemoryMap());
				break;
			}
		}
	}
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.acceptImpl(dialog, memoryMap);
		addSpecialItemDropOnlyUseInAcceptImplNotUndoneOnAbort(entity, item);
		Global.getSector().getListenerManager().addListener(this);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		Global.getSector().getListenerManager().removeListener(this);
		
		if (!rollProbability(PROB_PATHER)) return;
		
		PersonAPI person = getPerson();
		if (person == null || person.getMarket() == null) return;
		String patrolFaction = person.getMarket().getFactionId();
		if (patrolFaction.equals(person.getFaction().getId()) || 
				Misc.isPirateFaction(person.getMarket().getFaction())) {
			return;
		}
		
		DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
		e.setDelayNone();
		e.setEncounterInHyper();
		e.setLocationAnywhere(true, Factions.LUDDIC_PATH);
		e.beginCreate();
		e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.LUDDIC_PATH, FleetTypes.PATROL_LARGE, system);
		e.triggerSetAdjustStrengthBasedOnQuality(true, getQuality());
		e.triggerSetStandardAggroInterceptFlags();
		e.triggerSetFleetGenericHailPermanent("SITMPatherHail");
		e.triggerFleetPatherNoDefaultTithe();
		e.endCreate();
	}


	protected void updateInteractionDataImpl() {
		set("$sitm_barEvent", isBarEvent());
		set("$sitm_manOrWoman", getPerson().getManOrWoman());
		set("$sitm_heOrShe", getPerson().getHeOrShe());
		
		set("$sitm_price", Misc.getWithDGS(price));
		
		set("$sitm_aOrAnItem", getItemNameLowercaseItem());
		set("$sitm_item", getWithoutArticle(getItemNameLowercaseItem()));
		
		set("$sitm_personName", getPerson().getNameString());
		set("$sitm_systemName", system.getNameWithLowercaseTypeShort());
		set("$sitm_dist", getDistanceLY(system));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GET_ITEM) {
			info.addPara("There is " + getItemNameLowercaseItem() + 
					" to be found somewhere in the " + system.getNameWithLowercaseTypeShort() + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GET_ITEM) {
			info.addPara("There is " + getItemNameLowercaseItem() + " in the " +  
					system.getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return Misc.ucFirst(getWithoutArticle(getItemName())) + " Intel";
	}
	
	protected String getMissionTypeNoun() {
		return "intel";
	}
	
	protected String getMissionCompletionVerb() {
		return "acted on";
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return getMapLocationFor(system.getHyperspaceAnchor());
	}
}






