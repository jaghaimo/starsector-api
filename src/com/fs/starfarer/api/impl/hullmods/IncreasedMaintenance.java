package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class IncreasedMaintenance extends BaseHullMod {

	public static float CREW_PERCENT = 30;
	public static float SUPPLY_USE_MULT = 1.30f;
	public static float MAX_CR_PENALTY = 0.05f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		//stats.getSuppliesPerMonth().modifyMult(id, 1f + (SUPPLY_USE_MULT - 1f) * effect);
		stats.getSuppliesPerMonth().modifyPercent(id, Math.round((SUPPLY_USE_MULT - 1f) * effect * 100f));
		stats.getMinCrewMod().modifyPercent(id, CREW_PERCENT * effect);
		stats.getMaxCombatReadiness().modifyFlat(id, -Math.round(MAX_CR_PENALTY * effect * 100f) * 0.01f, "Increased Maintenance");
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		if (index == 0) return "" + (int)Math.round((1f + (SUPPLY_USE_MULT - 1f) * effect - 1f) * 100f) + "%";
		if (index == 1) return "" + (int)Math.round(CREW_PERCENT * effect) + "%";
		if (index == 2) return "" + Math.round(MAX_CR_PENALTY * 100f * effect) + "%";
		if (index >= 3) return CompromisedStructure.getCostDescParam(index, 3);
		return null;
	}


}
