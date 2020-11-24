package com.fs.starfarer.api.impl.combat;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class LowCRShipDamageSequence extends BaseEveryFrameCombatPlugin {
	private ShipAPI ship;
	private CombatEngineAPI engine;
	
	private float severity;
	private float elapsed = 0f;
	private float beforeDamage;
	private IntervalUtil tracker = new IntervalUtil(0.25f, 1f);
	
	private WeightedRandomPicker<Object> disableTargets = new WeightedRandomPicker<Object>(true);
	private float disableAttempts = 0;
	
	public LowCRShipDamageSequence(ShipAPI ship, float severity) {
		this.ship = ship;
		this.severity = severity;
		
		beforeDamage = 1f + 2f * (float) Math.random();
		
		List<WeaponAPI> weapons = ship.getUsableWeapons();
		for (WeaponAPI weapon : weapons) {
			disableTargets.add(weapon);
		}
		List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
		for (ShipEngineAPI engine : engines) {
			if (!engine.isSystemActivated()) {
				disableTargets.add(engine);
			}
		}
		
		disableAttempts = Math.max(1, Math.round((0.25f + 0.75f * severity) * (float) disableTargets.getItems().size() * 0.25f));
		
		if (severity >= 1) {
			disableAttempts *= 2f;
		}
	}
	
	public void disableNext() {
		if (disableAttempts <= 0) return;
		disableAttempts--;
		List<Object> remove = new ArrayList<Object>();
//		List<Object> usableWeapons = new ArrayList<Object>();
		for (Object target : disableTargets.getItems()) {
//			if (target instanceof ShipEngineAPI) {
//				float fractionIfDisabled = ((ShipEngineAPI) target).getContribution() + ship.getEngineFractionPermanentlyDisabled();
//				if (fractionIfDisabled > 0.5f) {
//					remove.add(target);
//				}
//			} else if (target instanceof WeaponAPI) {
//				WeaponAPI weapon = (WeaponAPI) target;
//				if (weapon.getAmmo() > 0) {
//					usableWeapons.add(target);
//				}
//			}
			if (!CRPluginImpl.isOkToPermanentlyDisableStatic(ship, target)) {
				remove.add(target);
			}
		}
//		if (usableWeapons.size() <= 1) remove.addAll(usableWeapons);
		for (Object target : remove) {
			disableTargets.remove(target);
		}
		
		Object module = disableTargets.pick();
		if (module != null) {
			disableTargets.remove(module);
			ship.applyCriticalMalfunction(module);
		}
	}
	
	

	public void init(CombatEngineAPI engine) {
		this.engine = engine;
	}
	
	public void advance(float amount, List<InputEventAPI> events) {
		if (engine.isPaused()) return;
	
		if (!engine.isEntityInPlay(ship)) return; // ship is in refit screen
		
//		System.out.println("Advance: " + amount);
		elapsed += amount;
		
		if (elapsed > beforeDamage) {
			tracker.advance(amount);
			if (tracker.intervalElapsed()) {
				disableNext();
				
			}
			if (ship.isHulk() || disableAttempts <= 0) {
				engine.removePlugin(this);
			}
		}
	}
	
	

	
	
	
}






