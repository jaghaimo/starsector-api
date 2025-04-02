package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AssignmentTargetAPI;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * Doesn't care about command points etc, just functions in a different way. May use command points/tasks/etc
 * to accomplish its goals, just as an implementation detail, but conceptually it's fundamentally different from how
 * human-type fleets work.
 * 
 * @author Alex
 *
 */
public class ThreatCombatStrategyAI {
	
	public static float SND_BASE = 60f;
	public static float SND_TIMER = 60f;
	public static float SND_FRACTION = 0.5f;
	
	protected boolean playerSide;
	protected CombatTaskManagerAPI taskManager;
	protected CombatFleetManagerAPI fleetManager;
	protected CombatFleetManagerAPI enemyFleetManager;
	protected int owner;
	protected boolean allyMode = false;
	
	protected IntervalUtil everySecond = new IntervalUtil(0.8f, 1.2f);
	protected CombatEngineAPI engine;
	protected float mw, mh;
	
	protected boolean abort = false;
	
	protected AssignmentInfo mainDefend1;
	protected AssignmentInfo mainDefend2;
	
	protected float captureAllTimeRemaining; 
	protected boolean gaveInitialOrders = false;
	
	protected float untilSNDOnSkirmishUnits; 
	
	
	public ThreatCombatStrategyAI(int owner) {
		engine = Global.getCombatEngine();
		this.owner = owner;
		playerSide = owner == 0;
		allyMode = playerSide;
		//allyMode = false;
		fleetManager = engine.getFleetManager(owner);
		taskManager = fleetManager.getTaskManager(allyMode);
		taskManager.getCommandPointsStat().modifyFlat("ThreatCombatStrategyAI", 1000000000);
		
		enemyFleetManager = engine.getFleetManager(owner == 0 ? 1 : 0);
		
		if (fleetManager.getGoal() == FleetGoal.ESCAPE || enemyFleetManager.getGoal() == FleetGoal.ESCAPE) {
			abort = true;
		} else {
			if (fleetManager.getAdmiralAI() != null) {
				taskManager.clearTasks();
				fleetManager.getAdmiralAI().setNoOrders(true);
			}
		}
		
		mw = engine.getMapWidth();
		mh = engine.getMapHeight();
		
		resetSNDTimer();
	}
	
	protected void resetSNDTimer() {
		untilSNDOnSkirmishUnits = SND_TIMER * (0.75f + (float) Math.random() * 0.5f); 
		untilSNDOnSkirmishUnits += SND_BASE; 
	}
	
	protected void manageSND(float amount) {
		untilSNDOnSkirmishUnits -= amount;
		if (captureAllTimeRemaining > 0) return;
		if (untilSNDOnSkirmishUnits <= 0) {
			for (DeployedFleetMemberAPI member : fleetManager.getDeployedCopyDFM()) {
				ShipAPI ship = member.getShip();
				if (ship == null || ship.getAI() == null) continue;
				if (ship.hasTag(ThreatShipConstructionScript.SHIP_UNDER_CONSTRUCTION)) continue;
				
				if (!ship.getHullSpec().hasTag(Tags.THREAT_SKIRMISH)) continue;
				if ((float) Math.random() > SND_FRACTION) continue;
				
				cancelOrders(member, false);
				ship.getAIFlags().setFlag(AIFlags.IGNORES_ORDERS, SND_BASE * (0.75f + (float) Math.random() * 0.5f));
			}
			resetSNDTimer();
		}
	}
	
