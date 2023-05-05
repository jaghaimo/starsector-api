package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class DistributedFireControl extends BaseHullMod {

	public static float WEAPON_DAMAGE_MULT = 0.5f;
	public static float EMP_DAMAGE_MULT = 0.5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getWeaponDamageTakenMult().modifyMult(id, WEAPON_DAMAGE_MULT);
		stats.getEmpDamageTakenMult().modifyMult(id, EMP_DAMAGE_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - WEAPON_DAMAGE_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round((1f - EMP_DAMAGE_MULT) * 100f) + "%";
		return null;
	}


}








