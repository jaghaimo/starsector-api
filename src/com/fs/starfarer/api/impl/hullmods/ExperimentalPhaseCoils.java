package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ExperimentalPhaseCoils extends BaseHullMod {

	public static float PHASE_COOLDOWN_REDUCTION = 80f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f - PHASE_COOLDOWN_REDUCTION / 100f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(PHASE_COOLDOWN_REDUCTION) + "%";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}








