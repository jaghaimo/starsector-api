package com.fs.starfarer.api.impl.combat.dweller;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.util.Misc;

/**
 * Multiple instances of this plugin - one for every projectile (on hit), and one for each weapon.
 * 
 * The goal is for the on-hit effect to fire off a lightning arc in case of a hit, and for the onfire/every frame copy
 * of the plugin to fire off a lightning arc in case there is a miss.
 * 
 * @author Alex
 *
 */
public class RiftLightningEffect implements OnHitEffectPlugin, OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
	

	
	
	public static Color RIFT_LIGHTNING_COLOR = new Color(255,50,50,255);
	public static float RIFT_LIGHTNING_SPEED = 10000f;
	
//	public static String RIFT_LIGHTNING_PROJ_TAG = "rift_lightning_proj_tag";
	public static String RIFT_LIGHTNING_DAMAGE_REMOVER = "rift_lightning_damage_remover";
	public static String RIFT_LIGHTNING_FIRED_TAG = "rift_lightning_fired_tag";
	public static String RIFT_LIGHTNING_SOURCE_WEAPON = "rift_lightning_source_weapon";
	
	public static class FiredLightningProjectile {
		public DamagingProjectileAPI projectile;
	}
	
	
//	/**
//	 * The actual damage is dealt by the rift explosion.
//	 * (Removing this: setting multiplier to 0 on projectile instead)
//	 * @author Alex
//	 *
//	 */
//	public static class RiftLightningBaseDamageNegator implements DamageDealtModifier {
//		@Override
//		public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
//			if (param instanceof DamagingProjectileAPI) {
//				DamagingProjectileAPI proj = (DamagingProjectileAPI) param;
//				if (proj.getCustomData().containsKey(RIFT_LIGHTNING_PROJ_TAG)) {
//					damage.getModifier().modifyMult(RIFT_LIGHTNING_PROJ_TAG, 0f);
//					return RIFT_LIGHTNING_PROJ_TAG;
//				}
//			}
//			return null;
//		}
//	}
	
	protected List<FiredLightningProjectile> fired = new ArrayList<>();
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
//		if (!fired.isEmpty()) {
//			System.out.println("FIRED");
//		}
		List<FiredLightningProjectile> remove = new ArrayList<>();
		
		float maxRange = weapon.getRange();
		for (FiredLightningProjectile data : fired) {
			float dist = Misc.getDistance(data.projectile.getSpawnLocation(), data.projectile.getLocation());
			boolean firedAlready = data.projectile.getCustomData().containsKey(RIFT_LIGHTNING_FIRED_TAG);
			if (dist > maxRange || firedAlready) {
				remove.add(data);
				if (!firedAlready) {
					fireArc(data.projectile, weapon, null, null);
				}
			}
		}
		fired.removeAll(remove);
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
//		if (weapon.getShip() != null && 
//				!weapon.getShip().hasListenerOfClass(RiftLightningBaseDamageNegator.class)) {
//			weapon.getShip().addListener(new RiftLightningBaseDamageNegator());
//		}
		//projectile.setCustomData(RIFT_LIGHTNING_PROJ_TAG, true);
		
		projectile.getDamage().getModifier().modifyMult(RIFT_LIGHTNING_DAMAGE_REMOVER, 0f);
		projectile.setCustomData(RIFT_LIGHTNING_SOURCE_WEAPON, weapon);

		FiredLightningProjectile data = new FiredLightningProjectile();
		data.projectile = projectile;
		fired.add(data);
	}
	

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		WeaponAPI weapon = (WeaponAPI) projectile.getCustomData().get(RIFT_LIGHTNING_SOURCE_WEAPON);
		if (weapon == null) return;
		
		fireArc(projectile, weapon, point, target);
	}
			
	public static void fireArc(DamagingProjectileAPI projectile, WeaponAPI weapon, Vector2f point, CombatEntityAPI target) {
		boolean firedAlready = projectile.getCustomData().containsKey(RIFT_LIGHTNING_FIRED_TAG);
		if (firedAlready) return;
		
		projectile.setCustomData(RIFT_LIGHTNING_FIRED_TAG, true);
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		ShipAPI ship = weapon.getShip();
		if (ship == null) return;
		
		//Vector2f from = weapon.getFirePoint(0);
		Vector2f from = projectile.getSpawnLocation();
		

		float dist = Float.MAX_VALUE;
		if (point != null) dist = Misc.getDistance(from, point);
		
		float maxRange = weapon.getRange();
		if (dist > maxRange || point == null) {
			dist = maxRange * (0.5f + 0.5f * (float) Math.random());
			if (projectile.didDamage()) {
				dist = maxRange;
			}
			point = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing());
			point.scale(dist);
			Vector2f.add(point, from, point);
		}
		
		float arcSpeed = RIFT_LIGHTNING_SPEED;
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		if (shroud != null) {
			float angle = Misc.getAngleInDegrees(ship.getLocation(), point);
			from = Misc.getUnitVectorAtDegreeAngle(angle + 90f - 180f * (float) Math.random());
			from.scale((0.5f + (float) Math.random() * 0.25f) * shroud.getShroudParams().maxOffset);
			Vector2f.add(ship.getLocation(), from, from);
		}
		

		EmpArcParams params = new EmpArcParams();
		params.segmentLengthMult = 8f;
		params.zigZagReductionFactor = 0.15f;
		params.fadeOutDist = 50f;
		params.minFadeOutMult = 10f;
