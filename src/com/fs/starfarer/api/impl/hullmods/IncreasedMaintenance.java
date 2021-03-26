package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class IncreasedMaintenance extends BaseHullMod {

	private static final float CREW_PERCENT = 50;
	private static final float SUPPLY_USE_MULT = 1.5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		//stats.getSuppliesPerMonth().modifyMult(id, 1f + (SUPPLY_USE_MULT - 1f) * effect);
		stats.getSuppliesPerMonth().modifyPercent(id, Math.round((SUPPLY_USE_MULT - 1f) * effect * 100f));
		stats.getMinCrewMod().modifyPercent(id, CREW_PERCENT * effect);
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		if (index == 0) return "" + (int)((1f + (SUPPLY_USE_MULT - 1f) * effect - 1f) * 100f) + "%";
		if (index == 1) return "" + (int)Math.round(CREW_PERCENT * effect) + "%";
		if (index >= 2) return CompromisedStructure.getCostDescParam(index, 2);
		return null;
	}


}
