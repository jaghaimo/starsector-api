package com.fs.starfarer.api.combat;


public interface OnFireEffectPlugin {
	void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine);
}
