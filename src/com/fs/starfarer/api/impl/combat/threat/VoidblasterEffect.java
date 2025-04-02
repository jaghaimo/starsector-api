package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.combat.SquallOnFireEffect;
import com.fs.starfarer.api.util.Misc;

public class VoidblasterEffect implements OnHitEffectPlugin, OnFireEffectPlugin, EveryFrameWeaponEffectPlugin,
											DamageDealtModifier {
	
	protected String weaponId;
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
	}
	
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		if (!ship.hasListenerOfClass(SquallOnFireEffect.class)) {
			ship.addListener(this);
			weaponId = weapon.getId();
		}
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		Vector2f vel = target.getVelocity();
		engine.addNegativeParticle(point, vel, 50f, 0f, 0.5f, Color.white);
		engine.addNegativeParticle(point, vel, 30f, 0f, 0.5f, Color.white);
		engine.addNegativeNebulaParticle(point, vel, 30f, 2f, 0f, 0f, 0.5f, Color.white);
		//engine.addNegativeParticle(point, vel, 15f, 0f, 0.5f, Color.white);
		if (!shieldHit) {
			float dir = 0f;
			float arc = 360f;
			dir = Misc.getAngleInDegrees(target.getLocation(), point);
			arc = 150f;
			engine.spawnDebrisSmall(point, vel, 12, dir, arc, 20f, 20f, 720f);
			engine.spawnDebrisMedium(point, vel, 4, dir, arc, 10f, 20f, 360f);
			engine.spawnDebrisLarge(point, vel, 1, dir, arc, 10f, 10f, 180f);
		}
	}
	
	
	public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
		if (param instanceof DamagingProjectileAPI) {
			DamagingProjectileAPI p = (DamagingProjectileAPI) param;
			if (p.getWeapon() != null && p.getWeapon().getId().equals(weaponId)) {
				if (target instanceof ShipAPI) {
					((ShipAPI)target).setSkipNextDamagedExplosion(true);
				}
				if (shieldHit) {
					damage.setSoftFlux(true);
				}
				return "voidblaster";
			}
		}
		return null;
	}
}







