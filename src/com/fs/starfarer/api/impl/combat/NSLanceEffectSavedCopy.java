package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPluginWithReset;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc;

public class NSLanceEffectSavedCopy implements BeamEffectPluginWithReset {

	public static float MIN_SPAWN_DIST = 200f;
	public static float DIST_PER_SPAWN = 150;
	public static float NUM_SPAWNS = 5;
	public static float SPAWN_INTERVAL = 0.1f;
	
	//private IntervalUtil fireInterval = new IntervalUtil(0.25f, 1.75f);

	protected Vector2f arcFrom = null;
	protected Vector2f prevMineLoc = null;
	
	protected boolean done = false;
	protected float spawned = 0;
	protected int numToSpawn = 0;
	protected float untilNextSpawn = 0;
	protected float spawnDir = 0;
	protected boolean canSpawn = false;
	public void reset() {
		done = false;
		spawned = 0;
		untilNextSpawn = 0;
		arcFrom = null;
		prevMineLoc = null;
		numToSpawn = 0;
		spawnDir = 0;
	}
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (done) return;
		
		//if (beam.getBrightness() < 1f) return;
		
		if (numToSpawn <= 0 && beam.getDamageTarget() != null) {
			float range = beam.getWeapon().getRange();
			float length = beam.getLengthPrevFrame();
			//float perSpawn = range / NUM_SPAWNS;
			float perSpawn = DIST_PER_SPAWN;
			
			numToSpawn = (int) ((range - length) / perSpawn) + 1;
			//numToSpawn = 5;
			numToSpawn = 1;
			untilNextSpawn = 0f;
		}
		numToSpawn = 5;
		
		if (beam.getBrightness() >= 1f) {
			canSpawn = true;
		}
		//untilNextSpawn = 0f;
		
		untilNextSpawn -= amount;
		if (untilNextSpawn > 0) return;
		//if (!canSpawn || beam.getBrightness() >= 1f) return;
		
//		NUM_SPAWNS = 15f;
//		SPAWN_INTERVAL = 0.03f;
		float range = beam.getWeapon().getRange();
		float length = beam.getLengthPrevFrame();
		float perSpawn = (range - MIN_SPAWN_DIST) / Math.max(1, (NUM_SPAWNS - 1));
		//float perSpawn = range / NUM_SPAWNS;
		//float perSpawn = DIST_PER_SPAWN;
		float rangeToSpawnAt = MIN_SPAWN_DIST + spawned * perSpawn;
		if (numToSpawn == 1) {
			rangeToSpawnAt = range;
		}
		
		ShipAPI ship = beam.getSource();
		
		boolean spawnedMine = false;
		if (length > rangeToSpawnAt - 10f) {
			float angle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
			Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
			loc.scale(rangeToSpawnAt);
			Vector2f.add(loc, beam.getFrom(), loc);
			
			spawnMine(ship, loc);
			spawnedMine = true;
		} else if (beam.getDamageTarget() != null) {
			Vector2f arcTo = getNextArcLoc(engine, beam, perSpawn);
			float thickness = beam.getWidth();
			engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), Color.white);