	protected void giveInitialOrders() {
		captureAllTimeRemaining = 80f;
		for (BattleObjectiveAPI curr : engine.getObjectives()) {
			taskManager.createAssignment(CombatAssignmentType.CAPTURE, curr, false);
		}		
	}
	
	
	public void advance(float amount) {
		//if (true) return;
		if (abort) return;
		if (engine.isPaused()) return;
		
		captureAllTimeRemaining -= amount;
		
		manageSND(amount);
		
		everySecond.advance(amount);
		if (everySecond.intervalElapsed()) {
			// if non-threat ships are deployed from this fleetManager, don't want to be doing any Threat things
			List<DeployedFleetMemberAPI> deployed = fleetManager.getDeployedCopyDFM();
			if (deployed.isEmpty()) return;
			
			boolean someMatching = false;
			for (DeployedFleetMemberAPI member : deployed) {
				if (!member.isFighterWing() && member.getShip() != null &&
						member.isAlly() == allyMode &&
						!member.getShip().getVariant().hasHullMod(HullMods.THREAT_HULLMOD)) {
					abort = true;
					return;
				} else if (!member.isFighterWing() && member.getShip() != null &&
						member.isAlly() == allyMode) {
					someMatching = true;
				}
			}
			
			if (!someMatching) return;
			
			if (!gaveInitialOrders) {
				giveInitialOrders();
				gaveInitialOrders = true;
			}
			
			float sign = 1f;
			if (owner == 0) sign = -1;
			Vector2f enemyCom = getEnemyCenterOfMass();
			Vector2f fabricatorLoc = new Vector2f(0, 0 + mh * 0.33f * sign);
			Vector2f axis = Misc.getUnitVector(fabricatorLoc, enemyCom);
			Vector2f perp = new Vector2f(axis.y, -axis.x);
			float distToEnemyCom = Misc.getDistance(fabricatorLoc, enemyCom);
			
			//float hiveOffset = 2000f;
			float hiveOffset = distToEnemyCom - 6000f;
			if (Math.abs(hiveOffset) < 2000f) {
				hiveOffset = Math.signum(hiveOffset) * 2000f;
			}
			if (hiveOffset > 2000) hiveOffset= 2000f;
			Vector2f hiveLoc = new Vector2f(axis);
			hiveLoc.scale(hiveOffset);
			Vector2f.add(hiveLoc, fabricatorLoc, hiveLoc);

			float enemyWeightNearFabricatorLoc = Misc.countEnemyWeightInArcAroundLocation(
					owner, fabricatorLoc, 0f, 360f, 3000f, null, true, true);
			
			int fabricators = 0;
			int hives = 0;
			for (DeployedFleetMemberAPI member : fleetManager.getDeployedCopyDFM()) {
				ShipAPI ship = member.getShip();
				if (ship == null || ship.getAI() == null) continue;
				if (isFabricator(ship)) {
					fabricators++;
				}
				if (isHive(ship)) {
					hives++;
				}
			}
			
			float defDist = distToEnemyCom - 2000f;
			if (enemyWeightNearFabricatorLoc >= 3f) {
				defDist = Math.min(defDist, 3000f);
			}
			if (fabricators == 0 && hives == 0) {
				if (mainDefend1 != null) {
					taskManager.removeAssignment(mainDefend1);
					mainDefend1 = null;
				}
				if (mainDefend2 != null) {
					taskManager.removeAssignment(mainDefend2);
					mainDefend2 = null;
				}
			} else if (defDist < 2000f) {
				if (mainDefend1 != null) {
					float dist = Misc.getDistance(mainDefend1.getTarget().getLocation(), fabricatorLoc);
					if (dist > 1000f) {
						taskManager.removeAssignment(mainDefend1);
						mainDefend1 = null;
					}
				}

				if (mainDefend2 != null) {
					taskManager.removeAssignment(mainDefend2);
					mainDefend2 = null;
				}
				
				if (mainDefend1 == null) {
					AssignmentTargetAPI wp = taskManager.createWaypoint2(fabricatorLoc, allyMode);
					mainDefend1 = taskManager.createAssignment(CombatAssignmentType.DEFEND, wp, false);
				}
				
				//Global.getCombatEngine().getCombatUI().addMessage(0, "Threat aggro mode engaged!");
				// enemies close to fabricators - attack! 
				for (DeployedFleetMemberAPI member : fleetManager.getDeployedCopyDFM()) {
					ShipAPI ship = member.getShip();
					if (ship == null || ship.getAI() == null) continue;
					if (isCombatUnit(ship) && ship.getAI() instanceof ShipAIPlugin) {
//						ShipAIPlugin ai = (ShipAIPlugin) ship.getAI();
//						ShipAIConfig config = ai.getConfig();
//						config.personalityOverride = Personalities.RECKLESS;
//						config.alwaysStrafeOffensively = true;
//						config.backingOffWhileNotVentingAllowed = false;
//						config.turnToFaceWithUndamagedArmor = false;
//						config.burnDriveIgnoreEnemies = true;
						ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 2f);
						ship.getAIFlags().setFlag(AIFlags.DO_NOT_VENT, 2f);
						ship.getAIFlags().setFlag(AIFlags.IGNORES_ORDERS, 2f);
					}
				}
			} else {
				Vector2f defLoc = new Vector2f(axis);
				defLoc.scale(defDist);
				Vector2f.add(defLoc, fabricatorLoc, defLoc);
				
				Vector2f defLoc1 = new Vector2f(perp);
				defLoc1.scale(1000f);
				Vector2f.add(defLoc1, defLoc, defLoc1);
				
				Vector2f defLoc2 = new Vector2f(perp);
				defLoc2.scale(-1000f);
				Vector2f.add(defLoc2, defLoc, defLoc2);
				
				
				if (mainDefend1 != null) {
					float dist = Misc.getDistance(mainDefend1.getTarget().getLocation(), defLoc1);
					if (dist > 1000f) {
						taskManager.removeAssignment(mainDefend1);
						mainDefend1 = null;
					}
				}
				if (mainDefend2 != null) {
					float dist = Misc.getDistance(mainDefend2.getTarget().getLocation(), defLoc2);
					if (dist > 1000f) {
						taskManager.removeAssignment(mainDefend2);
						mainDefend2 = null;
					}
				}
				
				if (mainDefend1 == null) {
					AssignmentTargetAPI wp = taskManager.createWaypoint2(defLoc1, allyMode);
					mainDefend1 = taskManager.createAssignment(CombatAssignmentType.DEFEND, wp, false);
				}
				
				if (mainDefend2 == null) {
					AssignmentTargetAPI wp = taskManager.createWaypoint2(defLoc2, allyMode);
					mainDefend2 = taskManager.createAssignment(CombatAssignmentType.DEFEND, wp, false);
				}
			}
			
			if (captureAllTimeRemaining <= 0f) {
				float axisAngle = Misc.getAngleInDegrees(axis);
				List<AssignmentTargetAPI> withCaptures = new ArrayList<>();
				for (AssignmentInfo info : taskManager.getAllAssignments()) {
					if (info.getTarget() == null) continue;
					if (info.getType() == CombatAssignmentType.CAPTURE || info.getType() == CombatAssignmentType.CONTROL) {
						if ((fabricators == 0 && hives == 0) ||
								!wantsToControl(fabricatorLoc, axisAngle, distToEnemyCom, info.getTarget().getLocation())) {
							taskManager.removeAssignment(info);
						} else {
							withCaptures.add(info.getTarget());
						}
					}
				}
				
				if (fabricators > 0 || hives > 0) {
					for (BattleObjectiveAPI curr : engine.getObjectives()) {
						if (withCaptures.contains(curr)) continue;
						if (wantsToControl(fabricatorLoc, axisAngle, distToEnemyCom, curr.getLocation())) {
							taskManager.createAssignment(CombatAssignmentType.CAPTURE, curr, false);
						}
					}
				}
			}
			
			Set<DeployedFleetMemberAPI> escorted = new LinkedHashSet<>();
			
			for (DeployedFleetMemberAPI member : fleetManager.getDeployedCopyDFM()) {
				ShipAPI ship = member.getShip();
				if (ship == null || ship.getAI() == null) continue;
				if (ship.hasTag(ThreatShipConstructionScript.SHIP_UNDER_CONSTRUCTION)) continue;
				
				float enemyCheckRange = 3000f;
				if (isHive(ship)) {
					enemyCheckRange = 1000f;
				}
				
				float enemyWeight = Misc.countEnemyWeightInArcAroundLocation(owner, ship.getLocation(), 0f, 360f, enemyCheckRange, null, true, true);
				float shipWeight = Misc.getShipWeight(ship, true);
				
				boolean enemiesNear = enemyWeight >= shipWeight * 0.5f;
				
				if (isFabricator(ship)) {
					float min = 0f;
					float max = 1000f;
					if (enemiesNear) {
						min = 1000f;
						max = 2000f;
					}
					giveMovementOrder(member, fabricatorLoc, min, max);
				} else if (isHive(ship)) {
					if (enemiesNear) {
						cancelOrders(member, true);
					} else {
						giveMovementOrder(member, hiveLoc, 1000f, 1500f);
					}
				} else if (isOverseer(ship)) {
					DeployedFleetMemberAPI closest = null;
					float minDist = Float.MAX_VALUE;
					for (DeployedFleetMemberAPI other : fleetManager.getDeployedCopyDFM()) {
						if (other == member || other.getShip() == null || escorted.contains(other)) continue;
						
						float extraDistScore = 0f;
						if (other.getShip().isCruiser() && isCombatUnit(other.getShip())) {
							extraDistScore = 0f;
						} else if (other.getShip().isDestroyer() && isCombatUnit(other.getShip())) {
							extraDistScore = 100000f;
						} else if (other.getShip().isFrigate() && isCombatUnit(other.getShip())) {
							if (enemiesNear) {
								extraDistScore = 500000f;
							} else {
								extraDistScore = 1000000f;
							}
						} else if (isHive(other.getShip())) {
							if (enemiesNear) {
								extraDistScore = 100000f;
							} else {
								extraDistScore = 10000000f;
							}
						} else {
							continue;
						}
						float dist = Misc.getDistance(member.getLocation(), other.getLocation()) + extraDistScore;
						if (dist < minDist) {
							closest = other;
							minDist = dist;
						}
					}
					if (closest != null) {
						member.getShip().getAIFlags().setFlag(AIFlags.TIMID_ESCORT, 2f);
						member.getShip().getAIFlags().setFlag(AIFlags.ESCORT_RANGE_MODIFIER, 2f, 300f);
						escort(member, closest);
					}
				}
				
			}

			cleanUpEmptyAssignments();
		}
	}
	
	protected boolean wantsToControl(Vector2f fabricatorLoc, float axisAngle, float distToEnemyCom, Vector2f objectiveLoc) {
		float dist = Misc.getDistance(fabricatorLoc, objectiveLoc);
		float angle = Misc.getAngleInDegrees(fabricatorLoc, objectiveLoc);
		float angleDiff = Misc.getAngleDiff(axisAngle, angle);
		
		float enemyWeight = Misc.countEnemyWeightInArcAroundLocation(owner, objectiveLoc, 0f, 360f, 3000f, null, true, true);
		if (enemyWeight <= 0f && angleDiff > 30f) return true;
		//return !(dist > 2000 && (dist > distToEnemyCom || angleDiff > 45f));
		return dist < 5000 || (dist < distToEnemyCom && angleDiff < 45f);
	}
	
	
	protected void cancelOrders(DeployedFleetMemberAPI member, boolean withSearchAndDestroy) {
		AssignmentInfo curr = taskManager.getAssignmentFor(member.getShip());
		if (curr != null) {
			taskManager.removeAssignment(curr);
		}
		if (withSearchAndDestroy) {
			taskManager.orderSearchAndDestroy(member, false);
		}
	}
	
	protected void escort(DeployedFleetMemberAPI member, DeployedFleetMemberAPI target) {
		if (member.getShip() == null) return;
		AssignmentInfo curr = taskManager.getAssignmentFor(member.getShip());
		if (curr != null && curr.getType() == CombatAssignmentType.LIGHT_ESCORT && 
				taskManager.getAssignmentTargetFor(member.getShip()) == target) {
			return;
		}
		
		AssignmentInfo info = taskManager.createAssignment(CombatAssignmentType.LIGHT_ESCORT, target, false);
		taskManager.setAssignmentWeight(info, 0f);
		taskManager.giveAssignment(member, info, false);
	}
	
	protected void giveMovementOrder(DeployedFleetMemberAPI member, Vector2f loc, float minDist, float maxDist) {
		AssignmentInfo curr = taskManager.getAssignmentFor(member.getShip());
		boolean needToMakeAssignment = curr == null || curr.getTarget() == null || Misc.getDistance(curr.getTarget().getLocation(), loc) > 100f;
		
		float dist = Misc.getDistance(member.getLocation(), loc);
		if (dist < minDist) {
			if (curr != null && (curr.getType() == CombatAssignmentType.RALLY_CIVILIAN)) {
				taskManager.removeAssignment(curr);
			}
		}
		
		boolean needToLeash = false;
		needToLeash |= curr == null && dist > minDist;
		needToLeash |= curr != null && dist > maxDist;
		
		if (needToMakeAssignment && needToLeash) {
			AssignmentTargetAPI wp = taskManager.createWaypoint2(loc, allyMode);
			AssignmentInfo task = taskManager.createAssignment(CombatAssignmentType.RALLY_CIVILIAN, wp, false);
			taskManager.giveAssignment(member, task, false);
		}
	}
	
	public void cleanUpEmptyAssignments() {
		taskManager.reassign(); // so assigned members isn't empty
		List<AssignmentInfo> remove = new ArrayList<>();
		for (AssignmentInfo curr : taskManager.getAllAssignments()) {
			if (curr.getType() == CombatAssignmentType.CONTROL) continue;
			if (curr.getType() == CombatAssignmentType.CAPTURE) continue;
			if (curr.getType() == CombatAssignmentType.DEFEND) continue;
			if (curr.getAssignedMembers().isEmpty()) {
				remove.add(curr);
			}
		}
		for (AssignmentInfo curr : remove) {
			taskManager.removeAssignment(curr);
		}
		
		taskManager.clearEmptyWaypoints();
	}
	
	protected Vector2f getEnemyCenterOfMass() {
		Vector2f com = new Vector2f();
		float weight = 0;
		for (DeployedFleetMemberAPI member : enemyFleetManager.getDeployedCopyDFM()) {
			if (member.isFighterWing()) continue;
			if (member.getShip() == null) continue;
			if (!engine.isAwareOf(owner, member.getShip())) continue;
			
			Vector2f.add(member.getLocation(), com, com);
			weight++; // maybe feels better than scaling weight by ship size etc
		}
		if (weight > 0) {
			com.scale(1f / weight);
		}
		return com;
	}
	
	
	public static boolean isCombatUnit(ShipAPI ship) {
		return ship.getHullSpec().hasTag(Tags.THREAT_COMBAT);
	}
	public static boolean isOverseer(ShipAPI ship) {
		return ship.getHullSpec().hasTag(Tags.THREAT_OVERSEER);
	}
	public static boolean isHive(ShipAPI ship) {
		return ship.getHullSpec().hasTag(Tags.THREAT_HIVE);
	}
	public static boolean isFabricator(ShipAPI ship) {
		return ship.getHullSpec().hasTag(Tags.THREAT_FABRICATOR);
		//return ship.getSystem() != null && ship.getSystem().getId().equals(ShipSystems.CONSTRUCTION_SWARM);
	}
}























