package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FaultyAutomatedSystems extends BaseHullMod {

	public static float CREW_PERCENT = 100f;
	public static float MAX_CR_PENALTY = 0.05f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		stats.getMinCrewMod().modifyPercent(id, CREW_PERCENT * effect);
		stats.getMaxCombatReadiness().modifyFlat(id, -Math.round(MAX_CR_PENALTY * effect * 100f) * 0.01f, "Faulty Automated Systems");
		
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		if (index == 0) return "" + (int) Math.round(CREW_PERCENT * effect) + "%";
		if (index == 1) return "" + Math.round(MAX_CR_PENALTY * 100f * effect) + "%";
		if (index >= 2) return CompromisedStructure.getCostDescParam(index, 1);
		return null;
	}


}
