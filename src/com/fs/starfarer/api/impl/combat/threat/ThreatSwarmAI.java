package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript.SwarmConstructionData;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ThreatSwarmAI implements ShipAIPlugin {

	public static float ATTRACTOR_RANGE_MAX_SAME_WING = 1000000f;
	public static float ATTRACTOR_RANGE_MAX = 500f;
	public static float COHESION_RANGE_MIN = 150f;
	public static float COHESION_RANGE_MAX = 300f;
	public static float REPEL_RANGE_MIN = 0f;
	public static float REPEL_RANGE_MAX = 150f;
	
	public static float MAX_TARGET_RANGE = 3000f;
	
	public static boolean isAttackSwarm(ShipAPI ship) {
		return ship != null && ship.getVariant().getHullVariantId().equals(SwarmLauncherEffect.ATTACK_SWARM_VARIANT);
	}
	public static boolean isConstructionSwarm(ShipAPI ship) {
		return ship != null && ship.getVariant().getHullVariantId().equals(SwarmLauncherEffect.CONSTRUCTION_SWARM_VARIANT);
	}
	public static boolean isReclamationSwarm(ShipAPI ship) {
		return ship != null && ship.getVariant().getHullVariantId().equals(SwarmLauncherEffect.RECLAMATION_SWARM_VARIANT);
	}
	
	public static class SharedSwarmWingData {
		public ShipAPI target = null;
	}
	
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
	
	public static float PROB_ENABLE_OTHER_GROUP = 0.5f;
	
	protected ShipwideAIFlags flags = new ShipwideAIFlags();
	protected ShipAPI ship;
	
	protected IntervalUtil updateInterval = new IntervalUtil(0.5f, 1.5f);
	protected IntervalUtil headingInterval = new IntervalUtil(0.5f, 1.5f);
	protected IntervalUtil attackRangeMultInterval = new IntervalUtil(0.2f, 1.8f);
	protected IntervalUtil reclamationReturnInterval = new IntervalUtil(0.2f, 1.8f);
	
	protected float sinceTurnedOffFlash = 0f;
	protected ShipAPI fabricator = null;
	
	protected List<FlockingData> flockingData = new ArrayList<>();
	protected float desiredHeading = 0f;
	protected float headingChangeRate = 0f;
	protected float elapsedSincePrevHeadingUpdate = 0f;
	protected float attackRangeMult = 1f;
	
	protected IntervalUtil enableOtherWeaponInterval = new IntervalUtil(5f, 15f);
	protected IntervalUtil priorityTargetPickerInterval = new IntervalUtil(1f, 3f);
	protected float enableOtherWeaponDuration = 0f;
	protected float elapsed = 0f;
	
	protected boolean startedConstruction = false;
	private SwarmConstructionData constructionData;
	private ThreatShipConstructionScript constructionScript;
	
	protected boolean attackSwarm = false;
	protected boolean constructionSwarm = false;
	protected boolean reclamationSwarm = false;
	
	public ThreatSwarmAI(ShipAPI ship) {
		this.ship = ship;
		
		attackSwarm = isAttackSwarm(ship);
		constructionSwarm = isConstructionSwarm(ship);
		reclamationSwarm = isReclamationSwarm(ship);
		
		doInitialSetup();
		
		updateInterval.forceIntervalElapsed();
		headingInterval.forceIntervalElapsed();
		attackRangeMultInterval.forceIntervalElapsed();
		priorityTargetPickerInterval.forceIntervalElapsed();
	}
	
	public SharedSwarmWingData getShared() {
		if (ship.getWing() == null) return new SharedSwarmWingData();
		
		String key = "SharedSwarmWingData";
		SharedSwarmWingData data = (SharedSwarmWingData) ship.getWing().getCustomData().get(key);
		if (data == null) {
			data = new SharedSwarmWingData();
			ship.getWing().getCustomData().put(key, data);
		}
		return data;
	}
	
	protected void doInitialSetup() {
		if (attackSwarm) {
			// 0: voltaic
			// 1: unstable
			// 2: kinetic
			// 3: seeker
			// 4: defabrication
			
			toggleOn(0);
			toggleOn(1);
			toggleOff(2);
			toggleOff(3);
			toggleOff(4);
			
			ship.giveCommand(ShipCommand.SELECT_GROUP, null, 6);
		}
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
	
	
	protected void advanceForSpecificSwarmType(float amount) {
		if (attackSwarm) {
			if (ship.isWingLeader()) {
				priorityTargetPickerInterval.advance(amount);
				if (priorityTargetPickerInterval.intervalElapsed()) {
					pickPriorityTarget();
				}
			}
			
			attackRangeMultInterval.advance(amount * 0.1f);
			if (attackRangeMultInterval.intervalElapsed()) {
				updateAttackRangeMult();
			}
			
			// 0: voltaic, always on
			// 1: unstable, always on
			// 2: kinetic
			// 3: seeker
			// 4: unused (was defab at some point)
			if (enableOtherWeaponDuration > 0) {
				enableOtherWeaponDuration -= amount;
				if (enableOtherWeaponDuration <= 0) {
					toggleOff(2);
					toggleOff(3);
					toggleOff(4);
				}
			} else {
				//amount *= 10f;
				boolean phaseMode = VoltaicDischargeOnFireEffect.isSwarmPhaseMode(ship);
				
				
				enableOtherWeaponInterval.advance(amount * 5f);
				if (enableOtherWeaponInterval.intervalElapsed()) {
					if ((float) Math.random() < PROB_ENABLE_OTHER_GROUP) {
						toggleOff(2);
						toggleOff(3);
						toggleOff(4);
						
						ShipAPI target = (ShipAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
						
//						boolean targetShieldsFacingUs = false;
//						if (target != null) {
//							ShieldAPI targetShield = target.getShield();
//							targetShieldsFacingUs = targetShield != null &&
//										targetShield.isOn() &&
//										Misc.isInArc(targetShield.getFacing(), Math.max(30f, targetShield.getActiveArc()),
//												target.getLocation(), ship.getLocation());
//						}
//						if (targetShieldsFacingUs) {
//							toggleOn(2);
//						} else {
//							toggleOn(3);
//						}
						
						
						// use Seeker only when it will be destroyed by using seeker, as a "final attack"
						boolean useSeeker = ship.getHullLevel() < 0.22f;
						boolean useKinetic = true;
						if (target == null || target.isFighter()) {
							useSeeker = false;
							useKinetic = false;
						}
						
						if (useSeeker) {
							toggleOn(3);
						} else if (useKinetic) {
							toggleOn(2);
						}
						
						enableOtherWeaponDuration = 0.5f + 0.5f * (float) Math.random();
					}
				}
			}
		}
		
		if (constructionSwarm) {
			if (constructionData == null) {
				RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
				if (swarm != null) {
					constructionData = (SwarmConstructionData) swarm.custom1;
				}
			}
			if (constructionData != null) {
				if (elapsed > constructionData.preConstructionTravelTime && !startedConstruction) {
					startedConstruction = true;
					RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
					if (swarm != null) {
						constructionScript = new ThreatShipConstructionScript(
											constructionData.variantId, ship, 0f, constructionData.constructionTime);
						Global.getCombatEngine().addPlugin(constructionScript);
					}
				}
			}
		}
		
		if (reclamationSwarm) {
			if (fabricator == null) {
				reclamationReturnInterval.advance(amount);
				if (reclamationReturnInterval.intervalElapsed()) {
					int owner = ship.getOriginalOwner();
					CombatEngineAPI engine = Global.getCombatEngine();
					for (ShipAPI curr : engine.getShips()) {
						if (curr == ship || curr.getOwner() != owner) continue;
						if (curr.isHulk() || curr.getOwner() == 100) continue;
						if (!ThreatCombatStrategyAI.isFabricator(curr)) continue;
						
						float dist = Misc.getDistance(curr.getLocation(), ship.getLocation());
						if (dist < curr.getCollisionRadius() + 200f) {
							// turn off flash and return to fabricator
							RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
							swarm.params.flashFrequency = 0f;
							swarm.params.flashProbability = 0f;
							
							fabricator = curr;
							break;
						}
					}
				}				
			} else {
				sinceTurnedOffFlash += amount;
				if (sinceTurnedOffFlash > 3f) {
					CombatEngineAPI engine = Global.getCombatEngine();
					if (fabricator.isAlive()) {
						fabricator.setCurrentCR(Math.min(1f, fabricator.getCurrentCR() + 0.01f * ship.getHullLevel()));
						
						RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
						RoilingSwarmEffect swarmFabricator = RoilingSwarmEffect.getSwarmFor(fabricator);
						if (swarm != null && swarmFabricator != null) {
							swarm.transferMembersTo(swarmFabricator, swarm.getNumActiveMembers());
						}
					}
					ship.setHitpoints(0f);
					ship.setSpawnDebris(false);
					engine.applyDamage(ship, ship.getLocation(), 100f, DamageType.ENERGY, 0f, true, false, ship, false);
				}
			}
		}
	}
	
	protected void pickPriorityTarget() {
		SharedSwarmWingData data = getShared();
		if (data.target != null && data.target.isAlive()) {
			return;
		}
		
		WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();
		CombatEngineAPI engine = Global.getCombatEngine();
		int owner = ship.getOriginalOwner();
		
		for (ShipAPI curr : engine.getShips()) {
			if (curr == ship) continue;
			if (curr.isFighter()) continue;
			if (curr.isHulk() || curr.getOwner() == 100) continue;
			
			if (curr.getOwner() != owner && engine.isAwareOf(owner, curr)) {
				float weight = getShipWeight(curr);
				if (curr.isFrigate()) {
					weight *= 0.0001f;
				}
				picker.add(curr, weight);
			} 
		}
		
		data.target = picker.pick();
	}
	protected void updateAttackRangeMult() {
		//attackRangeMult = 0.75f + 0.5f * (float) Math.random();
		attackRangeMult = 0.5f + 1f * (float) Math.random();
	}

	@Override
	public void advance(float amount) {
		//if (true) return;
		
		elapsed += amount;
		advanceForSpecificSwarmType(amount);
		
		updateInterval.advance(amount);
		if (updateInterval.intervalElapsed()) {
			updateFlockingData();
		}
		
		headingInterval.advance(amount * 5f);
		if (headingInterval.intervalElapsed()) {
			computeDesiredHeading();
			elapsedSincePrevHeadingUpdate = 0f;
		}
		
		giveMovementCommands();
		
		elapsedSincePrevHeadingUpdate += amount;
	}
	
	protected void giveMovementCommands() {
		if (constructionScript != null && constructionScript.getShip() != null) {
			ship.giveCommand(ShipCommand.DECELERATE, null, 0);
			return;
		}
		
		String source = "swarm_wingman_catch_up_speed_bonus";
		MutableShipStatsAPI stats = ship.getMutableStats();
		if (ship.isWingLeader() || ship.getWingLeader() == null) {
			stats.getMaxSpeed().unmodifyMult(source);
			stats.getAcceleration().unmodifyMult(source);
			stats.getDeceleration().unmodifyMult(source);
		} else {
			ShipAPI leader = ship.getWingLeader();
			float dist = Misc.getDistance(ship.getLocation(), leader.getLocation());
			float mult = (dist - COHESION_RANGE_MAX * 0.5f - 
					ship.getCollisionRadius() * 0.5f - leader.getCollisionRadius() * 0.5f) / COHESION_RANGE_MAX;
			if (mult < 0f) mult = 0f;
			if (mult > 1f) mult = 1f;
			stats.getMaxSpeed().modifyMult(source, 1f + .25f * mult);
			stats.getAcceleration().modifyMult(source, 1f + 0.5f * mult);
			stats.getDeceleration().modifyMult(source, 1f + 0.5f * mult);
		}
		
		float useHeading = desiredHeading;
		//useHeading += headingChangeRate * elapsedSincePrevHeadingUpdate;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		engine.headInDirectionWithoutTurning(ship, useHeading, 10000);
		Misc.turnTowardsFacingV2(ship, useHeading, 0f);
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
			headingChangeRate = ship.getAngularVelocity() * 0.5f;
		} else {
//			Vector2f currDir = new Vector2f(vel);
//			Misc.normalise(currDir);
//			currDir.scale(total.length() * 0.25f);
//			Vector2f.add(total, currDir, total);
			
			float prev = desiredHeading;
			desiredHeading = Misc.getAngleInDegrees(total);
			if (elapsedSincePrevHeadingUpdate > 0) {
				headingChangeRate = Misc.getAngleDiff(prev, desiredHeading) / elapsedSincePrevHeadingUpdate;
			} else {
				headingChangeRate = ship.getAngularVelocity() * 0.5f;
			}
		}
	}
	
	
	protected void updateFlockingData() {
		flockingData.clear();
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		if (constructionScript != null && constructionScript.getShip() != null) {
			return;
		}
		
		
		int owner = ship.getOriginalOwner();
		FighterWingAPI wing = ship.getWing();
		if (wing == null) return;
		
		Vector2f loc = ship.getLocation();
		boolean wingLeader = ship.isWingLeader();
		
		float attackRange = wing.getSpec().getAttackRunRange();
		attackRange *= attackRangeMult;
		float radius = ship.getCollisionRadius() * 0.5f;
		
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm != null) { // && (constructionSwarm) {
			radius = swarm.params.maxOffset;
		}
		
		SharedSwarmWingData shared = getShared();
		
		ShipAPI target = null;
		float targetDist = Float.MAX_VALUE;
		
		for (ShipAPI curr : engine.getShips()) {
			if (curr == ship) continue;
			if (curr.isFighter() && (!curr.isWingLeader() || curr.getOwner() == owner || !attackSwarm)) continue;
			
			// just avoid everything - looking for a clear area
			if (constructionSwarm) {
				float currRadius = curr.getCollisionRadius() * 2f;
				FlockingData data = new FlockingData();
				data.facing = curr.getFacing();
				data.loc = curr.getLocation();
				data.vel = curr.getVelocity();
				data.attractWeight = 0f;
				data.repelWeight = getShipWeight(curr) * 1f;
				data.minA = 0f;
				data.maxA = 0f;
				data.minR = radius + currRadius;
				data.maxR = radius + currRadius + Math.min(100f, currRadius * 1f);
				data.repelAtAngleDist = (data.maxR - data.minR) * 0.5f;
				flockingData.add(data);
				continue;
			}
			
			if (curr.isHulk() || curr.getOwner() == 100) continue;
			
			// return to Fabricator Units, ignore other ships
			if (reclamationSwarm) {
				if (!ThreatCombatStrategyAI.isFabricator(curr) || curr.getOwner() != owner) continue;
				float currRadius = curr.getCollisionRadius() * 0.5f;
				FlockingData data = new FlockingData();
				data.facing = curr.getFacing();
				data.loc = curr.getLocation();
				data.vel = curr.getVelocity();
				data.attractWeight = getShipWeight(curr) * (1f - ship.getCurrentCR());
				data.repelWeight = data.attractWeight * 10f;
				data.minA = radius + currRadius;
				data.maxA = 1000000f;
				data.minR = radius + currRadius;
				data.maxR = radius + currRadius + 100f;
				data.repelAtAngleDist = (data.maxR - data.minR) * 0.5f;
				flockingData.add(data);
				continue;
			}
			
			
			float currRadius = curr.getCollisionRadius() * 0.5f;
			
			if (curr.getOwner() != owner && engine.isAwareOf(owner, curr)) {
				FlockingData data = new FlockingData();
				data.facing = curr.getFacing();
				data.loc = curr.getLocation();
				data.vel = curr.getVelocity();
				data.attractWeight = getShipWeight(curr);
				data.repelWeight = data.attractWeight * 10f;
				data.minA = attackRange + radius + currRadius;
				data.maxA = 1000000f;
				data.repelAtAngleDist = Math.min(attackRange * 0.5f, 400f);
				data.minR = radius + currRadius;
				data.maxR = attackRange + radius + currRadius;
				if (curr == shared.target) {
					//boolean inFront = Misc.isInArc(curr.getFacing(), 90f, curr.getLocation(), ship.getLocation());
					float angleDiffFromFront = Misc.getAngleDiff(curr.getFacing(), Misc.getAngleInDegrees(curr.getLocation(), ship.getLocation()));
					float maxDiff = 45f;
					if (angleDiffFromFront < maxDiff) {
//						data.minR *= 2f - angleDiffFromFront / maxDiff;
//						data.maxR *= 2f - angleDiffFromFront / maxDiff;
						data.minR = data.minR + (data.maxR - data.minR) * 0.5f;
						data.minR += 500f * (1f - angleDiffFromFront / maxDiff);
						data.maxR += 500f * (1f - angleDiffFromFront / maxDiff);
						data.repelAtAngleDist += 500f * (1f - angleDiffFromFront / maxDiff);
					}
					data.attractWeight += 200f;
					data.repelWeight += 600f;
				}
				flockingData.add(data);
				
				float dist = Misc.getDistance(loc, curr.getLocation());
				if (dist < targetDist && dist < MAX_TARGET_RANGE) {
					target = curr;
					targetDist = dist;
				}
				
				// add extra attractor behind the enemy ship to encourage going around and not hanging out in one spot
				if ((curr.isDestroyer() || curr.isCruiser() || curr.isCapital()) && curr == shared.target) {
					data = new FlockingData();
					//Vector2f dir = Misc.getUnitVector(ship.getLocation(), curr.getLocation());
					Vector2f dir = Misc.getUnitVectorAtDegreeAngle(curr.getFacing() + 180f);
					dir.scale(curr.getCollisionRadius() * 0.5f + attackRange * attackRangeMult);
					data.facing = curr.getFacing();
					data.loc = Vector2f.add(curr.getLocation(), dir, new Vector2f());
					data.vel = curr.getVelocity();
					data.attractWeight = getShipWeight(curr) * 1f;
					data.minA = attackRange + radius + currRadius;
					data.maxA = 1000000f;
					if (curr == shared.target) {
						data.attractWeight += 200f;
					}
					flockingData.add(data);
				}
				
				
			} else if (curr.getOwner() == owner) {
				FlockingData data = new FlockingData();
				data.facing = curr.getFacing();
				data.loc = curr.getLocation();
				data.vel = curr.getVelocity();
				data.attractWeight = getShipWeight(curr) * 0.1f;
				data.repelWeight = data.attractWeight * 50f;
				data.minA = attackRange + radius + currRadius;
				data.maxA = 1000000f;
				data.minR = radius + currRadius;
				data.maxR = attackRange * 0.75f + radius + currRadius;
				data.repelAtAngleDist = Math.min(attackRange * 0.5f, 400f);
				flockingData.add(data);
			}
		}
		
		if (target != null) {
			flags.setFlag(AIFlags.MANEUVER_TARGET, 3f, target);
		} else {
			flags.unsetFlag(AIFlags.MANEUVER_TARGET);
		}
		
		if (flockingData.isEmpty() && !constructionSwarm) {
			FlockingData data = new FlockingData();
			data.facing = 0f;
			data.loc = new Vector2f();
			data.vel = new Vector2f();
			data.attractWeight = 5f;
			data.repelWeight = data.attractWeight * 10f;
			data.minA = 1000f;
			data.maxA = 1000000f;
			data.minR = 1000f;
			data.maxR = 3000f;
			data.repelAtAngleDist = 1000f;
			flockingData.add(data);
		}
		
//		if (true) {
//			FlockingData data = new FlockingData();
//			data.facing = 0f;
//			data.loc = new Vector2f(8000f, -18000f);
//			data.vel = new Vector2f();
//			data.attractWeight = 1000f;
//			data.minA = 1000f;
//			data.maxA = 1000000f;
//			flockingData.add(data);
//		}
		
		//RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm != null && swarm.params.flockingClass != null && swarm.attachedTo != null) {
			for (RoilingSwarmEffect curr : RoilingSwarmEffect.getFlockingMap().getList(swarm.params.flockingClass)) {
				if (curr == swarm) continue;
				if (curr.attachedTo == ship || curr.attachedTo == null ||
						curr.attachedTo.getOwner() != owner) {
					continue;
				}
				
				if (swarm.params.flockingClass.equals(curr.params.flockingClass)) {
					// avoid other construction swarms - looking for a clear area
					if (constructionSwarm) {
						float currRadius = curr.params.maxOffset;
						FlockingData data = new FlockingData();
						data.facing = curr.attachedTo.getFacing();
						data.loc = curr.attachedTo.getLocation();
						data.vel = curr.attachedTo.getVelocity();
						data.attractWeight = 0f;
						data.repelWeight = 8f;
						data.minA = 0f;
						data.maxA = 0f;
						data.minR = radius + currRadius;
						data.maxR = radius + currRadius + Math.min(100f, currRadius * 1f);
						data.repelAtAngleDist = (data.maxR - data.minR) * 0.5f;
						flockingData.add(data);
						continue;
					}
					
					
					boolean sameWing = wing == ((ShipAPI)curr.attachedTo).getWing();
					boolean otherWingLeader = ((ShipAPI)curr.attachedTo).isWingLeader();
					
					// actually - make the leader wait a bit, otherwise they never catch up
					// or not
					if (wingLeader && sameWing) continue; // others catch up/line up on leader
					
					if (!sameWing) {
						float dist = Misc.getDistance(loc, curr.attachedTo.getLocation());
						if (dist > ATTRACTOR_RANGE_MAX + 500f) continue;
					}
					
					
					float currRadius = curr.attachedTo.getCollisionRadius() * 0.5f;
					FlockingData data = new FlockingData();
					data.facing = curr.attachedTo.getFacing();
					data.loc = curr.attachedTo.getLocation();
					data.vel = curr.attachedTo.getVelocity();
					data.attractWeight = 1f;
					data.repelWeight = 10f;
					data.cohesionWeight = 1f;
					if (sameWing) {
						if (wingLeader) {
							data.attractWeight = 0.1f;
						} else {
							data.attractWeight = 3f;
						}
						data.minA = 0f + radius + currRadius;
						data.maxA = ATTRACTOR_RANGE_MAX_SAME_WING + radius + currRadius;
					} else {
						data.minA = 0f + radius + currRadius;
						data.maxA = ATTRACTOR_RANGE_MAX + radius + currRadius;
					}
					data.minR = REPEL_RANGE_MIN + radius + currRadius;
					data.maxR = REPEL_RANGE_MAX + radius + currRadius;
					if (wingLeader && otherWingLeader) {
						data.maxR = ATTRACTOR_RANGE_MAX + radius + currRadius;
					}
					data.minC = COHESION_RANGE_MIN + radius + currRadius;
					data.maxC = COHESION_RANGE_MAX + radius + currRadius;
					if (reclamationSwarm) {
						data.minR *= 0.33f;
						data.maxR *= 0.33f;
					}
					flockingData.add(data);
				}
			}
		}
	}
	
	
	
	
	
	
	
	@Override
	public ShipwideAIFlags getAIFlags() {
		return flags;
	}
	
	public static float getShipWeight(ShipAPI ship) {
		return getShipWeight(ship, true);
	}
	public static float getShipWeight(ShipAPI ship, boolean adjustForNonCombat) {
		boolean nonCombat = ship.isNonCombat(false);
		float weight = 0;
		switch (ship.getHullSize()) {
		case CAPITAL_SHIP: weight += 8; break;
		case CRUISER: weight += 4; break;
		case DESTROYER: weight += 2; break;
		case FRIGATE: weight += 1; break;
		case FIGHTER: weight += 1; break;
		}
		if (nonCombat && adjustForNonCombat) weight *= 0.25f;
		if (ship.isDrone()) weight *= 0.1f;
		return weight;
	}

	public void setDoNotFireDelay(float amount) {}
	public void forceCircumstanceEvaluation() {}
	public boolean needsRefit() { return false; }
	public void cancelCurrentManeuver() {}
	public ShipAIConfig getConfig() { return null; }
}













