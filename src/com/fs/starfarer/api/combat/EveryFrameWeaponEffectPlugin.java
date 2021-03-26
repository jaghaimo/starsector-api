package com.fs.starfarer.api.combat;

public interface EveryFrameWeaponEffectPlugin {
	/**
	 * Called before this frame's operations for the weapon - turning, firing, etc.
	 * @param amount
	 * @param engine
	 * @param weapon
	 */
	void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon);
	
}
