package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AndradaMods extends BaseHullMod {

	public static float CASUALTIES_PERCENT = 10f;
	public static float FLUX_PERCENT = 5f;
	public static float REPAIR_PERCENT = 25f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCrewLossMult().modifyPercent(id, CASUALTIES_PERCENT);
		
		stats.getFluxDissipation().modifyPercent(id, -FLUX_PERCENT);
		
		stats.getCombatEngineRepairTimeMult().modifyPercent(id, REPAIR_PERCENT);
		stats.getCombatWeaponRepairTimeMult().modifyPercent(id, REPAIR_PERCENT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) CASUALTIES_PERCENT + "%";
		if (index == 1) return "" + (int) FLUX_PERCENT + "%";
		if (index == 2) return "" + (int) REPAIR_PERCENT + "%";
		return null;
	}


}








