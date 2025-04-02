package com.fs.starfarer.api.impl.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class MiningBlasterOnHitEffect implements OnHitEffectPlugin {

	public static float DAMAGE = 100;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (!shieldHit && target instanceof ShipAPI) {
			float mult = 1f;
//			if (projectile != null && projectile.getSource() != null) {
//				mult = projectile.getSource().getMutableStats().getEnergyWeaponDamageMult().getModifiedValue();
//			}
			BreachOnHitEffect.dealArmorDamage(projectile, (ShipAPI) target, point, DAMAGE * mult);
		}
	}
}
