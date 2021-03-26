package com.fs.starfarer.api.impl.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

/**
 * IMPORTANT: will be multiple instances of this, as this doubles as the every frame effect and the on fire effect (same instance)
 * But also as the visual for each individual shot (created via onFire, using the non-default constructor)
 */
public class RealityDisruptorEffect implements OnFireEffectPlugin, OnHitEffectPlugin, EveryFrameWeaponEffectPlugin {

	protected CombatEntityAPI chargeGlowEntity;
	protected RealityDisruptorChargeGlow chargeGlowPlugin;
	public RealityDisruptorEffect() {
	}
	
	//protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		//interval.advance(amount);
		
		boolean charging = weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
		if (charging && chargeGlowEntity == null) {
			chargeGlowPlugin = new RealityDisruptorChargeGlow(weapon);
			chargeGlowEntity = Global.getCombatEngine().addLayeredRenderingPlugin(chargeGlowPlugin);	
		} else if (!charging && chargeGlowEntity != null) {
			chargeGlowEntity = null;
			chargeGlowPlugin = null;
		}
	}
	
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		if (chargeGlowPlugin != null) {
			chargeGlowPlugin.attachToProjectile(projectile);
			chargeGlowPlugin = null;
			chargeGlowEntity = null;
			
			MissileAPI missile = (MissileAPI) projectile;
			missile.setMine(true);
			missile.setNoMineFFConcerns(true);
			missile.setMineExplosionRange(RealityDisruptorChargeGlow.MAX_ARC_RANGE + 50f);
			missile.setMinePrimed(true);
			missile.setUntilMineExplosion(0f);
		}
		
//		RiftTrailEffect trail = new RiftTrailEffect((MissileAPI) projectile);
//		((MissileAPI) projectile).setEmpResistance(1000);
//		Global.getCombatEngine().addPlugin(trail);
		
		
//		RealityDisruptorEffect effect = new RealityDisruptorEffect(projectile);
//		CombatEntityAPI e = engine.addLayeredRenderingPlugin(effect);
//		e.getLocation().set(projectile.getLocation());
	}
	


	
}




