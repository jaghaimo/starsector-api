package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ErraticInjector extends BaseHullMod {

	public static float FUEL_PERCENT = 50;
	public static float ZERO_FLUX_PENALTY = 10;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		stats.getFuelUseMod().modifyPercent(id, FUEL_PERCENT * effect);
		
		stats.getZeroFluxSpeedBoost().modifyFlat(id, -ZERO_FLUX_PENALTY * effect);
		
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		if (index == 0) return "" + (int)Math.round(FUEL_PERCENT * effect) + "%";
		if (index == 1) return "" + (int)Math.round(ZERO_FLUX_PENALTY * effect) + "";
		if (index >= 2) return CompromisedStructure.getCostDescParam(index, 1);
		return null;
	}


}
