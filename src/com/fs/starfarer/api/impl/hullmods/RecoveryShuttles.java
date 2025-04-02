package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class RecoveryShuttles extends BaseHullMod {

	public static float CREW_LOSS_MULT = 0.25f;
	
	public static float SMOD_CREW_LOSS_MULT = 0.05f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		float mult = CREW_LOSS_MULT;
		if (sMod) mult = SMOD_CREW_LOSS_MULT; 
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, mult);
	}
		
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - SMOD_CREW_LOSS_MULT) * 100f) + "%";
		return null;
	}
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - CREW_LOSS_MULT) * 100f) + "%";
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		if (Misc.isAutomated(ship.getVariant())) return false;
		
		if (ship.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR)) return true;
		
		//int bays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
		int bays = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
//		if (ship != null && ship.getVariant().getHullSpec().getBuiltInWings().size() >= bays) {
//			return false;
//		}
		return ship != null && bays > 0; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && Misc.isAutomated(ship.getVariant())) {
			return "Can not be installed on automated ships";
		}
		return "Ship does not have fighter bays";
	}
}




