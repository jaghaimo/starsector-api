package com.fs.starfarer.api.impl.combat;

import java.util.HashMap;
import java.util.LinkedHashMap;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;

public class HeavyAdjudicatorEffect implements OnFireEffectPlugin {

	/**
	 * Key is <hullId>_<slot id>.
	 */
	public static HashMap<String, Vector2f> SECOND_MUZZLE_FLASH_OFFSET_MAP = new LinkedHashMap<>();
	
	static {
		SECOND_MUZZLE_FLASH_OFFSET_MAP.put("onslaught_mk1_WS 016", new Vector2f(-39, 17));
		SECOND_MUZZLE_FLASH_OFFSET_MAP.put("onslaught_mk1_WS 017", new Vector2f(-39, -18));
	}
	
	
	public HeavyAdjudicatorEffect() {
	}

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = projectile.getSource();
		if (ship == null) return;
		
		MuzzleFlashSpec spec = ((ProjectileWeaponSpecAPI)weapon.getSpec()).getMuzzleFlashSpec().clone();
		
		spec.setLength(30f);
		spec.setParticleCount(30);
		spec.setSpread(90f);
//		spec.setParticleSizeMin(3);
//		spec.setParticleSizeRange(0f);
		
		Vector2f p = projectile.getLocation();
		float angle = weapon.getCurrAngle();
		
		spawnMuzzleFlash(((ProjectileWeaponSpecAPI)weapon.getSpec()).getMuzzleFlashSpec(), p, angle, ship.getVelocity(), 1f, 0f);
		
		String slotId = weapon.getSlot().getId();
		String id = ship.getHullSpec().getBaseHullId() + "_" + slotId;
		
		Vector2f offset = SECOND_MUZZLE_FLASH_OFFSET_MAP.get(id);
		if (offset != null) {
			float sign = Math.signum(offset.y);
			offset = new Vector2f(offset);
			offset = Misc.rotateAroundOrigin(offset, angle);
			Vector2f.add(offset, p, offset);
			spawnMuzzleFlash(spec, offset, angle + sign * 90f, ship.getVelocity(), 2f, 20f);
		}
		
	}

	public static void spawnMuzzleFlash(MuzzleFlashSpec spec, Vector2f point, float angle, Vector2f shipVel,
								float velMult, float velAdd) {
		if (spec == null) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Color color = spec.getParticleColor();
		float min = spec.getParticleSizeMin();
		float range = spec.getParticleSizeRange();
		float spread = spec.getSpread();
		float length = spec.getLength();
		for (int i = 0; i < spec.getParticleCount(); i++) {
			float size = range * (float) Math.random() + min;
			float theta = (float) (Math.random() * Math.toRadians(spread) + Math.toRadians(angle - spread/2f));
			float r = (float) (Math.random() * length);
			Vector2f dir = new Vector2f((float)Math.cos(theta), (float)Math.sin(theta));
			float x = dir.x * r;
			float y = dir.y * r;
			Vector2f loc = new Vector2f(point.x + x, point.y + y);
			Vector2f vel = new Vector2f(x * velMult + shipVel.x + dir.x * velAdd,
										y * velMult + shipVel.y + dir.y * velAdd);
			if (velAdd > 0 || true) {
				Vector2f rand = Misc.getPointWithinRadius(new Vector2f(), spec.getLength() * 0.5f);
				Vector2f.add(vel, rand, vel);
			}
			//engine.addSmoothParticle(loc, vel, size, 1f, spec.getParticleDuration(), color);
			float dur = spec.getParticleDuration();
			dur *= 0.75f + (float) Math.random() * 0.5f;
			size *= 1.25f;
			engine.addNebulaParticle(loc, vel, size, 
					0.67f, // end size
					//2f, // end size
					0f, // ramp up
					0f, // full brightness
					dur, color);
		}
	}
	
}




