package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HighEfficiencyDriveField extends BaseLogisticsHullMod {

	public static final float REDUCTION = 0.333333f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFuelUseMod().modifyMult(id, 1f - REDUCTION);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(REDUCTION * 100f) + "%";
		return null;
	}
}




