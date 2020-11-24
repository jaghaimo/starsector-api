package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class EfficiencyOverhaul extends BaseLogisticsHullMod {
	public static final float MAINTENANCE_MULT = 0.8f;
	
	public static final float REPAIR_RATE_BONUS = 50f;
	public static final float CR_RECOVERY_BONUS = 50f;
	public static final float REPAIR_BONUS = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, MAINTENANCE_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, MAINTENANCE_MULT);
		stats.getFuelUseMod().modifyMult(id, MAINTENANCE_MULT);
		
		stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, CR_RECOVERY_BONUS);
		stats.getRepairRatePercentPerDay().modifyPercent(id, REPAIR_RATE_BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round((1f - MAINTENANCE_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round(CR_RECOVERY_BONUS) + "%";
		return null;
	}

	
}







