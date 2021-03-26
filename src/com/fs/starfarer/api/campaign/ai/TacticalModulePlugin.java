package com.fs.starfarer.api.campaign.ai;



import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.BoardingActionDecision;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.InitialBoardingResponse;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.PursuitOption;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface TacticalModulePlugin {
	
	void advance(float days);
	
	
	/**
	 * If set to non-null, tells the tactial module that reaching this destination
	 * is the goal.
	 * @param dest
	 */
	void setTravelDestination(Vector2f dest, float duration);


	/**
	 * Will not actually engage when followMode == true.
	 * @param priorityTarget
	 * @param duration
	 * @param followMode
	 */
	void setPriorityTarget(SectorEntityToken priorityTarget, float duration, boolean followMode);
	
	boolean isFleeing();
	SectorEntityToken getTarget();
	SectorEntityToken getLargestEnemy();
	boolean isBusy();


	void performCrashMothballingPriorToEscape(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet);
	EncounterOption pickEncounterOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet);
	PursuitOption pickPursuitOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet);
	BoardingActionDecision makeBoardingDecision(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CrewCompositionAPI maxAvailable);
	InitialBoardingResponse pickBoardingResponse(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CampaignFleetAPI otherFleet);
	List<FleetMemberAPI> pickBoardingTaskForce(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CampaignFleetAPI otherFleet);
	void reportNearbyAction(ActionType type, SectorEntityToken actor, SectorEntityToken target, String responseVariable);
	void notifyInteractedWith(CampaignFleetAPI other);


	void setTarget(SectorEntityToken target);


	void forceTargetReEval();


	boolean wantsToJoin(BattleAPI battle, boolean playerInvolved);

	boolean isMaintainingContact();

	boolean isHostileTo(CampaignFleetAPI other);
	boolean isHostileTo(CampaignFleetAPI other, boolean assumeTransponderOn);

	EncounterOption pickEncounterOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet, boolean pureCheck);


	boolean isStandingDown();


	float getPursuitDays();


	SectorEntityToken getPriorityTarget();
}




