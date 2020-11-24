package com.fs.starfarer.api.impl.combat;

import java.util.Random;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class BlinkerEffect implements EveryFrameWeaponEffectPlugin {

	private float elapsed = 0;
	boolean on = true;
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused()) return;
		elapsed += amount;

		if (elapsed > 0.5f) {
			on = !on;
			elapsed -= 0.5f;
		}
		
		ShipAPI ship = weapon.getShip();
		if (ship.isHulk()) on = false;
		
		if (ship.getFluxTracker().isVenting()) {
			on = false;
		} else if (ship.getFluxTracker().isOverloaded()) {
			on = new Random().nextInt(4) == 3;
		}
		
		if (on) {
			weapon.getAnimation().setFrame(0);
		} else {
			weapon.getAnimation().setFrame(1);
		}
	}
}
