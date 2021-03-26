package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

@SuppressWarnings("unchecked")
public class DegradedShields extends BaseHullMod {

	public static float SHIELD_PERCENT = 15f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		stats.getShieldDamageTakenMult().modifyPercent(id, SHIELD_PERCENT * effect);
		
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		if (index == 0) return "" + (int) Math.round(SHIELD_PERCENT * effect) + "%";
		if (index >= 1) return CompromisedStructure.getCostDescParam(index, 1);
		return null;
	}


}
