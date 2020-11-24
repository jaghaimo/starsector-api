package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class CompromisedStorage extends BaseHullMod {

public static final float CAPACITY_PENALTY_PERCENT = 30f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		stats.getMaxCrewMod().modifyMult(id, 1f - (CAPACITY_PENALTY_PERCENT * effect) / 100f);
		stats.getFuelMod().modifyMult(id, 1f - (CAPACITY_PENALTY_PERCENT * effect) / 100f);
		stats.getCargoMod().modifyMult(id, 1f - (CAPACITY_PENALTY_PERCENT * effect) / 100f);
		
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		if (index == 0) return "" + (int) Math.round(CAPACITY_PENALTY_PERCENT * effect) + "%";
		if (index >= 1) return CompromisedStructure.getCostDescParam(index, 1); 
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		return true; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}
}




