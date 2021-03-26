package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class AdvancedGroundSupport extends BaseHullMod {

	public static final float GROUND_BONUS = 200;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, GROUND_BONUS);
		//stats.getDynamic().getMod(Stats.FLEET_BOMBARD_COST_REDUCTION).modifyFlat(id, GROUND_BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) GROUND_BONUS;
		return null;
	}
}




