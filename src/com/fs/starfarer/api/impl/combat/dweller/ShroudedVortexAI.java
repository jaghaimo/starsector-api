package com.fs.starfarer.api.impl.combat.dweller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShroudedVortexAI implements ShipAIPlugin {

	public static class FlockingData {
		public Vector2f loc;
		public Vector2f vel;
		public float minA;
		public float maxA;
		public float minR;
		public float maxR;
		public float repelAtAngleDist;
		public float minC;
		public float maxC;
		public float attractWeight;
		public float repelWeight;
		public float cohesionWeight;
		public float facing;
	}	
	
	
	/**
	 * Loses hitpoints over time, when near zero, blows up.
	 */
	public static float HULL_FRACTION_LOST_PER_SECOND = 0.05f;
	
	public static String VORTEX_FLOCKING = "vortex_flocking";
	public static float ATTRACTOR_RANGE_MAX = 2000f;
	public static float COHESION_RANGE_MIN = 150f;
	public static float COHESION_RANGE_MAX = 300f;
	public static float REPEL_RANGE_MIN = 0f;
	public static float REPEL_RANGE_MAX = 400f;
	
	protected ShipwideAIFlags flags = new ShipwideAIFlags();
	protected ShipAPI ship;
	protected boolean exploded = false;
	protected Vector2f prevVel = null;
	
	protected IntervalUtil updateInterval = new IntervalUtil(0.5f, 1.5f);
	protected IntervalUtil headingInterval = new IntervalUtil(0.5f, 1.5f);
	
	protected ShipAPI target = null;
	protected float timeOnTarget = 0f;
	protected float numCollisions = 0f;
	
	protected List<FlockingData> flockingData = new ArrayList<>();
	protected float desiredHeading = 0f;
	
	public ShroudedVortexAI(ShipAPI ship) {
		this.ship = ship;
		
		doInitialSetup();
		
		updateInterval.forceIntervalElapsed();
	}
	
	protected void doInitialSetup() {
		ship.addTag(VORTEX_FLOCKING);
	}
	
	protected void toggleOn(int groupNum) {
		List<WeaponGroupAPI> groups = ship.getWeaponGroupsCopy();
		if (groups.size() <= groupNum) return;
		groups.get(groupNum).toggleOn();
	}
	protected void toggleOff(int groupNum) {
		List<WeaponGroupAPI> groups = ship.getWeaponGroupsCopy();
		if (groups.size() <= groupNum) return;
		groups.get(groupNum).toggleOff();
	}
	
	
	@Override
	public void advance(float amount) {
		//if (true) return;
		
		if (target != null) {
			timeOnTarget += amount;
		}
		
		updateInterval.advance(amount);
		if (updateInterval.intervalElapsed()) {
			ShipAPI prev = target;
			target = findTarget();
			if (prev != target) {
				timeOnTarget = 0f;
			}
			
			updateFlockingData();
		}
		
		headingInterval.advance(amount * 5f);
		if (headingInterval.intervalElapsed()) {
			computeDesiredHeading();
		}
		
//		String id = getClass().getSimpleName();
//		if (timeOnTarget > 3f) {
//			ship.getMutableStats().getAcceleration().modifyMult(id, 0.24f);
//			ship.getMutableStats().getDeceleration().modifyMult(id, 0.24f);
//		} else {
//			ship.getMutableStats().getAcceleration().unmodifyMult(id);
//			ship.getMutableStats().getDeceleration().unmodifyMult(id);
//		}
		
		if (prevVel != null) {
			float delta = Vector2f.sub(prevVel, ship.getVelocity(), new Vector2f()).length();
			// likely collision, stop the ship to make it feel heavier
			if (delta > ship.getMaxSpeedWithoutBoost() * 0.25f) {
				ship.getVelocity().scale(0.1f);
				numCollisions++;
			}
		}
		prevVel = new Vector2f(ship.getVelocity()); 

		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		if (shroud != null) {
			ShipAPI sourceShip = (ShipAPI) shroud.custom1;
			if (sourceShip != null) {
				float dist = Misc.getDistance(ship.getLocation(), sourceShip.getLocation());
				if (dist > (ship.getCollisionRadius() + sourceShip.getCollisionRadius() * 0.75f)) {
					ship.setCollisionClass(CollisionClass.SHIP);
				} else {
					ship.setCollisionClass(CollisionClass.FIGHTER);
				}
			}
		}
		
		
		float damage = ship.getMaxHitpoints() * HULL_FRACTION_LOST_PER_SECOND * (1f + numCollisions) * amount;
		ship.setHitpoints(ship.getHitpoints() - damage);
		if (ship.getHitpoints() <= 0f) {
			// like other damage, this will trigger the explosion visuals/damage in ShroudedVortexShipCreator
			engine.applyDamage(ship, ship.getLocation(), 10000f, DamageType.ENERGY, 0f, true, false, ship, false);
		}
		
		giveMovementCommands();
	}
	
	protected void giveMovementCommands() {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		//ship.giveCommand(ShipCommand.DECELERATE, null, 0);
		if (ship.hasTag(ShroudedVortexShipCreator.TAG_MIRRORED_VORTEX)) {
			ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
		} else {
			ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
		}

		float heading = Misc.getAngleInDegrees(ship.getVelocity());
		if (target != null) {
			float speed = (ship.getVelocity().length() + ship.getMaxSpeed()) * 0.5f;
			Vector2f point = engine.getAimPointWithLeadForAutofire(ship, 1f, target, speed);
			heading = Misc.getAngleInDegrees(ship.getLocation(), point);
		}
		
		//engine.headInDirectionWithoutTurning(ship, heading, 10000);
		
		engine.headInDirectionWithoutTurning(ship, desiredHeading, 10000);
	}
	
	
	@Override
	public ShipwideAIFlags getAIFlags() {
		return flags;
	}
	
	
	public ShipAPI findTarget() {
		float range = 5000f;
		float goodRange = ship.getHullLevel() / HULL_FRACTION_LOST_PER_SECOND * ship.getMaxSpeed() * 0.75f;
		Vector2f from = ship.getLocation();
		
		CombatEngineAPI engine = Global.getCombatEngine();
		Iterator<Object> iter = engine.getShipGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = ship.getOwner();
//		ShipAPI best = null;
//		float maxScore = -100000f;
		
		float currAngle = Misc.getAngleInDegrees(ship.getVelocity());
		
		WeightedRandomPicker<ShipAPI> good = new WeightedRandomPicker<>();
		WeightedRandomPicker<ShipAPI> lessGood = new WeightedRandomPicker<>();
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof ShipAPI)) continue;
			
			ShipAPI other = (ShipAPI) o;
			if (other == ship) continue;
			if (other.getOwner() == owner) continue;
			if (other.isHulk()) continue;
			if (other.isPhased()) continue;
			if (!engine.isAwareOf(owner, other))
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;
			

			float dist = Misc.getDistance(from, other.getLocation());
			if (dist > range) continue;
			
			float score = 1f;
			if (other.isFrigate()) {
				score = 11f;
			} else if (other.isDestroyer()) {
				score = 15f;
			} else if (other.isCruiser() || other.isCapital()) {
				score = 25f;
			}
			
			
			float angleToOther = Misc.getAngleInDegrees(ship.getLocation(), other.getLocation());
			float angleDiff = Misc.getAngleDiff(currAngle, angleToOther);
			
			float f = angleDiff / 90f;
			if (f > 1f) f = 1f; 
			
			float minus = 5f * dist / 5000f;
			if (minus > 5f) minus = 3f;
			
			score -= minus;
			
			score -= f * 5f;
			
			if (dist > goodRange) {
				lessGood.add(other, score);
			} else {
				good.add(other, score);
			}
