package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class DegradedLifeSupport extends BaseHullMod {

	public static float MAX_CREW_MULT = 0.5f;
	public static float MAX_CR_PENALTY = 0.05f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		float crewMult = MAX_CREW_MULT + (1f - MAX_CREW_MULT) * (1f - effect);
		
		stats.getMaxCrewMod().modifyMult(id, crewMult);
		stats.getMaxCombatReadiness().modifyFlat(id, -Math.round(MAX_CR_PENALTY * effect * 100f) * 0.01f, "Degraded Life Support");
		
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		float crewMult = MAX_CREW_MULT + (1f - MAX_CREW_MULT) * (1f - effect);
		
		if (index == 0) return "" + (int)Math.round((1f - crewMult) * 100f) + "%";
		if (index == 1) return "" + Math.round(MAX_CR_PENALTY * 100f * effect) + "%";
		if (index >= 2) return CompromisedStructure.getCostDescParam(index, 1);
		return null;
	}


}
