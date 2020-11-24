package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

public interface OnHitEffectPlugin {
	void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine);
}