//			if (dist > goodRange) {
//				score -= 100f;
//			}
//			
//			if (score > maxScore) {
//				maxScore = score;
//				best = other;
//			}
		}
		
		if (target != null) {
			if (good.getItems().contains(target)) return target;
			if (lessGood.getItems().contains(target) && good.isEmpty()) return target;
		}
		
		if (!good.isEmpty()) {
			return good.pick();
		}
		
		return lessGood.pick();
	}
	
	
	
	
	protected void updateFlockingData() {
		flockingData.clear();
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		int owner = ship.getOriginalOwner();
		Vector2f loc = ship.getLocation();
		float radius = ship.getCollisionRadius() * 1f;
		
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			FlockingData data = new FlockingData();
			data.facing = target.getFacing();
			data.loc = target.getLocation();
			data.vel = target.getVelocity();
			data.attractWeight = 100f;
			if (dist - ship.getCollisionRadius() - target.getCollisionRadius() < 500f) {
				data.attractWeight *= 10f;
			}
			data.repelWeight = 0f;
			data.minA = 0f;
			data.maxA = 1000000f;
			data.minR = 0f;
			data.maxR = 0f;
			data.repelAtAngleDist = 0f;
			flockingData.add(data);
		}
		
		for (ShipAPI curr : engine.getShips()) {
			if (curr == ship) continue;
			if (curr.getOwner() != owner) continue;
			if (curr.isHulk() || curr.getOwner() == 100) continue;
			
			float currRadius = curr.getCollisionRadius();
		
			if (curr.hasTag(VORTEX_FLOCKING)) {
				FlockingData data = new FlockingData();
				data.facing = Misc.getAngleInDegrees(curr.getVelocity());
				data.loc = curr.getLocation();
				data.vel = curr.getVelocity();
				data.repelWeight = 100f;
				data.cohesionWeight = 1f;
				data.attractWeight = 3f;
				
				data.minA = 0f + radius + currRadius;
				data.maxA = ATTRACTOR_RANGE_MAX + radius + currRadius;
				
				data.minR = REPEL_RANGE_MIN + radius + currRadius;
				data.maxR = REPEL_RANGE_MAX + radius + currRadius;
				
				data.minC = COHESION_RANGE_MIN + radius + currRadius;
				data.maxC = COHESION_RANGE_MAX + radius + currRadius;
	
				flockingData.add(data);
			} else if (!curr.isFighter()) {
				FlockingData data = new FlockingData();
				data.facing = Misc.getAngleInDegrees(curr.getVelocity());
				data.loc = curr.getLocation();
				data.vel = curr.getVelocity();
				data.attractWeight = 0f;
				data.cohesionWeight = 0f;
				data.repelWeight = 100f;
				
				data.minA = 0f;
				data.maxA = 0f;
				
				data.minR = REPEL_RANGE_MIN * 0.5f + radius + currRadius;
				data.maxR = REPEL_RANGE_MAX * 0.5f + radius + currRadius;
				
				data.minC = 0f;
				data.maxC = 0f;
	
				flockingData.add(data);
			}
		}
	}	
	
	protected void computeDesiredHeading() {
		
		Vector2f loc = ship.getLocation();
		Vector2f vel = ship.getVelocity();
		float facing = ship.getFacing();
		
		Vector2f total = new Vector2f();
		
		for (FlockingData curr : flockingData) {
			float dist = Misc.getDistance(curr.loc, loc);
			if (curr.maxR > 0 && dist < curr.maxR) {
				float repelWeight = curr.repelWeight;
				if (dist > curr.minR && curr.maxR > curr.minR) {
					repelWeight = (dist - curr.minR)  / (curr.maxR - curr.minR);
					if (repelWeight > 1f) repelWeight = 1f;
					repelWeight = 1f - repelWeight;
					repelWeight *= curr.repelWeight;
				}
				
				Vector2f dir = Misc.getUnitVector(curr.loc, loc);
				
				float distIntoRepel = curr.maxR - dist;
				float repelAdjustmentAngle = 0f;
				if (distIntoRepel < curr.repelAtAngleDist && curr.repelAtAngleDist > 0) {
					float repelMult = (1f - distIntoRepel / curr.repelAtAngleDist);
					repelAdjustmentAngle = 90f * repelMult;
					repelWeight *= (1f - repelMult);

					float repelAngle = Misc.getAngleInDegrees(dir);
					float turnDir = Misc.getClosestTurnDirection(dir, vel);
					repelAdjustmentAngle *= turnDir;
					dir = Misc.getUnitVectorAtDegreeAngle(repelAngle + repelAdjustmentAngle);
				}
				
				dir.scale(repelWeight);
				Vector2f.add(total, dir, total);
			}
			
			if (curr.maxA > 0 && dist < curr.maxA) {
				float attractWeight = curr.attractWeight;
				if (dist > curr.minA && curr.maxA > curr.minA) {
					attractWeight = (dist - curr.minA)  / (curr.maxA - curr.minA);
					if (attractWeight > 1f) attractWeight = 1f;
					attractWeight = 1f - attractWeight;
					attractWeight *= curr.attractWeight;
				}
				
				Vector2f dir = Misc.getUnitVector(loc, curr.loc);
				dir.scale(attractWeight);
				Vector2f.add(total, dir, total);
			}
			
			if (curr.maxC > 0 && dist < curr.maxC) {
				float cohesionWeight = curr.cohesionWeight;
				if (dist > curr.minC && curr.maxC > curr.minC) {
					cohesionWeight = (dist - curr.minC)  / (curr.maxC - curr.minC);
					if (cohesionWeight > 1f) cohesionWeight = 1f;
					cohesionWeight = 1f - cohesionWeight;
					cohesionWeight *= curr.cohesionWeight;
				}
				
				Vector2f dir = new Vector2f(curr.vel);
				Misc.normalise(dir);
				dir.scale(cohesionWeight);
				Vector2f.add(total, dir, total);
			}
		}
		
		if (total.length() <= 0) {
			desiredHeading = ship.getFacing();
		} else {
			desiredHeading = Misc.getAngleInDegrees(total);
		}
	}
	
	
	public void setDoNotFireDelay(float amount) {}
	public void forceCircumstanceEvaluation() {}
	public boolean needsRefit() { return false; }
	public void cancelCurrentManeuver() {}
	public ShipAIConfig getConfig() { return null; }
}













