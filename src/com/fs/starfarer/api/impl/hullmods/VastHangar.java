package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class VastHangar extends BaseHullMod {

	//public static float SUPPLY_COST_REDUCTION = 25;
	public static int CONVERTED_HANGAR_BONUS = 1;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getSuppliesPerMonth().modifyMult(id, 1f - SUPPLY_COST_REDUCTION * 0.01f);
		
		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_MOD).modifyFlat(id, CONVERTED_HANGAR_BONUS);
		
//		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_PERFORMANCE_PENALTY).modifyFlat(id, 1f);
//		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_COST_INCREASE).modifyFlat(id, 1f);

		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_CREW_INCREASE).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REARM_INCREASE).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_DP_INCREASE).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REFIT_PENALTY).modifyFlat(id, 1f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int) SUPPLY_COST_REDUCTION + "%";
		if (index == 0) return "" + (int) CONVERTED_HANGAR_BONUS + "";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}








