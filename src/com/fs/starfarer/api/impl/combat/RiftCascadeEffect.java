package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BoundsAPI.SegmentAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * Colors are:
 * beam fringe color, for beam fringe and emp arcs
 * beam glow color (beam weapon glow)
 * mine glow color (border around core of explosion, also pings?)
 * mine ping color (should be same as glow color)
 * explosion undercolor (specified in code only)
 * color subtracted around source of beam (code only)
 */
public class RiftCascadeEffect implements BeamEffectPlugin { //WithReset {
	
	public static Color STANDARD_RIFT_COLOR = new Color(100,60,255,255);
	public static Color EXPLOSION_UNDERCOLOR = new Color(100, 0, 25, 100);
	public static Color NEGATIVE_SOURCE_COLOR = new Color(200,255,200,25);
	
	public static String RIFTCASCADE_MINELAYER = "riftcascade_minelayer";
	
	public static int MAX_RIFTS = 5;
	public static float UNUSED_RANGE_PER_SPAWN = 200;
	public static float SPAWN_SPACING = 175;
	public static float SPAWN_INTERVAL = 0.1f;
	
	
	
	protected Vector2f arcFrom = null;
	protected Vector2f prevMineLoc = null;
	
	protected boolean doneSpawningMines = false;
	protected float spawned = 0;
	protected int numToSpawn = 0;
	protected float untilNextSpawn = 0;
	protected float spawnDir = 0;
	
	protected IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);

	// re-instantiated when beam fires so this doesn't matter
//	public void reset() {
//		doneSpawningMines = false;
//		spawned = 0;
//		untilNextSpawn = 0;
//		arcFrom = null;
//		prevMineLoc = null;
//		numToSpawn = 0;
//		spawnDir = 0;
//	}
	
	public RiftCascadeEffect() {
		//System.out.println("23rwerefewfwe");
	}
	
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		tracker.advance(amount);
		if (tracker.intervalElapsed()) {
			spawnNegativeParticles(engine, beam);
		}
		
		if (beam.getBrightness() < 1f) return;
		
		
		if (doneSpawningMines) return;
		
		if (numToSpawn <= 0 && beam.getDamageTarget() != null) {
			float range = beam.getWeapon().getRange();
			float length = beam.getLengthPrevFrame();
			//float perSpawn = range / NUM_SPAWNS;
			numToSpawn = (int) ((range - length) / UNUSED_RANGE_PER_SPAWN) + 1;
			if (numToSpawn > MAX_RIFTS) {
				numToSpawn = MAX_RIFTS;
			}
			untilNextSpawn = 0f;
		}
		//numToSpawn = 5;
		
//		if (beam.getBrightness() >= 1f) {
//			canSpawn = true;
//		}
		//untilNextSpawn = 0f;
		
		untilNextSpawn -= amount;
		if (untilNextSpawn > 0) return;
