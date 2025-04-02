package com.fs.starfarer.api.impl.campaign.enc;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData.UniqueEncounterData;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl.AbyssalEPData;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AbyssalRogueStellarObjectDireHintsEPEC extends AbyssalRogueStellarObjectEPEC {

	public static enum AbyssalDireHintType {
		MINING_OP,
		GAS_GIANT_TURBULENCE,
		BLACK_HOLE_READINGS,
		GHOST_SHIP,
	}
	
	/**
	 * Not related to sensor ghosts OR IS IT
	 * 
	 * @author Alex
	 *
	 * Copyright 2023 Fractal Softworks, LLC
	 */
	public static enum GhostShipType {
		GS_AI_CORES,
		GS_TRI_TACHYON,
		GS_ZGR_MERC,
		GS_ACADEMY,
		GS_ADVENTURERS,
		GS_OPIS,
		GS_PIRATES,
		GS_VAMBRACE;
//		GS_VAMBRACE2;
		
		public String getTimeoutKey() {
			return "$" + name() + "_timeout";
		}
	}

	
	
	public static WeightedRandomPicker<AbyssalDireHintType> DIRE_HINT_TYPES = new WeightedRandomPicker<AbyssalDireHintType>();
	static {
		DIRE_HINT_TYPES.add(AbyssalDireHintType.MINING_OP, 10f);
		DIRE_HINT_TYPES.add(AbyssalDireHintType.GAS_GIANT_TURBULENCE, 10f);
		DIRE_HINT_TYPES.add(AbyssalDireHintType.BLACK_HOLE_READINGS, 10f);
		DIRE_HINT_TYPES.add(AbyssalDireHintType.GHOST_SHIP, 20f);
	}
	
	
	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		return AbyssalFrequencies.getAbyssalRogueStellarObjectDireHintsFrequency(manager, point);
	}
	
	
	@Override
	protected void addSpecials(StarSystemAPI system, EncounterManager manager, EncounterPoint point, AbyssalEPData data) {
		WeightedRandomPicker<AbyssalDireHintType> picker = new WeightedRandomPicker<AbyssalDireHintType>(data.random);
		picker.addAll(DIRE_HINT_TYPES);
		
		UniqueEncounterData ueData = SharedData.getData().getUniqueEncounterData();
		WeightedRandomPicker<GhostShipType> ghostShipPicker = new WeightedRandomPicker<GhostShipType>(data.random);
		for (GhostShipType type : EnumSet.allOf(GhostShipType.class)) {
			if (ueData.wasInteractedWith(type.name())) {
				continue;
			}
			if (Global.getSector().getMemoryWithoutUpdate().contains(type.getTimeoutKey())) {
				continue;
			}
			ghostShipPicker.add(type);
		}
		
		if (ghostShipPicker.isEmpty()) {
			picker.remove(AbyssalDireHintType.GHOST_SHIP);
		}
		
		if (DebugFlags.ABYSSAL_GHOST_SHIPS_DEBUG) {
			picker.add(AbyssalDireHintType.GHOST_SHIP, 1000000000f);
		}
		
		boolean done = false;
		do {
			AbyssalDireHintType type = picker.pickAndRemove();
			
//			type = AbyssalDireHintType.MINING_OP;
//			type = AbyssalDireHintType.GAS_GIANT_TURBULENCE;
//			type = AbyssalDireHintType.BLACK_HOLE_READINGS;
			
			if (type == AbyssalDireHintType.BLACK_HOLE_READINGS) {
				done = addBlackHoleReadings(system, point, data);
			} else if (type == AbyssalDireHintType.GAS_GIANT_TURBULENCE) {
				done = addGasGiantTurbulence(system, point, data);
			} else if (type == AbyssalDireHintType.MINING_OP) {
				done = addMiningOp(system, point, data);
			} else if (type == AbyssalDireHintType.GHOST_SHIP) {
				GhostShipType ghostType = ghostShipPicker.pickAndRemove();
				if (ghostType != null) {
					done = addGhostShip(system, ghostType, point, data);
				}
			}
		} while (!picker.isEmpty() && !done);
	}
	

	protected boolean addBlackHoleReadings(StarSystemAPI system, EncounterPoint point, AbyssalEPData data) {
		PlanetAPI blackHole = null;
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isBlackHole()) {
				blackHole = planet;
				break;
			}
		}
		if (blackHole == null) return false;
		
		blackHole.getMemoryWithoutUpdate().set("$abyssalBlackHoleReadings", true);
		
		return true;
	}
	
	protected boolean addGasGiantTurbulence(StarSystemAPI system, EncounterPoint point, AbyssalEPData data) {
		WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(data.random);
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isGasGiant()) {
				picker.add(planet);
			}
		}
		PlanetAPI giant = picker.pick();
		if (giant == null) return false;
		
		giant.getMemoryWithoutUpdate().set("$abyssalGasGiantTurbulence", true);
		
		return true;
	}
	
	protected boolean addMiningOp(StarSystemAPI system, EncounterPoint point, AbyssalEPData data) {
		WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(data.random);
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isGasGiant()) continue;
			if (planet.isStar()) continue;
			picker.add(planet);
		}
		PlanetAPI giant = picker.pick();
		if (giant == null) return false;
		
		giant.getMemoryWithoutUpdate().set("$abyssalPlanetoidMiningOp", true);
		
		return true;
	}
	
	protected boolean addGhostShip(StarSystemAPI system, GhostShipType type, EncounterPoint point, AbyssalEPData data) {
		WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(data.random);
		for (PlanetAPI planet : system.getPlanets()) {
			picker.add(planet);
		}
		PlanetAPI planet = picker.pick();
		if (planet == null) return false;
		
		
		//ship.getMemoryWithoutUpdate().set("$fromGhost", true);
		if (DebugFlags.ABYSSAL_GHOST_SHIPS_DEBUG) {
			type = GhostShipType.GS_AI_CORES;
			type = GhostShipType.GS_TRI_TACHYON;
			type = GhostShipType.GS_ADVENTURERS;
			type = GhostShipType.GS_OPIS;
			type = GhostShipType.GS_PIRATES;
			type = GhostShipType.GS_ZGR_MERC;
			type = GhostShipType.GS_ACADEMY;
			type = GhostShipType.GS_VAMBRACE;
			//type = GhostShipType.GS_VAMBRACE2;
		}
		
		if (type == GhostShipType.GS_AI_CORES) {
			String variantId = pickVariant(Factions.INDEPENDENT, ShipRoles.COMBAT_LARGE, data.random);
			if (variantId == null) return false;
			addShipAroundPlanet(planet, variantId, ShipCondition.GOOD, type.name(), data.random);
		} else if (type == GhostShipType.GS_TRI_TACHYON) {
			addShipAroundPlanet(planet, "apogee_Balanced", ShipCondition.GOOD, type.name(), data.random);
		} else if (type == GhostShipType.GS_ADVENTURERS) {
			String variantId = pickVariant(Factions.MERCENARY, ShipRoles.COMBAT_MEDIUM, data.random);
			if (variantId == null) return false;
			addShipAroundPlanet(planet, variantId, ShipCondition.GOOD, type.name(), data.random);
		} else if (type == GhostShipType.GS_OPIS) {
			addShipAroundPlanet(planet, "starliner_Standard", ShipCondition.GOOD, type.name(), data.random);
		} else if (type == GhostShipType.GS_PIRATES) {
			String variantId = pickVariant(Factions.PIRATES, ShipRoles.COMBAT_LARGE, data.random);
			if (variantId == null) return false;
			addShipAroundPlanet(planet, variantId, ShipCondition.BATTERED, type.name(), data.random);
		} else if (type == GhostShipType.GS_ACADEMY) {
			addShipAroundPlanet(planet, "apogee_Balanced", ShipCondition.BATTERED, type.name(), "GAS Itzamna", data.random);
		} else if (type == GhostShipType.GS_ZGR_MERC) {
			String variantId = pickVariant(Factions.MERCENARY, ShipRoles.COMBAT_MEDIUM, data.random);
			if (variantId == null) return false;
			addShipAroundPlanet(planet, variantId, ShipCondition.WRECKED, type.name(), data.random);
		} else if (type == GhostShipType.GS_VAMBRACE) {
			SectorEntityToken vambrace = system.addCustomEntity("derelict_vambrace", 
					"Derelict Structure", "derelict_vambrace", Factions.NEUTRAL);
			float orbitRadius = planet.getRadius() + data.random.nextFloat() * 100f;
			float orbitDays = orbitRadius / (10f + data.random.nextFloat() * 5f);
			vambrace.setCircularOrbit(planet, data.random.nextFloat() * 360f, orbitRadius, orbitDays);
		}
		/* else if (type == GhostShipType.GS_VAMBRACE2) {
			SectorEntityToken vambrace = system.addCustomEntity("derelict_vambrace","Derelict Structure", "derelict_vambrace2", "neutral");
			vambrace.setCircularOrbit(planet, 300f, planet.getRadius() + 100f, 100f);
		}*/
		Global.getSector().getMemoryWithoutUpdate().set(type.getTimeoutKey(), true, 90f);
		
		return true;
	}
	
	public void addShipAroundPlanet(SectorEntityToken planet, String variantId, ShipCondition condition, 
			String gsType, Random random) {
		this.addShipAroundPlanet(planet, variantId, condition, gsType, null, random);
	}
	public void addShipAroundPlanet(SectorEntityToken planet, String variantId, ShipCondition condition, 
									String gsType, String shipName, Random random) {
		PerShipData psd = new PerShipData(variantId, condition, 0f);
		if (shipName != null) {
			psd.shipName = shipName;
			psd.nameAlwaysKnown = true;
		}
		DerelictShipData params = new DerelictShipData(psd, true);

		CustomCampaignEntityAPI ship = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
				random, planet.getContainingLocation(), Entities.WRECK, Factions.NEUTRAL, params);
		//SalvageSpecialAssigner.assignSpecials(ship, false, data.random);
		//ship.addTag(Tags.EXPIRES);
		
		ship.setDiscoverable(true);
		float orbitRadius = planet.getRadius() + 200f + random.nextFloat() * 100f;
		float orbitDays = orbitRadius / (10f + random.nextFloat() * 5f);
		ship.setCircularOrbit(planet, random.nextFloat() * 360f, orbitRadius, orbitDays);
		
		ship.setLocation(planet.getLocation().x, planet.getLocation().y);
		ship.getVelocity().set(planet.getVelocity());
		
		ship.getMemoryWithoutUpdate().set("$gsType", gsType);
	}
	
	public String pickVariant(String factionId, String shipRole, Random random) {
		ShipPickParams params = new ShipPickParams(ShipPickMode.ALL);
		List<ShipRolePick> picks = Global.getSector().getFaction(factionId).pickShip(shipRole, params, null, random);
		if (picks == null || picks.isEmpty()) return null;
		return picks.get(0).variantId;
		
	}
}

















