package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.impl.campaign.CoreCampaignPluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.econ.impl.TechMining;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.StarSystemData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BlueprintSpecial.BlueprintSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial.BreadcrumbSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.CargoManifestSpecial.CargoManifestSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SleeperPodsSpecial.SleeperPodsSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SleeperPodsSpecial.SleeperSpecialType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial.SurveyDataSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial.SurveyDataSpecialType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TransmitterTrapSpecial.TransmitterTrapSpecialData;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SalvageSpecialAssigner {

	public static class SpecialCreationContext {
		public String themeId;
		public List<SectorEntityToken> all = new ArrayList<SectorEntityToken>();
	}
	
	public static interface SpecialCreator {
		Object createSpecial(SectorEntityToken entity, SpecialCreationContext context);
	}
	
	public static void assignSpecialForBattleWreck(SectorEntityToken entity) {
		Random random = StarSystemGenerator.random;
		WeightedRandomPicker<SpecialCreator> picker = new WeightedRandomPicker<SpecialCreator>(random);
		
		picker.add(new NothingSpecialCreator(), 10f);
		picker.add(new ShipRecoverySpecialCreator(random, 0, 0, false, null, null), 10f);
		SpecialCreator pick = picker.pick();
		
		SpecialCreationContext context = new SpecialCreationContext();
		
		Object specialData = pick.createSpecial(entity, context);
		if (specialData != null) {
			Misc.setSalvageSpecial(entity, specialData);
		}
	}
	
	public static void assignSpecialForDistressDerelict(SectorEntityToken entity) {
		Random random = new Random();
		
		WeightedRandomPicker<SpecialCreator> picker = new WeightedRandomPicker<SpecialCreator>(random);
		
		DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) entity.getCustomPlugin();
		PerShipData shipData = plugin.getData().ship;
		ShipVariantAPI variant = shipData.variant;
		if (variant == null && shipData.variantId != null) {
			variant = Global.getSettings().getVariant(shipData.variantId);
		}
		float p = variant.getHullSpec().getMaxCrew();
		float c = variant.getHullSpec().getCargo();
		
		WeightedRandomPicker<String> recoverableShipFactions = getNearbyFactions(random, entity);
		if (entity.getContainingLocation().hasTag(Tags.THEME_REMNANT)) {
			recoverableShipFactions = Misc.createStringPicker(random,
					Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f);
		}
		WeightedRandomPicker<String> officerFactions = recoverableShipFactions;
		WeightedRandomPicker<String> valuableCargo = getValuableCargo(random);
		
		picker.add(new NothingSpecialCreator(), 10f);
		picker.add(new ShipRecoverySpecialCreator(random, 0, 0, false, null, null), 30f);
		picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, p * 0.25f, p * 0.5f, null), 20f);
		picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 15f);
		picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 1, officerFactions), 2f);
		picker.add(new CargoManifestSpecialCreator(random, valuableCargo, c * 0.25f, c * 0.5f), 10f);
		picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK_NOT_SYSTEM), 5f);
		picker.add(new BlueprintSpecialCreator(random), 1f);
		
		SpecialCreator pick = picker.pick();
		SpecialCreationContext context = new SpecialCreationContext();
		
		Object specialData = pick.createSpecial(entity, context);
		if (specialData != null) {
			Misc.setSalvageSpecial(entity, specialData);
		}
	}
	
	public static void assignSpecialForDebrisField(SectorEntityToken entity) {
		Random random = StarSystemGenerator.random;
		WeightedRandomPicker<SpecialCreator> picker = new WeightedRandomPicker<SpecialCreator>(random);
		
		WeightedRandomPicker<String> recoverableShipFactions = getNearbyFactions(random, entity);
		WeightedRandomPicker<String> trapFactions = Misc.createStringPicker(random, Factions.PIRATES, 10f);
		WeightedRandomPicker<String> valuableCargo = getValuableCargo(random);
		
		picker.add(new NothingSpecialCreator(), 60f);
		picker.add(new ShipRecoverySpecialCreator(random, 1, 3, true, null, recoverableShipFactions), 10f);
		picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, 20, 40, null), 6f);
		picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ORGANS, 1, 5, null), 3f);
		picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, recoverableShipFactions), 1f);
		picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, recoverableShipFactions), 0.2f);
		picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 10, 50), 10f);
		picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK_NOT_SYSTEM), 10f);
		picker.add(new BreadcrumbSpecialCreator(random, null), 10f);
		picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 25), 10f);
		
		SpecialCreator pick = picker.pick();
		
		SpecialCreationContext context = new SpecialCreationContext();
		
		Object specialData = pick.createSpecial(entity, context);
		if (specialData != null) {
			Misc.setSalvageSpecial(entity, specialData);
		}
	}
	
	public static WeightedRandomPicker<String> getValuableCargo(Random random) {
		WeightedRandomPicker<String> valuableCargo = Misc.createStringPicker(random,
				Commodities.VOLATILES, 10f, Commodities.RARE_METALS, 10f, Commodities.RARE_ORE, 10f,
				Commodities.HEAVY_MACHINERY, 10f,
				Commodities.HAND_WEAPONS, 10f, Commodities.ORGANS, 10f, Commodities.DRUGS, 10f,
				Commodities.LUXURY_GOODS, 10f, Commodities.LOBSTER, 10f);
		return valuableCargo;
	}
	
	public static WeightedRandomPicker<String> getIndustryCargo(Random random) {
		WeightedRandomPicker<String> industryCargo = Misc.createStringPicker(random,
				Commodities.VOLATILES, 10f, Commodities.RARE_METALS, 10f, Commodities.RARE_ORE, 10f,
				Commodities.HEAVY_MACHINERY, 10f, Commodities.ORE, 10f);
		return industryCargo;
	}
	
	public static WeightedRandomPicker<String> getHabCargo(Random random) {
		WeightedRandomPicker<String> habCargo = Misc.createStringPicker(random,
				Commodities.HAND_WEAPONS, 10f, Commodities.ORGANS, 10f, Commodities.DRUGS, 10f,
				Commodities.LUXURY_GOODS, 10f, Commodities.LOBSTER, 10f);
		return habCargo;
	}
	
	public static WeightedRandomPicker<String> getNearbyFactions(Random random, SectorEntityToken entity) {
		WeightedRandomPicker<String> picker = Misc.createStringPicker(random, Factions.INDEPENDENT, 10f);
		for (MarketAPI market : Misc.getNearbyMarkets(entity.getLocationInHyperspace(), 10f)) {
			picker.add(market.getFactionId(), market.getSize());
		}
		return picker;
	}
	
	public static WeightedRandomPicker<String> getNearbyFactions(Random random, SectorEntityToken entity,
																float rangeLY,
																float indWeight, float pirateWeight) {
		if (random == null) random = new Random();
		return getNearbyFactions(random, entity.getLocationInHyperspace(), rangeLY, indWeight, pirateWeight);
	}
	public static WeightedRandomPicker<String> getNearbyFactions(Random random, Vector2f locationInHyper,
														float rangeLY,
														float indWeight, float pirateWeight) {
		if (random == null) random = new Random();
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		if (indWeight > 0) picker.add(Factions.INDEPENDENT, indWeight);
		if (pirateWeight > 0) picker.add(Factions.PIRATES, pirateWeight);
		for (MarketAPI market : Misc.getNearbyMarkets(locationInHyper, rangeLY)) {
			picker.add(market.getFactionId(), market.getSize());
		}
		return picker;
	}
	
	public static SpecialCreator pickSpecialFor(SectorEntityToken entity, SpecialCreationContext context) {
		
		WeightedRandomPicker<SpecialCreator> picker = new WeightedRandomPicker<SpecialCreator>(StarSystemGenerator.random);
		
		Random random = StarSystemGenerator.random;

		String type = entity.getCustomEntityType();
		
		WeightedRandomPicker<String> recoverableShipFactions = getNearbyFactions(random, entity);
		
		if (entity.getContainingLocation().hasTag(Tags.THEME_REMNANT)) {
			recoverableShipFactions = Misc.createStringPicker(random,
					Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f);
		}
		
		
		
		WeightedRandomPicker<String> remnantsFaction = Misc.createStringPicker(random, Factions.REMNANTS, 10f);
		WeightedRandomPicker<String> piratesFaction = Misc.createStringPicker(random, Factions.PIRATES, 10f);
		
		
		WeightedRandomPicker<String> trapFactions = piratesFaction;
		if (entity.getContainingLocation().hasTag(Tags.THEME_REMNANT_SUPPRESSED) ||
				entity.getContainingLocation().hasTag(Tags.THEME_REMNANT_RESURGENT)) {
			trapFactions = remnantsFaction;
		}
		
		
//		WeightedRandomPicker<String> officerFactions = Misc.createStringPicker(random,
//					Factions.PIRATES, 10f, Factions.HEGEMONY, 5f, Factions.INDEPENDENT, 10f,
//					Factions.TRITACHYON, 5f, Factions.LUDDIC_CHURCH, 5f, Factions.PERSEAN, 10f,
//					Factions.DIKTAT, 5f);
//		
//		if (entity.getContainingLocation().hasTag(Tags.THEME_REMNANT)) {
//			officerFactions.add(Factions.TRITACHYON, 10f);
//			officerFactions.add(Factions.HEGEMONY, 5f);
//		}
		
		WeightedRandomPicker<String> officerFactions = recoverableShipFactions;
		
		
		
		WeightedRandomPicker<String> valuableCargo = getValuableCargo(random);
		WeightedRandomPicker<String> industryCargo = getIndustryCargo(random);
		
		WeightedRandomPicker<String> habCargo = getHabCargo(random);
		
		
		// ruins on a planet
		if (entity instanceof PlanetAPI) {
			
			float sizeMult = TechMining.getTechMiningRuinSizeModifier(entity.getMarket());
			
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 3, false, DerelictType.SMALL, recoverableShipFactions), 10f);
			if (sizeMult > 0) {
				picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, 500 * sizeMult, 1000 * sizeMult, null), 6f);
				picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ORGANS, 50 * sizeMult, 500 * sizeMult, null), 3f);
			}
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			// min/max doesn't matter for ADMIN
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 5f);
			
			picker.add(new CargoManifestSpecialCreator(random, industryCargo, 500 * sizeMult, 2500 * sizeMult), 15f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 500 * sizeMult, 2500 * sizeMult), 15f);
			
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_LARGE, trapFactions, 
					(int)(10 + 40 * sizeMult), (int)(10 + 40 * sizeMult)), 10f);
			
			// text for these is not set up to handle the "planet" case
			//picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK), 20f);
			//picker.add(new BreadcrumbSpecialCreator(random, context.all), 10f);
		}
		
		
		// derelict ship
		if (entity.getCustomPlugin() instanceof DerelictShipEntityPlugin || Entities.WRECK.equals(type)) {
			DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) entity.getCustomPlugin();
			
			PerShipData shipData = plugin.getData().ship;
			ShipVariantAPI variant = shipData.variant;
			if (variant == null && shipData.variantId != null) {
				variant = Global.getSettings().getVariant(shipData.variantId);
			}
			float p = variant.getHullSpec().getMaxCrew();
			float c = variant.getHullSpec().getCargo();
			
			picker.add(new NothingSpecialCreator(), 40f);
			picker.add(new ShipRecoverySpecialCreator(random, 0, 0, false, null, null), 30f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, p * 0.25f, p * 0.5f, null), 7f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ORGANS, p * 0.1f, p * 0.2f, null), 3f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 1, officerFactions), 0.2f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, c * 0.25f, c * 0.5f), 10f);
			picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK_NOT_SYSTEM), 10f);
			picker.add(new BreadcrumbSpecialCreator(random, context.all), 10f);
			
			if (entity.getOrbit() != null) {
				picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 25), 10f);
			}
			
			if (!entity.hasTag(Tags.EXPIRES)) {
				picker.add(new BlueprintSpecialCreator(random), 1f);
			}
		}
		
		// debris field
		boolean debris = entity instanceof CampaignTerrainAPI &&
						((CampaignTerrainAPI)entity).getPlugin() instanceof DebrisFieldTerrainPlugin;
		if (debris) {
			picker.add(new NothingSpecialCreator(), 60f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 3, true, null, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, 10, 50, null), 6f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ORGANS, 1, 5, null), 3f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 0.2f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 10, 50), 10f);
			picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK_NOT_SYSTEM), 10f);
			picker.add(new BreadcrumbSpecialCreator(random, context.all), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 25), 10f);
		}
		
		if (Entities.STATION_MINING_REMNANT.equals(type) || Entities.STATION_MINING.equals(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 3, false, DerelictType.CIVILIAN, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, 100, 200, null), 6f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ORGANS, 5, 50, null), 3f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 3f);
			picker.add(new CargoManifestSpecialCreator(random, industryCargo, 50, 250), 30f);
			picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK), 20f);
			picker.add(new BreadcrumbSpecialCreator(random, context.all), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_MEDIUM, trapFactions, 10, 16), 10f);
		}
		
		if (Entities.STATION_RESEARCH_REMNANT.equals(type) || Entities.STATION_RESEARCH.equals(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 3, false, DerelictType.CIVILIAN, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, 100, 200, null), 6f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ORGANS, 5, 50, null), 3f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 3f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 10, 30), 10f);
			picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK), 10f);
			picker.add(new BreadcrumbSpecialCreator(random, context.all), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_MEDIUM, trapFactions, 10, 16), 20f);
		}
		
		if (Entities.ORBITAL_HABITAT_REMNANT.equals(type) || Entities.ORBITAL_HABITAT.equals(type)) {
			picker.add(new NothingSpecialCreator(), 40f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 3, false, DerelictType.CIVILIAN, recoverableShipFactions), 20f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.CREW, 100, 200, null), 20f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ORGANS, 5, 50, null), 5f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 5f);
			picker.add(new CargoManifestSpecialCreator(random, habCargo, 10, 30), 10f);
			picker.add(new SurveyDataSpecialCreator(random, SurveyDataSpecialType.AUTO_PICK), 5f);
			picker.add(new BreadcrumbSpecialCreator(random, context.all), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_MEDIUM, trapFactions, 10, 16), 10f);
		}
		
		
		List<String> weapons = Arrays.asList(Entities.WEAPONS_CACHE, Entities.WEAPONS_CACHE_HIGH, Entities.WEAPONS_CACHE_LOW, Entities.WEAPONS_CACHE_REMNANT);
		if (weapons.contains(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 1, false, DerelictType.SMALL, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 2f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 10, 30), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 8), 10f);
		}
		
		List<String> weaponsSmall = Arrays.asList(Entities.WEAPONS_CACHE_SMALL, Entities.WEAPONS_CACHE_SMALL_HIGH,
								Entities.WEAPONS_CACHE_SMALL_LOW, Entities.WEAPONS_CACHE_SMALL_REMNANT);
		if (weaponsSmall.contains(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 1, false, DerelictType.SMALL, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 10, 30), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 8), 10f);
		}		
		
		
		List<String> supplies = Arrays.asList(Entities.SUPPLY_CACHE);
		if (supplies.contains(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 1, false, DerelictType.SMALL, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 0.2f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 10, 30), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 8), 10f);
		}
		
		List<String> suppliesSmall = Arrays.asList(Entities.SUPPLY_CACHE_SMALL);
		if (suppliesSmall.contains(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 1, false, DerelictType.SMALL, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 0.2f);
			picker.add(new CargoManifestSpecialCreator(random, valuableCargo, 10, 30), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 8), 10f);
		}
		
		
		List<String> equipment = Arrays.asList(Entities.EQUIPMENT_CACHE);
		if (equipment.contains(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 1, false, DerelictType.SMALL, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 0.2f);
			picker.add(new CargoManifestSpecialCreator(random, industryCargo, 10, 30), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 8), 10f);
		}
		
		List<String> equipmentSmall = Arrays.asList(Entities.EQUIPMENT_CACHE_SMALL);
		if (equipmentSmall.contains(type)) {
			picker.add(new NothingSpecialCreator(), 30f);
			picker.add(new ShipRecoverySpecialCreator(random, 1, 1, false, DerelictType.SMALL, recoverableShipFactions), 10f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.OFFICER, 1, 15, officerFactions), 1f);
			picker.add(new SleeperPodsSpecialCreator(random, SleeperSpecialType.ADMIN, 1, 5, officerFactions), 0.2f);
			picker.add(new CargoManifestSpecialCreator(random, industryCargo, 10, 30), 10f);
			picker.add(new TransmitterTrapSpecialCreator(random, 0.5f, FleetTypes.PATROL_SMALL, trapFactions, 4, 8), 10f);
		}
		
		
		return picker.pick();
	}
	
	
	public static void assignSpecials(SectorEntityToken entity) {
		SpecialCreationContext context = new SpecialCreationContext();
		
		SpecialCreator creator = pickSpecialFor(entity, context);
		if (creator == null) return;
		
		Object specialData = creator.createSpecial(entity, context);
		if (specialData != null) {
			Misc.setSalvageSpecial(entity, specialData);
			
			if (DerelictThemeGenerator.DEBUG) {
				String id = entity.getCustomEntityType();
				if (id == null) id = entity.getClass().getSimpleName();
				System.out.println("Assigned " + specialData.getClass().getSimpleName() + " to " + id);
			}
		}
	}
	
	
	public static void assignSpecials(List<StarSystemData> systemData, SpecialCreationContext context) {
		
		context.all.clear();
		for (StarSystemData data : systemData) {
			for (AddedEntity added : data.generated) {
				context.all.add(added.entity);
			}
			for (PlanetAPI planet : data.system.getPlanets()) {
				if (planet.getMarket() != null && 
						planet.getMarket().isPlanetConditionMarketOnly() && 
						CoreCampaignPluginImpl.hasRuins(planet.getMarket())) {
					context.all.add(planet);
				}
			}
		}
		
		if (DerelictThemeGenerator.DEBUG) {
			System.out.println("\n\n\nAssigning salvage specials");
		}
		
		for (SectorEntityToken entity : context.all) {
			SpecialCreator creator = pickSpecialFor(entity, context);
			if (creator == null) continue;
			
			Object specialData = creator.createSpecial(entity, context);
			if (specialData != null) {
				Misc.setSalvageSpecial(entity, specialData);
				
				if (DerelictThemeGenerator.DEBUG) {
					String id = entity.getCustomEntityType();
					if (id == null) id = entity.getClass().getSimpleName();
					System.out.println("Assigned " + specialData.getClass().getSimpleName() + " to " + id);
				}
			}
			
			//System.out.println("" + StarSystemGenerator.random.nextLong());
		}
		
		if (DerelictThemeGenerator.DEBUG) {
			System.out.println("Finished assigning salvage specials\n\n\n\n");
		}
		
	}
	
	
	
	

	public static class NothingSpecialCreator implements SpecialCreator {
		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			return null;
		}
	}
	
	public static class ShipRecoverySpecialCreator implements SpecialCreator {
		private int min, max;
		private WeightedRandomPicker<String> factionPicker;
		private Random random;
		private boolean badCondition;
		private DerelictType type;
		public ShipRecoverySpecialCreator(Random random, int min, int max, boolean badCondition,
										  DerelictType type,
										  WeightedRandomPicker<String> factionPicker) {
			this.badCondition = badCondition;
			this.type = type;
			if (random == null) random = new Random();
			this.random = random;
			this.min = min;
			this.max = max;
			this.factionPicker = factionPicker;
		}

		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			
			if (entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
				DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) entity.getCustomPlugin();
				
				ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
				data.addShip(plugin.getData().ship.clone());
				return data;
			}
			