//		if (!canSpawn || beam.getBrightness() >= 1f) return;
		
		float perSpawn = SPAWN_SPACING;
		
		ShipAPI ship = beam.getSource();
		
		boolean spawnedMine = false;
		if (beam.getLength() > beam.getWeapon().getRange() - 10f) {
			float angle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
			Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
			loc.scale(beam.getLength());
			Vector2f.add(loc, beam.getFrom(), loc);
			
			spawnMine(ship, loc);
			spawnedMine = true;
		} else if (beam.getDamageTarget() != null) {
			Vector2f arcTo = getNextArcLoc(engine, beam, perSpawn);
			float thickness = beam.getWidth();
			//thickness = 20;
			float dist = Misc.getDistance(arcFrom, arcTo);
			if (dist < SPAWN_SPACING * 2f) {
				EmpArcEntityAPI arc = engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), Color.white);
				arc.setCoreWidthOverride(Math.max(20f, thickness * 0.67f));
				//Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 1f, arc.getLocation(), arc.getVelocity());
			}
			spawnMine(ship, arcTo);
			spawnedMine = true;
			arcFrom = arcTo;
		}

		untilNextSpawn = SPAWN_INTERVAL;
		if (spawnedMine) {
			spawned++;
			if (spawned >= numToSpawn) {
				doneSpawningMines = true;
			}
		}
	}
	
	public void spawnNegativeParticles(CombatEngineAPI engine, BeamAPI beam) {
		float length = beam.getLengthPrevFrame();
		if (length <= 10f) return;
		
		//NEGATIVE_SOURCE_COLOR = new Color(200,255,200,25);
		
		Vector2f from = beam.getFrom();
		Vector2f to = beam.getRayEndPrevFrame();
		
		ShipAPI ship = beam.getSource();
		
		float angle = Misc.getAngleInDegrees(from, to);
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
//		Vector2f perp1 = new Vector2f(-dir.y, dir.x);
//		Vector2f perp2 = new Vector2f(dir.y, -dir.x);
		
		//Color color = new Color(150,255,150,25);
		Color color = NEGATIVE_SOURCE_COLOR;
		//color = Misc.setAlpha(color, 50);
		
		float sizeMult = 1f;
		sizeMult = 0.67f;
		
		for (int i = 0; i < 3; i++) {
			float rampUp = 0.25f + 0.25f * (float) Math.random();
			float dur = 1f + 1f * (float) Math.random();
			//dur *= 2f;
			float size = 200f + 50f * (float) Math.random();
			size *= sizeMult;
			//size *= 0.5f;
			//Vector2f loc = Misc.getPointWithinRadius(from, size * 0.5f);
			//Vector2f loc = Misc.getPointAtRadius(from, size * 0.33f);
			Vector2f loc = Misc.getPointAtRadius(beam.getWeapon().getLocation(), size * 0.33f);
			engine.addNegativeParticle(loc, ship.getVelocity(), size, rampUp / dur, dur, color);
			//engine.addNegativeNebulaParticle(loc, ship.getVelocity(), size, 2f, rampUp, 0f, dur, color);
		}
		
		if (true) return;
		
		// particles along the beam
		float spawnOtherParticleRange = 100;
		if (length > spawnOtherParticleRange * 2f && (float) Math.random() < 0.25f) {
			//color = new Color(150,255,150,255);		
			color = new Color(150,255,150,75);		
			int numToSpawn = (int) ((length - spawnOtherParticleRange) / 200f + 1);
			numToSpawn = 1;
			for (int i = 0; i < numToSpawn; i++) {
				float distAlongBeam = spawnOtherParticleRange + (length - spawnOtherParticleRange * 2f) * (float) Math.random();
				float groupSpeed = 100f + (float) Math.random() * 100f;
				for (int j = 0; j < 7; j++) {
					float rampUp = 0.25f + 0.25f * (float) Math.random();
					float dur = 1f + 1f * (float) Math.random();
					float size = 50f + 50f * (float) Math.random();
					Vector2f loc = new Vector2f(dir);
					float sign = Math.signum((float) Math.random() - 0.5f);
					loc.scale(distAlongBeam + sign * (float) Math.random() * size * 0.5f);
					Vector2f.add(loc, from, loc);
					
//					Vector2f off = new Vector2f(perp1);
//					if ((float) Math.random() < 0.5f) off = new Vector2f(perp2);
//					
//					off.scale(size * 0.1f);
					//Vector2f.add(loc, off, loc);
					
					loc = Misc.getPointWithinRadius(loc, size * 0.25f);
					
					float dist = Misc.getDistance(loc, to);
					Vector2f vel = new Vector2f(dir);
					if ((float) Math.random() < 0.5f) {
						vel.negate();
						dist = Misc.getDistance(loc, from);
					}
					
					float speed = groupSpeed;
					float maxSpeed = dist / dur;
					if (speed > maxSpeed) speed = maxSpeed;
					vel.scale(speed);
					Vector2f.add(vel, ship.getVelocity(), vel);
					
					engine.addNegativeParticle(loc, vel, size, rampUp, dur, color);
				}
			}
		}
	}
	
	
	public Vector2f getNextArcLoc(CombatEngineAPI engine, BeamAPI beam, float perSpawn) {
		CombatEntityAPI target = beam.getDamageTarget();
		float radiusOverride = -1f;
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (ship.getParentStation() != null && ship.getStationSlot() != null) {
				//radiusOverride = Misc.getDistance(ship.getLocation(), ship.getParentStation().getLocation());
				//radiusOverride += ship.getCollisionRadius();
				radiusOverride = Misc.getDistance(beam.getRayEndPrevFrame(), ship.getParentStation().getLocation()) + 0f;
				target = ship.getParentStation();
			}
		}
		
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
			if (spawnDir == 0) spawnDir = 1;
			
			boolean computeNextLoc = false;
			if (prevMineLoc != null) {
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
		if (radiusOverride >= 0) {
			targetRadius = radiusOverride;
		}
		
//		if (target instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) target;
//			targetLoc = ship.getShieldCenterEvenIfNoShield();
//			targetRadius = ship.getShieldRadiusEvenIfNoShield();
//		}
		
		boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getRayEndPrevFrame());
		if (hitShield) perSpawn *= 0.67f;
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
		if (radiusOverride >= 0) {
			actualRadius = radiusOverride;
		}
		if (!hitShield) {
			//actualRadius *= 1f + 0.1f * (float) Math.random();
			actualRadius += 30f + 50f * (float) Math.random();
		} else {
//			actualRadius = target.getShield().getRadius();
//			actualRadius += 20f + 20f * (float) Math.random();
			actualRadius += 30f + 50f * (float) Math.random();
		}
		
