package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class DriveFieldStabilizer extends BaseHullMod {

	public static final float BURN_BONUS = 1;
	public static final float SENSOR_PROFILE = 200f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.FLEET_BURN_BONUS).modifyFlat(id, BURN_BONUS);
		stats.getSensorProfile().modifyFlat(id, SENSOR_PROFILE);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BURN_BONUS;
		if (index == 1) return "" + (int) SENSOR_PROFILE;
		return null;
	}
}




