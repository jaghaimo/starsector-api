package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface EngagementResultForFleetAPI {
	
	//public List<DeployedFleetMember> getAllEverDeployed();
	
	CampaignFleetAPI getFleet();
	FleetGoal getGoal();
	boolean isWinner();
	
	boolean enemyCanCleanDisengage();
	
	List<FleetMemberAPI> getDeployed();
	List<FleetMemberAPI> getDestroyed();
	List<FleetMemberAPI> getDisabled();
	List<FleetMemberAPI> getRetreated();
	List<FleetMemberAPI> getReserves();

	void resetAllEverDeployed();
	
	List<DeployedFleetMemberAPI> getAllEverDeployedCopy();
	boolean isPlayer();
	void setGoal(FleetGoal goal);
	
//	int getMarinesLost();
//	CrewCompositionAPI getCrewLosses();

}