//		float angleDiff = Misc.getAngleDiff(beamSourceToTarget + 180f, angle);
//		if (angleDiff > 150f) {
//			actualRadius += perSpawn * (180f - angleDiff) / 30f;
//		}
		
		arcTo = Misc.getUnitVectorAtDegreeAngle(angle);
		arcTo.scale(actualRadius);
		Vector2f.add(targetLoc, arcTo, arcTo);
		
		
		// now we've got an arcTo location somewhere roughly circular; try to cleave more closely to the hull
		// if the target is a ship
		if (target instanceof ShipAPI && !hitShield) {
			ShipAPI ship = (ShipAPI) target;
			BoundsAPI bounds = ship.getExactBounds();
			if (bounds != null) {
				Vector2f best = null;
				float bestDist = Float.MAX_VALUE;
				for (SegmentAPI segment : bounds.getSegments()) {
					float test = Misc.getDistance(segment.getP1(), arcTo);
					if (test < bestDist) {
						bestDist = test;
						best = segment.getP1();
					}
				}
				if (best != null) {
					Object o = Global.getSettings().getWeaponSpec(RIFTCASCADE_MINELAYER).getProjectileSpec();
					if (o instanceof MissileSpecAPI) {
						MissileSpecAPI spec = (MissileSpecAPI) o;
						float explosionRadius = (float) spec.getBehaviorJSON().optJSONObject("explosionSpec").optDouble("coreRadius");
						float sizeMult = getSizeMult();
						explosionRadius *= sizeMult;
						
						Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(best, arcTo));
						dir.scale(explosionRadius * 0.9f);
						Vector2f.add(best, dir, dir);
						arcTo = dir;
						
//						dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(prevMineLoc, arcTo));
//						dir.scale(perSpawn);
//						Vector2f.add(prevMineLoc, dir, dir);
					}
				}
			}
			
		}
		
		return arcTo;
	}
	
	public float getSizeMult() {
		float sizeMult = 1f - spawned / (float) Math.max(1, numToSpawn - 1);
		sizeMult = 0.5f + 0.5f * sizeMult;
		return sizeMult;
	}
	
	public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
															  RIFTCASCADE_MINELAYER, 
															  mineLoc, 
															  (float) Math.random() * 360f, null);
		
		// "spawned" does not include this mine
		float sizeMult = getSizeMult();
		mine.setCustomData(RiftCascadeMineExplosion.SIZE_MULT_KEY, sizeMult);
			
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.MISSILE, false, mine.getDamage());
		}
		
		mine.getDamage().getModifier().modifyMult("mine_sizeMult", sizeMult);
		
		
		float fadeInTime = 0.05f;
		mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);
		
		//Global.getCombatEngine().addPlugin(createMissileJitterPlugin(mine, fadeInTime));
		
		//mine.setFlightTime((float) Math.random());
		float liveTime = 0f;
		//liveTime = 0.01f;
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		mine.addDamagedAlready(source);
		mine.setNoMineFFConcerns(true);
		
		prevMineLoc = mineLoc;
	}

}





