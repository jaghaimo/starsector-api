package com.fs.starfarer.api.impl.combat.dweller;

import java.util.Iterator;
import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow.EMPArcHitType;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow.RDRepairRateDebuff;
import com.fs.starfarer.api.util.Misc;

/**
 */
public class InimicalEmanationOnFireEffect implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

	public static float EXTRA_ARC = 30f;
	public static float REPAIR_RATE_DEBUFF_DUR = 5f;
	
//	public static String PREV_INIMICAL_EMANATION_FIRE_TIMESTAMP_KEY = "prev_inimical_emanation_fire_timestamp_key";
//	public static float EXTRA_RANGE_SYMPATHETIC = 300f;
	

	protected float extraRangeOnNextFire = 0f;
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
//		Float firedPrev = (Float) engine.getCustomData().get(PREV_INIMICAL_EMANATION_FIRE_TIMESTAMP_KEY);
//		if (firedPrev == null) firedPrev = -1000f;
//		
//		float currTimestamp = engine.getTotalElapsedTime(false);
//		float sinceFired = currTimestamp - firedPrev;
//		if (sinceFired < 0.1f && (float) Math.random() > 0.85f) {
//			extraRangeOnNextFire = EXTRA_RANGE_SYMPATHETIC;
//			weapon.setForceFireOneFrame(true);
//		}
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		if (ship == null) return;
		
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
		
		CombatEntityAPI target = findTarget(projectile, weapon, engine);
		
		Vector2f noTargetDest = null;
		if (target == null) noTargetDest = pickNoTargetDest(projectile, weapon, engine);
		
		Vector2f towards = noTargetDest;
		if (target != null) towards = target.getLocation();
		
		float thickness = 30f;
		Color color = weapon.getSpec().getGlowColor();
		Color coreColor = Color.white;
		coreColor = Misc.zeroColor;
		coreColor = color;
		
		
		color = new Color(255,0,30,255);
		//coreColor = new Color(255,10,50,155);
		coreColor = new Color(255,10,255,255);
//		coreColor = new Color(255,0,30,255);
//		color = new Color(255,10,255,255);
		
		//color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
		color = DwellerShroud.SHROUD_GLOW_COLOR;
		coreColor = color;
//		coreColor = Misc.interpolateColor(color, Color.white, 0.25f);
//		coreColor = Color.white;
		
		float coreWidthMult = 1f;
		
		
		Vector2f from = projectile.getLocation();
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		if (shroud != null) {
			float angle = Misc.getAngleInDegrees(ship.getLocation(), towards);
			from = Misc.getUnitVectorAtDegreeAngle(angle + 90f - 180f * (float) Math.random());
			from.scale((0.5f + (float) Math.random() * 0.25f) * shroud.getShroudParams().maxOffset);
			Vector2f.add(ship.getLocation(), from, from);
		}
		
		EmpArcParams params = new EmpArcParams();
		//params.segmentLengthMult = 10000f;
		params.segmentLengthMult = 4f;
		
//		params.maxZigZagMult = 0f;
//		params.zigZagReductionFactor = 1f;
		
		params.maxZigZagMult = 0.25f;
		params.zigZagReductionFactor = 1f;
		
		//params.glowColorOverride = new Color(255,10,155,255);
		
		//params.zigZagReductionFactor = 0.25f;
		//params.maxZigZagMult = 0f;
		//params.flickerRateMult = 0.75f;
//		params.flickerRateMult = 1f;
//		params.flickerRateMult = 0.75f;
		params.flickerRateMult = 0.75f + 0.25f * (float) Math.random();
		
		params.fadeOutDist = 150f;
		params.minFadeOutMult = 5f;
		
		params.glowSizeMult = 0.5f;
		//params.glowAlphaMult = 0.5f;
		//params.flamesOutMissiles = false;
		
//		params.movementDurOverride = 0.1f;
//		params.flickerRateMult = 0.5f;
//		params.glowSizeMult = 1f;
//		params.brightSpotFadeFraction = 0.1f;
//		params.brightSpotFullFraction = 0.9f;
		
//		params.maxZigZagMult = 1f;
//		params.zigZagReductionFactor = 0f;
//		params.flickerRateMult = 0.25f;
		
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArc(ship, from, ship,
					   target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "inimical_emanation_impact",
					   thickness, // thickness
					   color,
					   coreColor,
					   params
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			arc.setRenderGlowAtStart(false);
			if (shroud != null) {
				arc.setFadedOutAtStart(true);
			}
			arc.setWarping(0.2f);
			
			if (target instanceof ShipAPI && !arc.isShieldHit()) {
				ShipAPI s = (ShipAPI) target;
				List<RDRepairRateDebuff> listeners = s.getListeners(RDRepairRateDebuff.class);
				if (listeners.isEmpty()) {
					s.addListener(new RDRepairRateDebuff(s, REPAIR_RATE_DEBUFF_DUR));
				} else {
					listeners.get(0).resetDur(REPAIR_RATE_DEBUFF_DUR);
				}
			}
			
			
			if (arc.getTargetLocation() != null) {
				RealityDisruptorChargeGlow.spawnEMPParticles(EMPArcHitType.INIMICAL_EMANATION, null, arc.getTargetLocation(), target);
			}
			
		} else {
			Vector2f to = noTargetDest;
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, ship, to, ship, thickness, color, coreColor, params);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			arc.setRenderGlowAtStart(false);
			if (shroud != null) {
				arc.setFadedOutAtStart(true);
			}
			arc.setWarping(0.2f);
			
			RealityDisruptorChargeGlow.spawnEMPParticles(EMPArcHitType.INIMICAL_EMANATION, null, to, ship);
		}
		
//		float fireTimestamp = engine.getTotalElapsedTime(false);
//		engine.getCustomData().put(PREV_INIMICAL_EMANATION_FIRE_TIMESTAMP_KEY, Float.valueOf(fireTimestamp));
	}
	
	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float spread = 50f;
		float range = Math.min(weapon.getRange() - spread, 300f);
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle() + (EXTRA_ARC/2f - EXTRA_ARC * (float) Math.random()));
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, spread);
		return dir;
	}
	
	public CombatEntityAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange() + 50f + extraRangeOnNextFire;
		extraRangeOnNextFire = 0f;
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		
		ShipAPI ship = weapon.getShip();
		boolean ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue(Stats.PD_IGNORES_FLARES, 0) >= 1;
		ignoreFlares |= weapon.hasAIHint(AIHints.IGNORES_FLARES);
		
		boolean phaseMode = true;
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					//!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			boolean phaseHit = false;
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isHulk()) continue;
				//if (!otherShip.isAlive()) continue;
				if (otherShip.isPhased()) {
					if (phaseMode) {
						phaseHit = true;
					} else {
						continue;
					}
				}
				if (!otherShip.isTargetable()) continue;
			}
			
			if (!phaseHit && other.getCollisionClass() == CollisionClass.NONE) continue;
			
			if (ignoreFlares && other instanceof MissileAPI) {
				MissileAPI missile = (MissileAPI) other;
				if (missile.isFlare()) continue;
			}

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist > range) continue;
			
			if (!Misc.isInArc(weapon.getCurrAngle(), EXTRA_ARC, from, other.getLocation())) continue;
			
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
		}
		return best;
	}

}
