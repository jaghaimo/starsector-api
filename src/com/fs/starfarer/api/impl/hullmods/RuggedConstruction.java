package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class RuggedConstruction extends BaseHullMod {

	public static float DEPLOYMENT_COST_MULT = 0.5f;
	
//	public static float MIN_HULL = 30f;
//	public static float MAX_HULL = 40f;
//	
//	public static float MIN_CR = 30f;
//	public static float MAX_CR = 40f;
//	
//	public static float CR_LOSS_WHEN_DISABLED = 0.1f;
//	public static float REPAIR_FRACTION = 0.5f;
	
	public static float DMOD_EFFECT_MULT = 0.5f;
	public static float DMOD_AVOID_CHANCE = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
		//stats.getDynamic().getMod(Stats.DMOD_AVOID_PROB_MOD).modifyFlat(id, DMOD_AVOID_CHANCE * 0.01f);
		stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, (1f - DMOD_AVOID_CHANCE * 0.01f));
		
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		
		//stats.getDynamic().getStat(Stats.CR_LOSS_WHEN_DISABLED_MULT).modifyMult(id, CR_LOSS_WHEN_DISABLED);
		//stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, REPAIR_FRACTION + 0.45f);
		//stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, REPAIR_FRACTION);
		
		
//		stats.getDynamic().getMod(Stats.RECOVERED_HULL_MIN).modifyFlat(id, MIN_HULL * 0.01f);
//		stats.getDynamic().getMod(Stats.RECOVERED_HULL_MAX).modifyFlat(id, MAX_HULL * 0.01f);
//		stats.getDynamic().getMod(Stats.RECOVERED_CR_MIN).modifyFlat(id, MIN_CR * 0.01f);
//		stats.getDynamic().getMod(Stats.RECOVERED_CR_MAX).modifyFlat(id, MAX_CR * 0.01f);
		
//		stats.getMinArmorFraction().modifyFlat(id, 0.1f);
//		stats.getBeamDamageTakenMult().modifyMult(id, 0.5f);
		
		stats.getSuppliesToRecover().modifyMult(id, DEPLOYMENT_COST_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - DMOD_EFFECT_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) DMOD_AVOID_CHANCE + "%";
		if (index == 2) return "" + (int) Math.round((1f - DEPLOYMENT_COST_MULT) * 100f) + "%";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true; // probably intended even though it doesn't, actually -am
	}

}








