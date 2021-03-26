package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ShieldShunt extends BaseHullMod {

	public static final float EMP_RESISTANCE = 50f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - EMP_RESISTANCE * 0.01f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.setShield(ShieldType.NONE, 0f, 1f, 1f);
	}


	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) EMP_RESISTANCE + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().getHullSpec().getShieldType() == ShieldType.NONE && 
				!ship.getVariant().hasHullMod("frontshield")) return false;
		if (ship.getVariant().hasHullMod("shield_shunt")) return true;
		return ship != null && ship.getShield() != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship has no shields";
	}
	
	
}









