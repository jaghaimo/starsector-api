package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;

public class FlightControl extends BaseHullMod {
	
//	public static float ENGAGEMENT_REDUCTION = 0.4f;
//	public static float REARM_TIME_MULT = 0.1f;
//	
//	
//	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//		
//		// doesn't work
//		//stats.getDynamic().getStat(Stats.FIGHTER_REARM_TIME_MULT).modifyMult(id, REARM_TIME_MULT);
//		
//		
//		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
//		
//		stats.getFighterWingRange().modifyMult(id, 1f - ENGAGEMENT_REDUCTION * effect);
//		CompromisedStructure.modifyCost(hullSize, stats, id);
//	}
//	
//	@Override
//	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
//		super.applyEffectsToFighterSpawnedByShip(fighter, ship, id);
//	}
//
//
//
//	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
//		float effect = 1f;
//		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
//		
//		if (index == 0) return "" + (int) Math.round(ENGAGEMENT_REDUCTION * 100f * effect) + "%";
//		if (index >= 1) return CompromisedStructure.getCostDescParam(index, 1); 
//		return null;
//	}
}




