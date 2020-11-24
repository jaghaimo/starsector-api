package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class PeriodicMissileReload extends BaseHullMod {

	
	public static String MR_DATA_KEY = "core_reload_data_key";
	
	public static class PeriodicMissileReloadData {
		IntervalUtil interval = new IntervalUtil(10f, 15f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
		
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = MR_DATA_KEY + "_" + ship.getId();
		PeriodicMissileReloadData data = (PeriodicMissileReloadData) engine.getCustomData().get(key);
		if (data == null) {
			data = new PeriodicMissileReloadData();
			engine.getCustomData().put(key, data);
		}
		
		data.interval.advance(amount);
		if (data.interval.intervalElapsed()) {
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.getType() != WeaponType.MISSILE) continue;
				
				if (w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {
					w.setAmmo(w.getMaxAmmo());
				}
			}
		}
		
	}
	
}