//			engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), beam.getCoreColor());
//			engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), beam.getCoreColor());
//			engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), beam.getCoreColor());
//			engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), beam.getCoreColor());
			spawnMine(ship, arcTo);
			spawnedMine = true;
			arcFrom = arcTo;
		}

		untilNextSpawn = SPAWN_INTERVAL;
		if (spawnedMine) {
			spawned++;
			if (spawned >= numToSpawn) {
				done = true;
			}
		}
	}
	
	public Vector2f getNextArcLoc(CombatEngineAPI engine, BeamAPI beam, float perSpawn) {
		CombatEntityAPI target = beam.getDamageTarget();
		
		if (arcFrom == null) {
			arcFrom = new Vector2f(beam.getRayEndPrevFrame());
//			Vector2f loc = Misc.getUnitVectorAtDegreeAngle(beamAngle);
//			loc.scale(beam.getLengthPrevFrame());
//			//loc.scale(200f);
//			Vector2f.add(loc, beam.getFrom(), loc);
//			arcFrom = loc;
			
			float beamAngle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
			float beamSourceToTarget = Misc.getAngleInDegrees(beam.getFrom(), target.getLocation());
			
			// this is the direction we'll rotate - from the target's center - so that it's spawning mines around the side
			// closer to the beam's straight line
			spawnDir = Misc.getClosestTurnDirection(beamAngle, beamSourceToTarget);
			
			boolean computeNextLoc = false;
			if (prevMineLoc != null && false) {
				float dist = Misc.getDistance(arcFrom, prevMineLoc);
				if (dist < perSpawn) {
					perSpawn -= dist;
					computeNextLoc = true;
				}
			}
			if (!computeNextLoc) {
				return arcFrom;
			}
		}
		
//		target = Global.getCombatEngine().getPlayerShip();
//		target.getLocation().y += 750f;
		
		Vector2f targetLoc = target.getLocation();
		float targetRadius = target.getCollisionRadius();
		
//		if (target instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) target;
//			targetLoc = ship.getShieldCenterEvenIfNoShield();
//			targetRadius = ship.getShieldRadiusEvenIfNoShield();
//		}
		
		boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getRayEndPrevFrame());
		
//		float beamAngle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
//		float beamSourceToTarget = Misc.getAngleInDegrees(beam.getFrom(), targetLoc);
//		
//		// this is the direction we'll rotate - from the target's center - so that it's spawning mines around the side
//		// closer to the beam's straight line
//		float dir = Misc.getClosestTurnDirection(beamAngle, beamSourceToTarget);
		
		float prevAngle = Misc.getAngleInDegrees(targetLoc, arcFrom);
		float anglePerSegment = 360f * perSpawn / (3.14f * 2f * targetRadius);
		if (anglePerSegment > 90f) anglePerSegment = 90f;
		float angle = prevAngle + anglePerSegment * spawnDir;
		
		
		Vector2f arcTo = Misc.getUnitVectorAtDegreeAngle(angle);
		arcTo.scale(targetRadius);
		Vector2f.add(targetLoc, arcTo, arcTo);
		
		float actualRadius = Global.getSettings().getTargetingRadius(arcTo, target, hitShield);
		if (!hitShield) {
			//actualRadius *= 1f + 0.1f * (float) Math.random();
			actualRadius += 30f + 50f * (float) Math.random();
		} else {
			actualRadius += 30f + 50f * (float) Math.random();
		}
		
//		float angleDiff = Misc.getAngleDiff(beamSourceToTarget + 180f, angle);
//		if (angleDiff > 150f) {
//			actualRadius += perSpawn * (180f - angleDiff) / 30f;
//		}
		
		
		arcTo = Misc.getUnitVectorAtDegreeAngle(angle);
		arcTo.scale(actualRadius);
		Vector2f.add(targetLoc, arcTo, arcTo);
		
		
//		target.getLocation().y -= 750f;
		
		return arcTo;
	}
	
	public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		
		//Vector2f currLoc = mineLoc;
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
															  "nslance_minelayer", 
															  mineLoc, 
															  (float) Math.random() * 360f, null);
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.MISSILE, false, mine.getDamage());
//			float extraDamageMult = source.getMutableStats().getMissileWeaponDamageMult().getModifiedValue();
//			mine.getDamage().setMultiplier(mine.getDamage().getMultiplier() * extraDamageMult);
		}
		
		
		float fadeInTime = 0.05f;
		mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);
		
		//Global.getCombatEngine().addPlugin(createMissileJitterPlugin(mine, fadeInTime));
		
		//mine.setFlightTime((float) Math.random());
		float liveTime = 0f;
		//liveTime = 0.01f;
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		mine.addDamagedAlready(source);
		
		prevMineLoc = mineLoc;
	}

}





