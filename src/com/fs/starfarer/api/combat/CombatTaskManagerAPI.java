package com.fs.starfarer.api.combat;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;

public interface CombatTaskManagerAPI {
	/**
	 * Returns the current assignment for a ship (the assignment type, and the target, if any).
	 * Returns null if there isn't one (i.e. the ship is on a default search-and-destroy).
	 * 
	 * For fighter wings, can pass in any fighter from the wing to get the assignment.
	 * 
	 * @param ship
	 * @return
	 */
	AssignmentInfo getAssignmentFor(ShipAPI ship);
	List<AssignmentInfo> getAllAssignments();
	
	
	/**
	 * target should be one of:
	 * 	BattleObjectiveAPI
	 * 	DeployedFleetMemberAPI
	 * 	the result of createWaypoint()
	 * 
	 * @param type
	 * @param target
	 * @param useCommandPointIfNeeded
	 * @return
	 */
	AssignmentInfo createAssignment(CombatAssignmentType type, AssignmentTargetAPI target, boolean useCommandPoint);
	
	void giveAssignment(DeployedFleetMemberAPI member, AssignmentInfo assignment, boolean useCommandPointIfNeeded);
	void orderRetreat(DeployedFleetMemberAPI member, boolean useCommandPointIfNeeded, boolean direct);
	void orderSearchAndDestroy(DeployedFleetMemberAPI member, boolean useCommandPointIfNeeded);
	
	/**
	 * Cancels all assignments. New assignments can still be created.
	 */
	void orderSearchAndDestroy();
	
	/**
	 * Cancels all assignment and orders all ships to retreat. Can not be aborted.
	 */
	void orderFullRetreat();
	
	boolean isInFullRetreat();
	MutableStat getCommandPointsStat();
	int getCommandPointsLeft();
	boolean isPreventFullRetreat();
	void setPreventFullRetreat(boolean preventFullRetreat);
	
	boolean isFullAssault();
	void setFullAssault(boolean explicitSearchAndDestroy);
	
	float getSecondsUntilNextPoint();
	float getCPRateMult();
	float getCPInterval();
	MutableStat getCPRateModifier();
	void removeAssignment(AssignmentInfo info);
	void clearEmptyWaypoints();
	AssignmentTargetAPI createWaypoint2(Vector2f loc, boolean ally);
	void setAssignmentWeight(AssignmentInfo info, float weight);
	void reassign();
	AssignmentTargetAPI getAssignmentTargetFor(ShipAPI ship);
	void clearTasks();
	AssignmentInfo getAssignmentInfoForTarget(AssignmentTargetAPI target);
}