//			boolean debris = entity instanceof CampaignTerrainAPI && 
//				((CampaignTerrainAPI)entity).getPlugin() instanceof DebrisFieldTerrainPlugin;
			String desc = null;
			if (entity instanceof PlanetAPI) {
				desc = "found in a sealed hangar bay";
			}
			
			ShipRecoverySpecialData data = new ShipRecoverySpecialData(desc);
			int num = min + random.nextInt(max - min + 1);
			for (int i = 0; i < num; i++) {
				String factionId = factionPicker.pick();
				ShipCondition condition = DerelictShipEntityPlugin.pickDerelictCondition(random);
				if (badCondition) {
					condition = DerelictShipEntityPlugin.pickBadCondition(random);
				}
				DerelictShipData dsd = DerelictShipEntityPlugin.createRandom(factionId, type, random);
				if (dsd == null || dsd.ship == null) continue;
				
				dsd.ship.condition = condition;
				data.addShip(dsd.ship);
			}
			
			if (data.ships == null || data.ships.isEmpty()) return null;
			
			return data;
		}
		
	}
	
	
	public static class SleeperPodsSpecialCreator implements SpecialCreator {
		private SleeperSpecialType type;
		private int min, max;
		private WeightedRandomPicker<String> officerFactions;
		private Random random;
		
		public SleeperPodsSpecialCreator(Random random, SleeperSpecialType type, float min, float max, 
										 WeightedRandomPicker<String> officerFactions) {
			if (min < 1) min = 1;
			if (max < 1) max = 1;
			this.random = random;
			this.type = type;
			this.min = (int) min;
			this.max = (int) max;
			this.officerFactions = officerFactions;
		}


		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			SleeperPodsSpecialData data = new SleeperPodsSpecialData(type, null);
			
			if (type == SleeperSpecialType.OFFICER) {
				String factionId = officerFactions.pick();
				FactionAPI faction = Global.getSector().getFaction(factionId);
				int level = min + random.nextInt(max - min + 1);
				
				SkillPickPreference pref = SkillPickPreference.EITHER;
				float f = random.nextFloat();
				if (f < 0.25f) {
					pref = SkillPickPreference.CARRIER;
				} else if (f < 0.5f) {
					pref = SkillPickPreference.NON_CARRIER;
				}
				
				PersonAPI officer = OfficerManagerEvent.createOfficer(faction, level, true, pref, true, random);
				data.officer = officer;
				data.min = 1;
				data.max = 1;
			} else if (type == SleeperSpecialType.ADMIN) {
				//System.out.println("ADMIN: " + entity.getContainingLocation().getName() + ", " + entity.getName());
				String factionId = officerFactions.pick();
				FactionAPI faction = Global.getSector().getFaction(factionId);
				
				WeightedRandomPicker<Integer> tierPicker = new WeightedRandomPicker<Integer>(random);
				//tierPicker.add(0, 0);
				tierPicker.add(1, 50);
				tierPicker.add(2, 50);
				
				int tier = tierPicker.pick();
				
				PersonAPI officer = OfficerManagerEvent.createAdmin(faction, tier, random);
				data.officer = officer;
				data.min = 1;
				data.max = 1;
			} else {
				data.min = min;
				data.max = max;
			}
			
			return data;
		}
		
	}
	
	
	public static class SurveyDataSpecialCreator implements SpecialCreator {
		private Random random;
		private SurveyDataSpecialType type;
		
		public SurveyDataSpecialCreator(Random random, SurveyDataSpecialType type) {
			this.random = random;
			this.type = type;
		}


		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			SurveyDataSpecialData data = new SurveyDataSpecialData(type);
			return data;
		}
		
	}
	
	public static class BlueprintSpecialCreator implements SpecialCreator {
		private Random random;
		
		public BlueprintSpecialCreator(Random random) {
			this.random = random;
		}
		
		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			return new BlueprintSpecialData();
		}
	}
	
	
	public static class CargoManifestSpecialCreator implements SpecialCreator {
		private Random random;
		private SurveyDataSpecialType type;
		private WeightedRandomPicker<String> cargoPicker;
		private int min;
		private int max;
		
		public CargoManifestSpecialCreator(Random random, WeightedRandomPicker<String> cargoPicker, float min, float max) {
			if (min < 1) min = 1;
			if (max < 1) max = 1;
			this.random = random;
			this.cargoPicker = cargoPicker;
			this.min = (int) min;
			this.max = (int) max;
		}
		
		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			CargoManifestSpecialData data = new CargoManifestSpecialData(cargoPicker.pick(), min, max);
			return data;
		}
	}
	
	public static class TransmitterTrapSpecialCreator implements SpecialCreator {
		private Random random;
		private WeightedRandomPicker<String> factionPicker;
		private int minPts;
		private int maxPts;
		private final float chance;
		private final String fleetType;
		
		public TransmitterTrapSpecialCreator(Random random, float chance, String fleetType, 
									WeightedRandomPicker<String> factionPicker, int min, int max) {
			this.random = random;
			this.chance = chance;
			this.fleetType = fleetType;
			this.factionPicker = factionPicker;
			this.minPts = min;
			this.maxPts = max;
		}
		
		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			
			
			TransmitterTrapSpecialData data = new TransmitterTrapSpecialData();
			data.prob = chance;
			String factionId = factionPicker.pick();

			data.nearbyFleetFaction = factionId;
			data.useAllFleetsInRange = true;
			
			if (fleetType != null) {
				int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);
				combatPoints *= 5;
				
				FleetParamsV3 params = new FleetParamsV3(
						null,
						entity.getLocationInHyperspace(),
						factionId,
						null, 
						fleetType,
						combatPoints, // combatPts
						0f, // freighterPts 
						0f, // tankerPts
						0f, // transportPts
						0f, // linerPts
						0f, // utilityPts
						0f // qualityMod
						);				
				data.params = params;
			}
			
			return data;
		}
	}
	
	
	public static class BreadcrumbSpecialCreator implements SpecialCreator {
		private Random random;
		private List<SectorEntityToken> all;
		
		public BreadcrumbSpecialCreator(Random random, List<SectorEntityToken> all) {
			this.random = random;
			this.all = all;
		}
		
		public Object createSpecial(SectorEntityToken entity, SpecialCreationContext context) {
			WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(random);
			
//			boolean debris = entity instanceof CampaignTerrainAPI &&
//						((CampaignTerrainAPI)entity).getPlugin() instanceof DebrisFieldTerrainPlugin;
			
			if (all != null) {
				for (SectorEntityToken other : all) {
					if (other == entity) continue;
					// only breadcrumb to larger ships
					if (!isLargeShipOrNonShip(other)) continue;
					picker.add(other);
				}
			}
			
			List<StarSystemAPI> systems = Misc.getNearbyStarSystems(entity, 10f);
			for (StarSystemAPI system : systems) {
				for (SectorEntityToken other : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
					if (!other.hasSensorProfile() && !other.isDiscoverable()) continue;
					if (other == entity) continue;
					// only breadcrumb to larger ships
					if (!isLargeShipOrNonShip(other)) continue;
					picker.add(other);
				}
			}
			
			
			SectorEntityToken target = picker.pick();
			if (target == null) return null;
			
			BreadcrumbSpecialData data = new BreadcrumbSpecialData(target.getId());
			return data;
		}
		
		public static boolean isLargeShipOrNonShip(SectorEntityToken other) {
			if (other.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
				DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) other.getCustomPlugin();
				ShipVariantAPI variant = dsep.getData().ship.variant;
				if (variant == null && dsep.getData().ship.variantId != null) {
					variant = Global.getSettings().getVariant(dsep.getData().ship.variantId);
				}
				if (variant != null) {
					if (variant.getHullSize() == HullSize.FRIGATE) return false;
					if (variant.getHullSize() == HullSize.DESTROYER) return false;
					if (variant.getHullSize() == HullSize.CRUISER &&
							variant.getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN)) return false;
				}
			}
			return true;
		}
	}
	
}