//		params.flickerRateMult = 0.7f;
		params.flickerRateMult = 0.3f;
//		params.flickerRateMult = 0.05f;
//		params.glowSizeMult = 3f;
//		params.brightSpotFullFraction = 0.5f;
		
		params.movementDurOverride = Math.max(0.05f, dist / arcSpeed);
			
		//Color color = weapon.getSpec().getGlowColor();
		Color color = RIFT_LIGHTNING_COLOR;
		EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(from, ship, point, null,
				80f, // thickness
				color,
				new Color(255,255,255,255),
				params
				);
		arc.setCoreWidthOverride(40f);
		
		arc.setRenderGlowAtStart(false);
		arc.setFadedOutAtStart(true);
		arc.setSingleFlickerMode(true);
	
		spawnMine(ship, point, params.movementDurOverride * 0.8f); // - 0.05f);
		
		
		if (shroud != null) {
			DwellerShroudParams shroudParams = shroud.getShroudParams();
			params = new EmpArcParams();
			params.segmentLengthMult = 4f;
			params.glowSizeMult = 4f;
			params.flickerRateMult = 0.5f + (float) Math.random() * 0.5f;
			params.flickerRateMult *= 1.5f;
			
			//Color fringe = shroudParams.overloadArcFringeColor;
			Color fringe = color;
			Color core = Color.white;

			float thickness = shroudParams.overloadArcThickness;
			
			//Vector2f to = Misc.getPointAtRadius(from, 1f);
			
			float angle = Misc.getAngleInDegrees(from, ship.getLocation());
			angle = angle + 90f * ((float) Math.random() - 0.5f);
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			dist = shroudParams.maxOffset;
			dist = dist * 0.5f + dist * 0.5f * (float) Math.random();
			//dist *= 1.5f;
			dist *= 0.5f;
			dir.scale(dist);
			Vector2f to = Vector2f.add(from, dir, new Vector2f());
			
			arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(
					from, ship, to, ship, thickness, fringe, core, params);
			
			arc.setCoreWidthOverride(shroudParams.overloadArcCoreThickness);
			arc.setSingleFlickerMode(false);
			//arc.setRenderGlowAtStart(false);
		}
		
	}
	
	public static void spawnMine(ShipAPI source, Vector2f mineLoc, float delay) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		
		//Vector2f currLoc = mineLoc;
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
															  "rift_lightning_minelayer", 
															  mineLoc, 
															  (float) Math.random() * 360f, null);
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.ENERGY, false, mine.getDamage());
		}
		
		
		float fadeInTime = 0.05f;
		mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);
		
		float liveTime = Math.max(delay, 0f);
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		mine.addDamagedAlready(source);
		mine.setNoMineFFConcerns(true);
		if (liveTime <= 0.016f) {
			mine.explode();
		}
	}

}
