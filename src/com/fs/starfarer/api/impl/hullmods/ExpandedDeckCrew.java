package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ExpandedDeckCrew extends BaseHullMod {

	public static final float RATE_DECREASE_MODIFIER = 10f;
	public static final float RATE_INCREASE_MODIFIER = 20f;
	
	public static final float CREW_PER_DECK = 20f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - RATE_DECREASE_MODIFIER / 100f);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, RATE_INCREASE_MODIFIER);
		
		int crew = (int) (stats.getNumFighterBays().getBaseValue() * CREW_PER_DECK);
		stats.getMinCrewMod().modifyFlat(id, crew);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) RATE_DECREASE_MODIFIER + "%";
		if (index == 1) return "" + (int) RATE_INCREASE_MODIFIER + "%";
		if (index == 2) return "" + (int) CREW_PER_DECK + "";
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		int baysModified = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
		if (baysModified <= 0) return false; // only count removed bays, not added bays for this
		
		int bays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
//		if (ship != null && ship.getVariant().getHullSpec().getBuiltInWings().size() >= bays) {
//			return false;
//		}
		return ship != null && bays > 0; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship does not have standard fighter bays";
	}
}




