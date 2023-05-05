package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class DefensiveTargetingArray extends BaseHullMod {

	public static float PD_DAMAGE_BONUS = 50f;
	public static float SMOD_RANGE_BONUS = 100f;
	

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterWingRange().modifyMult(id, 0f);
	}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		fighter.getMutableStats().getDamageToFighters().modifyFlat(id, PD_DAMAGE_BONUS / 100f);
		fighter.getMutableStats().getDamageToMissiles().modifyFlat(id, PD_DAMAGE_BONUS / 100f);
		
		boolean sMod = isSMod(ship);
		if (sMod) {
			fighter.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(id, SMOD_RANGE_BONUS);
			fighter.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(id, SMOD_RANGE_BONUS);
		}
		
		if (fighter.getWing() != null && fighter.getWing().getSpec() != null) {
			if (fighter.getWing().getSpec().isRegularFighter() || 
					fighter.getWing().getSpec().isAssault() ||
					fighter.getWing().getSpec().isBomber() ||
					fighter.getWing().getSpec().isInterceptor()) {
				fighter.addTag(Tags.WING_STAY_IN_FRONT_OF_SHIP);
			}
		}
	}
		
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SMOD_RANGE_BONUS + "";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) PD_DAMAGE_BONUS + "%";
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		int bays = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
		return ship != null && bays > 0; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship does not have fighter bays";
	}
}




