package com.fs.starfarer.api.impl.combat.threat;

import java.util.Iterator;

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
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 */
public class VoltaicDischargeOnFireEffect implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin, 
													FragmentWeapon {

	public static String SWARM_TAG_PHASE_MODE = "swarm_tag_phase_mode";
	
	public static Color EMP_FRINGE_COLOR_BRIGHT = new Color(213,255,237,255);
	
	public static Color EMP_FRINGE_COLOR = new Color(130,155,145,255);
	public static Color PHASE_FRINGE_COLOR = new Color(120,110,185,255);
	public static Color PHASE_CORE_COLOR = new Color(255,255,255,127);
	
	public static float EXTRA_ARC = 360f;
	public static int FRAGMENTS_TO_FIRE = 10;
	
	
	public static boolean isSwarmPhaseMode(ShipAPI ship) {
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		return swarm != null && swarm.params.tags.contains(SWARM_TAG_PHASE_MODE);
	}
	public static void setSwarmPhaseMode(ShipAPI ship) {
		new AttackSwarmPhaseModeScript(ship);
		
//		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
//		if (swarm != null) {
//			Color color = Misc.setAlpha(VoltaicDischargeOnFireEffect.PHASE_FRINGE_COLOR, 60);
//			swarm.params.flashFringeColor = color;
//			swarm.params.flashRadius = 180f;
//			swarm.params.tags.add(VoltaicDischargeOnFireEffect.SWARM_TAG_PHASE_MODE);
//			
//			for (WeaponAPI w : ship.getAllWeapons()) {
//				if (w.usesAmmo() && w.getSpec().hasTag(Tags.FRAGMENT_GLOW)) {
//					w.setAmmo(Integer.MAX_VALUE);
//					w.setMaxAmmo(Integer.MAX_VALUE);
//				}
//				if (w.getSpec().hasTag(Tags.OVERSEER_CHARGE) || 
//						(ship.isFighter() && w.getSpec().hasTag(Tags.OVERSEER_CHARGE_FIGHTER))) {
//					w.setAmmo(w.getMaxAmmo());
//				}
//			}
//		}
	}
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		ShipAPI ship = weapon.getShip();
		if (ship == null) return;
		
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		int active = swarm == null ? 0 : swarm.getNumActiveMembers();
		int required = getNumFragmentsToFire();
		boolean disable = active < required;
		weapon.setForceDisabled(disable);
		
		showNoFragmentSwarmWarning(weapon, ship);
	}
	
	@Override
	public int getNumFragmentsToFire() {
		return FRAGMENTS_TO_FIRE;
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		//ARC = 30f;
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
		
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(projectile.getSource());
		if (swarm == null || swarm.getAttachedTo() == null) return;
		
		CombatEntityAPI target = findTarget(projectile, weapon, engine);
		
		Vector2f noTargetDest = null;
		if (target == null) noTargetDest = pickNoTargetDest(projectile, weapon, engine);
		
		Vector2f towards = noTargetDest;
		if (target != null) towards = target.getLocation();
		
		SwarmMember pick = pickFragmentTowardsPointWithinRange(swarm, towards, 150f);
		if (pick == null) return;
		
		pick.setRecentlyPicked(1f);
		
		float thickness = 30f;
		//Color color = weapon.getSpec().getGlowColor();
		//Color color = new Color(255,0,0,255);
		Color color = EMP_FRINGE_COLOR;
		Color coreColor = Color.white;
		
		boolean phaseMode = isSwarmPhaseMode(projectile.getSource());
		if (phaseMode) {
			color = PHASE_FRINGE_COLOR;
			if (target instanceof ShipAPI && ((ShipAPI)target).isPhased()) {
				coreColor = PHASE_CORE_COLOR;
			}
		}
		
		float coreWidthMult = 0.75f;
		
		EmpArcParams params = new EmpArcParams();
		params.segmentLengthMult = 8f;
		//params.zigZagReductionFactor = 0.25f;
		params.zigZagReductionFactor = 0.5f;
		//params.maxZigZagMult = 0f;
		//params.flickerRateMult = 0.75f;
		params.flickerRateMult = 1f;
		params.fadeOutDist = 1000f;
		params.minFadeOutMult = 1f;
//		params.fadeOutDist = 200f;
//		params.minFadeOutMult = 2f;
		params.glowSizeMult = 0.5f;
		params.glowAlphaMult = 0.75f;
		
		// actually, probably fine given how long it takes to chew through the missile health with low damage per hit
		//params.flamesOutMissiles = false; // a bit much given the RoF and general prevalence
		
		pick.flash();
		pick.flash.forceIn();
		pick.flash.setDurationOut(0.25f);
		
		//weapon.setAmmo(20);
		
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArc(projectile.getSource(), pick.loc, weapon.getShip(),
					   target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "voltaic_discharge_emp_impact",
					   thickness, // thickness
					   color,
					   coreColor,
					   params
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			arc.setUpdateFromOffsetEveryFrame(true);
			arc.setRenderGlowAtStart(false);
			arc.setFadedOutAtStart(true);
		} else {
			params.flickerRateMult = 1f;
			
			Vector2f to = noTargetDest;
			//Vector2f to = targetLoc;
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(pick.loc, weapon.getShip(), to, weapon.getShip(), thickness, color, coreColor, params);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			arc.setUpdateFromOffsetEveryFrame(true);
			arc.setRenderGlowAtStart(false);
			arc.setFadedOutAtStart(true);
			//Global.getSoundPlayer().playSound("shock_repeater_emp_impact", 1f, 1f, to, new Vector2f());
		}
	}
	
	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float spread = 50f;
		float range = Math.min(weapon.getRange() - spread, 150f);
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle() + (EXTRA_ARC/2f - EXTRA_ARC * (float) Math.random()));
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, spread);
		return dir;
	}
	
	public CombatEntityAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange() + 50f;
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		
		ShipAPI ship = weapon.getShip();
		boolean ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue(Stats.PD_IGNORES_FLARES, 0) >= 1;
		ignoreFlares |= weapon.hasAIHint(AIHints.IGNORES_FLARES);
		
		boolean phaseMode = isSwarmPhaseMode(ship);
		
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
	
	public static SwarmMember pickFragmentTowardsPointWithinRange(RoilingSwarmEffect swarm, Vector2f towards, float maxRange) {
		WeightedRandomPicker<SwarmMember> picker = swarm.getPicker(true, true, towards);
		while (!picker.isEmpty()) {
			SwarmMember p = picker.pickAndRemove();
			float dist = Misc.getDistance(p.loc, swarm.getAttachedTo().getLocation());
			if (dist > maxRange) continue;
			return p;
		}
		return null;
	}


}
