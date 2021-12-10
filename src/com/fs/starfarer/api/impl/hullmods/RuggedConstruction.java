package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class RuggedConstruction extends BaseHullMod {

	public static float DMOD_EFFECT_MULT = 0.5f;
	public static float DMOD_AVOID_CHANCE = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
		//stats.getDynamic().getMod(Stats.DMOD_AVOID_PROB_MOD).modifyFlat(id, DMOD_AVOID_CHANCE * 0.01f);
		stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, (1f - DMOD_AVOID_CHANCE * 0.01f));
		
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		
//		stats.getMinArmorFraction().modifyFlat(id, 0.1f);
//		stats.getBeamDamageTakenMult().modifyMult(id, 0.5f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - DMOD_EFFECT_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) DMOD_AVOID_CHANCE + "%";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}








