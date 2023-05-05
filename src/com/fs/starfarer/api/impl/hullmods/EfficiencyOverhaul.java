package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class EfficiencyOverhaul extends BaseLogisticsHullMod {
	public static float MAINTENANCE_MULT = 0.8f;
	
	public static float REPAIR_RATE_BONUS = 50f;
	public static float CR_RECOVERY_BONUS = 50f;
	public static float REPAIR_BONUS = 50f;
	
	public static float SMOD_MODIFIER = 0.1f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		boolean sMod = isSMod(stats);
		
		stats.getMinCrewMod().modifyMult(id, MAINTENANCE_MULT - (sMod ? SMOD_MODIFIER : 0));
		stats.getSuppliesPerMonth().modifyMult(id, MAINTENANCE_MULT - (sMod ? SMOD_MODIFIER : 0));
		stats.getFuelUseMod().modifyMult(id, MAINTENANCE_MULT - (sMod ? SMOD_MODIFIER : 0));
		
		stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, CR_RECOVERY_BONUS);
		stats.getRepairRatePercentPerDay().modifyPercent(id, REPAIR_RATE_BONUS);
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round(SMOD_MODIFIER * 100f) + "%";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round((1f - MAINTENANCE_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round(CR_RECOVERY_BONUS) + "%";
		return null;
	}

	
}







