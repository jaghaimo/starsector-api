package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class SensorDishRotationEffect implements EveryFrameWeaponEffectPlugin {

	private float currDir = Math.signum((float) Math.random() - 0.5f);
	
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused()) return;
		
		float curr = weapon.getCurrAngle();
		
		curr += currDir * amount * 10f;
		float arc = weapon.getArc();
		float facing = weapon.getArcFacing() + (weapon.getShip() != null ? weapon.getShip().getFacing() : 0);
		if (!isBetween(facing - arc/2, facing + arc/2, curr)) {
			currDir = -currDir;
		}
		
		weapon.setCurrAngle(curr);
	}

	public static boolean isBetween(float one, float two, float check) {
		one = normalizeAngle(one);
		two = normalizeAngle(two);
		check = normalizeAngle(check);

		//System.out.println(one + "," + two + "," + check);
		if (check >= one && check <= two) return true;
		
		if (one > two) {
			if (check <= two) return true;
			if (check >= one) return true;
		}
		return false;
	}
	
	public static float normalizeAngle(float angleDeg) {
		return (angleDeg % 360f + 360f) % 360f;
	}
}
