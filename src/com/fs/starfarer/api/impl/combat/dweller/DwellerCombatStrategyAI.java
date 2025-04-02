package com.fs.starfarer.api.impl.combat.dweller;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.IntervalUtil;

/**
 * Like the Threat one:
 * Doesn't care about command points etc, just functions in a different way. May use command points/tasks/etc
 * to accomplish its goals, just as an implementation detail, but conceptually it's fundamentally different from how
 * human-type fleets work.
 * 
 * @author Alex
 *
 */
public class DwellerCombatStrategyAI {
	
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
	
	protected boolean gaveInitialOrders = false;
	
	
	public DwellerCombatStrategyAI(int owner) {
		engine = Global.getCombatEngine();
		this.owner = owner;
		playerSide = owner == 0;
		allyMode = playerSide;
		//allyMode = false;
		fleetManager = engine.getFleetManager(owner);
		taskManager = fleetManager.getTaskManager(allyMode);
		taskManager.getCommandPointsStat().modifyFlat("DwellerCombatStrategyAI", 1000000000);
		
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
	}
	
	protected void giveInitialOrders() {
				
	}
	
	
	public void advance(float amount) {
		//if (true) return;
		if (abort) return;
		if (engine.isPaused()) return;
		
		everySecond.advance(amount);
		if (everySecond.intervalElapsed()) {
			// if non-threat ships are deployed from this fleetManager, don't want to be doing any Threat things
			List<DeployedFleetMemberAPI> deployed = fleetManager.getDeployedCopyDFM();
			if (deployed.isEmpty()) return;
			
			boolean someMatching = false;
			for (DeployedFleetMemberAPI member : deployed) {
				if (!member.isFighterWing() && member.getShip() != null &&
						member.isAlly() == allyMode &&
						!member.getShip().getVariant().hasHullMod(HullMods.DWELLER_HULLMOD)) {
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
			
			for (DeployedFleetMemberAPI member : fleetManager.getDeployedCopyDFM()) {
				ShipAPI ship = member.getShip();
				if (ship == null || ship.getAI() == null) continue;
				
				if (isMaw(ship)) {
					AssignmentInfo curr = taskManager.getAssignmentInfoForTarget(member);
					if (curr == null) {
						taskManager.createAssignment(CombatAssignmentType.DEFEND, member, false);
						taskManager.orderSearchAndDestroy(member, false);
					}
				} else if (isEye(ship)) {
					AssignmentInfo curr = taskManager.getAssignmentInfoForTarget(member);
					if (curr == null) {
						taskManager.createAssignment(CombatAssignmentType.HEAVY_ESCORT, member, false);
						taskManager.orderSearchAndDestroy(member, false);
					}
				} else if (isStinger(ship)) {
//					AssignmentInfo curr = taskManager.getAssignmentInfoForTarget(member);
//					if (curr == null) {
//						taskManager.createAssignment(CombatAssignmentType.HEAVY_ESCORT, member, false);
//						taskManager.orderSearchAndDestroy(member, false);
//					}
				}
			}
			
			cleanUpEmptyAssignments();
		}
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
	
	
	public static boolean isMaw(ShipAPI ship) {
		boolean isMaw = ship != null && ship.isCapital();
		return isMaw;
	}
	public static boolean isEye(ShipAPI ship) {
		boolean isEye = ship != null && ship.isCruiser() && ship.getHullSpec().hasTag(Tags.DWELLER_RECKLESS);
		return isEye;
	}
	public static boolean isStinger(ShipAPI ship) {
		boolean isEye = ship != null && ship.isCruiser() && ship.getHullSpec().hasTag(Tags.DWELLER_TIMID);
		return isEye;
	}
	public static boolean isTendril(ShipAPI ship) {
		boolean isTendril = ship != null && ship.isDestroyer();
		return isTendril;
	}
}























