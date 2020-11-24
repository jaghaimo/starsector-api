package com.fs.starfarer.api.combat;

public interface EveryFrameWeaponEffectPlugin {
	void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon);
}
