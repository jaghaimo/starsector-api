package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class CompromisedStructure extends BaseHullMod {
	public static float DEPLOYMENT_COST_MULT = 0.8f;
	
	public static void modifyCost(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesToRecover().modifyMult(id, DEPLOYMENT_COST_MULT);
		
		float effect = stats.getDynamic().getValue(Stats.DMOD_REDUCE_MAINTENANCE, 0);
		if (effect > 0) {
			stats.getSuppliesPerMonth().modifyMult(id, DEPLOYMENT_COST_MULT);
		}
	}
	public static String getCostDescParam(int index, int startIndex) {
		if (index - startIndex == 0) {
			return "" + (int) Math.round((1f - DEPLOYMENT_COST_MULT) * 100f) + "%";
		}
		return null;
	}
	
	public static float ARMOR_PENALTY_MULT = 0.8f;
	public static float HULL_PENALTY_MULT = 0.8f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		float armorMult = ARMOR_PENALTY_MULT + (1f - ARMOR_PENALTY_MULT) * (1f - effect);
		float hullMult = HULL_PENALTY_MULT + (1f - HULL_PENALTY_MULT) * (1f - effect);
		
		stats.getArmorBonus().modifyMult(id, armorMult);
		stats.getHullBonus().modifyMult(id, hullMult);
		modifyCost(hullSize, stats, id);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		float armorMult = ARMOR_PENALTY_MULT + (1f - ARMOR_PENALTY_MULT) * (1f - effect);
		float hullMult = HULL_PENALTY_MULT + (1f - HULL_PENALTY_MULT) * (1f - effect);
		
		if (index == 0) return "" + (int) Math.round((1f - armorMult) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round((1f - hullMult) * 100f) + "%";
		if (index >= 2) return getCostDescParam(index, 2); 
		return null;
	}
	
	
}




