package com.fs.starfarer.api.combat;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CombatFleetManagerAPI {
	public interface AssignmentInfo {
		CombatAssignmentType getType();
		AssignmentTargetAPI getTarget();
	}
	
	
//	public MutableStat getCommandPointsStat();
//	public MutableStat getMaxFleetPoints();
	
	/**
	 * Deploy a ship/fighter wing with the given spec or variant id.
	 * 
	 * If there isn't one in the reserves, a temporary FleetMemberAPI is created and added to the reserves
	 * (but not the underlying CampaignFleetAPI, if any)
	 * 
	 * @param id
	 * @param location Where to deploy.
	 * @param facing Facing at time of deployment.
	 * @return
	 */
	public ShipAPI spawnShipOrWing(String specId, Vector2f location, float facing);
	
	/**
	 * Deploy a ship/fighter wing with the given spec or variant id.
	 * 
	 * If there isn't one in the reserves, a temporary FleetMemberAPI is created and added to the reserves
	 * (but not the underlying CampaignFleetAPI, if any)
	 * @param specId
	 * @param location
	 * @param facing
	 * @param level crew experience level
	 * @param initialBurnDur amount of time travel drive should be on (in seconds)
	 * @return
	 */
	public ShipAPI spawnShipOrWing(String specId, Vector2f location, float facing, float initialBurnDur);
	
	/**
	 * member does not actually have to be in the reserves.
	 * @param member
	 * @param location
	 * @param facing
	 * @param initialBurnDur
	 * @return
	 */
	public ShipAPI spawnFleetMember(FleetMemberAPI member, Vector2f location, float facing, float initialBurnDur);

	
	
	/**
	 * Returns ship that corresponds to the fleet member passed in. Returns the wing leader for fighter wings.
	 * @param fleetMember
	 * @return
	 */
	public ShipAPI getShipFor(FleetMemberAPI fleetMember);
	
	public List<FleetMemberAPI> getDeployedCopy(); 
	public List<FleetMemberAPI> getReservesCopy(); 
	
	
	DeployedFleetMemberAPI getDeployedFleetMember(ShipAPI ship);
	DeployedFleetMemberAPI getDeployedFleetMemberEvenIfDisabled(ShipAPI ship);
	AssignmentTargetAPI createWaypoint(Vector2f location, boolean ally);
	
	
	FleetGoal getGoal();
	void addToReserves(FleetMemberAPI member);
	void removeFromReserves(FleetMemberAPI member);

	CombatTaskManagerAPI getTaskManager(boolean ally);

	boolean isOnlyTimidOrNonCombatDeployed();

	List<FleetMemberAPI> getDisabledCopy();
	List<FleetMemberAPI> getDestroyedCopy();
	List<FleetMemberAPI> getRetreatedCopy();

	boolean isSuppressDeploymentMessages();
	void setSuppressDeploymentMessages(boolean suppressDeploymentMessages);

	
	boolean isDefendingStation();
	List<DeployedFleetMemberAPI> getStations();

	List<DeployedFleetMemberAPI> getDeployedCopyDFM();

	int getOwner();

	void setDefaultCommander(PersonAPI defaultCommander);
	PersonAPI getDefaultCommander();

	/**
	 * May return null if both the reserves and the deployed lists are empty. 
	 * @return
	 */
	PersonAPI getFleetCommander();

	/**
	 * Max deployment points available.
	 * @return
	 */
	int getMaxStrength();

	boolean isDeployedStation();
	void setDeployedStation(boolean deployedStation);

	void setDeploymentYOffset(float deploymentYOffset);
	float getDeploymentYOffset();

	float getEnemyCleanDisengageProgress();
	float getEnemyCleanDisengageThreshold();
	float getEnemyCleanDisengagePoints();
	boolean canEnemyDisengageCleanly();

	List<PersonAPI> getAllFleetCommanders();
	PersonAPI getFleetCommanderPreferPlayer();

	List<DeployedFleetMemberAPI> getAllEverDeployedCopy();

	boolean isCanForceShipsToEngageWhenBattleClearlyLost();
	
	/**
	 * Defaults to true for enemy, false for player side.
	 * @param canForceShipsToEngageWhenBattleClearlyLost
	 */
	void setCanForceShipsToEngageWhenBattleClearlyLost(boolean canForceShipsToEngageWhenBattleClearlyLost);

	ShipAPI spawnShipOrWing(String specId, Vector2f location, float facing, float initialBurnDur, PersonAPI captain);

	Map<DeployedFleetMemberAPI, DeployedFleetMemberAPI> getShardToOriginalShipMap();

	DeployedFleetMemberAPI getDeployedFleetMemberFromAllEverDeployed(ShipAPI ship);

	ShipAPI getShipFor(PersonAPI captain);

	FleetMemberAPI getBiggestStationDeployedOrNot();

	AdmiralAIPlugin getAdmiralAI();
	void setAdmiralAI(AdmiralAIPlugin admiralAI);
}





