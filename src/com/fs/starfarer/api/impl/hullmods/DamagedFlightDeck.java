package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class DamagedFlightDeck extends BaseHullMod {
	public static final float REFIT_TIME_PERCENT = 30f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_TIME_PERCENT * effect);
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		if (index == 0) return "" + (int) Math.round(REFIT_TIME_PERCENT * effect) + "%";
		if (index >= 1) return CompromisedStructure.getCostDescParam(index, 1); 
		return null;
	}
	
	
}




