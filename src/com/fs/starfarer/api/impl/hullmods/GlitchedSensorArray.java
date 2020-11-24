package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class GlitchedSensorArray extends BaseHullMod {
	public static final float RANGE_MULT = 0.85f;
	public static final float SENSOR_MULT = 0.5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		float rangeMult = RANGE_MULT + (1f - RANGE_MULT) * (1f - effect);
		float sensorMult = SENSOR_MULT + (1f - SENSOR_MULT) * (1f - effect);
		
		
		stats.getBallisticWeaponRangeBonus().modifyMult(id, rangeMult);
		stats.getEnergyWeaponRangeBonus().modifyMult(id, rangeMult);
		stats.getSensorStrength().modifyMult(id, sensorMult);
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		float rangeMult = RANGE_MULT + (1f - RANGE_MULT) * (1f - effect);
		float sensorMult = SENSOR_MULT + (1f - SENSOR_MULT) * (1f - effect);
		
		if (index == 0) return "" + (int) Math.round((1f - rangeMult) * 100f) + "%";
		//if (index == 1) return "50%";
		if (index == 1) return "" + (int) Math.round((1f - sensorMult) * 100f) + "%";
		if (index >= 2) return CompromisedStructure.getCostDescParam(index, 2); 
		return null;
	}
	
	
}




