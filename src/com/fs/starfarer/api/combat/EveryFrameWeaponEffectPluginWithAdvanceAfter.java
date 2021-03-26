package com.fs.starfarer.api.combat;

public interface EveryFrameWeaponEffectPluginWithAdvanceAfter extends EveryFrameWeaponEffectPlugin {
	/**
	 * Called after this frame's operations for the weapon - turning, firing, etc.
	 * @param amount
	 * @param engine
	 * @param weapon
	 */
	void advanceAfter(float amount, CombatEngineAPI engine, WeaponAPI weapon);
}
