package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FragileSubsystems extends BaseHullMod {

	public static final float PEAK_PENALTY_PERCENT = 30f;
	public static final float DEGRADE_INCREASE_PERCENT = 30f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		stats.getPeakCRDuration().modifyMult(id, 1f - (PEAK_PENALTY_PERCENT * effect) / 100f);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT * effect);
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		
		if (index == 0) return "" + (int) Math.round(PEAK_PENALTY_PERCENT * effect) + "%";
		if (index == 1) return "" + (int) Math.round(DEGRADE_INCREASE_PERCENT * effect) + "%";
		if (index >= 2) return CompromisedStructure.getCostDescParam(index, 2); 
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && (ship.getHullSpec().getNoCRLossTime() < 10000 || ship.getHullSpec().getCRLossPerSecond(ship.getMutableStats()) > 0); 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship does not suffer from CR degradation";
	}
}




