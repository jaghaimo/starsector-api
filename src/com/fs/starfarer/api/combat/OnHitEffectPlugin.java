package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public interface OnHitEffectPlugin {
	void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine);
}
