package com.fs.starfarer.api.impl.hullmods;

import java.util.Iterator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class EscortPackage extends BaseHullMod {

	public static float MANEUVER_BONUS = 25f;
	public static float SPEED_BONUS = 10f;
	public static float RANGE_BONUS = 20f;
	public static float SHIELD_BONUS = 10f;
	
	public static float EFFECT_RANGE = 700f;
	public static float EFFECT_FADE = 500f;
	
	public static Object STATUS_KEY = new Object();
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "1000";
		if (index == 1) return "" + (int)Math.round(MANEUVER_BONUS) + "%";
		if (index == 2) return "" + (int)Math.round(SPEED_BONUS) + "%";
		if (index == 3) return "" + (int)Math.round(RANGE_BONUS) + "%";
		if (index == 4) return "doubled";
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(SHIELD_BONUS) + "%";
		return null;
	}


	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return getUnapplicableReason(ship) == null;
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && ship.isFrigate()) {
			return "Can not be installed on frigates";
		}
		if (ship != null && ship.isCapital()) {
			return "Can not be installed on capital ships";
		}
		return super.getUnapplicableReason(ship);
	}

	
	public static String EP_DATA_KEY = "core_escort_package_data_key";
	public static class EscortPackageData {
		IntervalUtil interval = new IntervalUtil(0.9f, 1.1f);
		float mag = 0;
	}
	
	public void applyEPEffect(ShipAPI ship, ShipAPI other, float mag) {
		String id = "escort_package_bonus" + ship.getId();
		MutableShipStatsAPI stats = ship.getMutableStats();
		
		if (mag > 0) {
			boolean sMod = isSMod(ship);
			
			float maneuver = MANEUVER_BONUS * mag;
			stats.getAcceleration().modifyPercent(id, maneuver);
			stats.getDeceleration().modifyPercent(id, maneuver);
			stats.getTurnAcceleration().modifyPercent(id, maneuver * 2f);
			stats.getMaxTurnRate().modifyPercent(id, maneuver);
			
			float speed = SPEED_BONUS * mag;
			stats.getMaxSpeed().modifyPercent(id, speed);
			
			float range = RANGE_BONUS * mag;
			stats.getBallisticWeaponRangeBonus().modifyPercent(id, range);
			stats.getEnergyWeaponRangeBonus().modifyPercent(id, range);
			
			if (sMod && ship.isDestroyer()) {
				float shields = SHIELD_BONUS * mag;
				stats.getShieldDamageTakenMult().modifyMult(id, 1f - shields / 100f);
			}
		} else {
			stats.getAcceleration().unmodify(id);
			stats.getDeceleration().unmodify(id);
			stats.getTurnAcceleration().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
			
			stats.getMaxSpeed().unmodify(id);
			
			stats.getBallisticWeaponRangeBonus().unmodify(id);
			stats.getEnergyWeaponRangeBonus().unmodify(id);
			
			stats.getShieldDamageTakenMult().unmodify(id);
		}
		
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = EP_DATA_KEY + "_" + ship.getId();
		EscortPackageData data = (EscortPackageData) engine.getCustomData().get(key);
		if (data == null) {
			data = new EscortPackageData();
			engine.getCustomData().put(key, data);
		}

		boolean playerShip = ship == Global.getCombatEngine().getPlayerShip();
		
		data.interval.advance(amount * 4f);
		if (data.interval.intervalElapsed() || playerShip) {
			float checkSize = EFFECT_RANGE + EFFECT_FADE + ship.getCollisionRadius() + 300f;
			checkSize *= 2f;
			
			Iterator<Object> iter = Global.getCombatEngine().getShipGrid().getCheckIterator(
											ship.getLocation(), checkSize, checkSize);
			
			ShipAPI best = null;
			float bestMag = 0f;
			while (iter.hasNext()) {
				Object next = iter.next();
				if (!(next instanceof ShipAPI)) continue;
				
				ShipAPI other = (ShipAPI) next;
				
				if (ship == other) continue;
				if (other.getOwner() != ship.getOwner()) continue;
				if (other.isHulk()) continue;
				
				if (other.getHullSize().ordinal() <= ship.getHullSize().ordinal()) continue;
				
				float radSum = ship.getShieldRadiusEvenIfNoShield() + other.getShieldRadiusEvenIfNoShield();
				radSum *= 0.75f;
				float dist = Misc.getDistance(ship.getShieldCenterEvenIfNoShield(), other.getShieldCenterEvenIfNoShield());
				dist -= radSum;
				
				float mag = 0f;
				if (dist < EFFECT_RANGE) {
					mag = 1f;
				} else if (dist < EFFECT_RANGE + EFFECT_FADE) {
					mag = 1f - (dist - EFFECT_RANGE) / EFFECT_FADE;
				}
				
				if (ship.isDestroyer() && other.isCapital()) {
					mag *= 2f;
				}
				
				if (mag > bestMag) {
					best = other;
					bestMag = mag;
				}
			}
			
			//if (best != null && bestMag > 0) {
				applyEPEffect(ship, best, bestMag);
			//}
			
			data.mag = bestMag;
		}
		
		if (playerShip) {
			if (data.mag > 0.005f) {
				String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_escort_package");
				String percent = "" + (int) Math.round(data.mag * 100f) + "%";
				Global.getCombatEngine().maintainStatusForPlayerShip(
						STATUS_KEY, icon, "Escort package", percent + " telemetry quality", false);
			} else {
				String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_escort_package");
				Global.getCombatEngine().maintainStatusForPlayerShip(
						STATUS_KEY, icon, "Escort package", "no connection", true);
			}
		}
		
	}
	
}














