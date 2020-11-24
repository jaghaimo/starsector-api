package com.fs.starfarer.api.campaign.ai;

import java.util.List;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetActionTextProvider;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface CampaignFleetAIAPI {
	
	public static enum ActionType {
		CANCEL,
		HOSTILE,
	}
	
	public static enum EncounterOption {
		ENGAGE,
		DISENGAGE,
		HOLD,
		HOLD_VS_STRONGER,
	}
	
//	public static enum PostEngagementOption {
//		MAINTAIN_CONTACT,
//		STAND_DOWN,
//	}
	
	public static enum PursuitOption {
		PURSUE,
		HARRY,
		LET_THEM_GO,
	}
	
	public static enum InitialBoardingResponse {
		BOARD,
		ENGAGE,
		LET_IT_GO,
	}
	
	public static enum BoardingActionType {
		HARD_DOCK,
		LAUNCH,
		ABORT,
	}
	
	public static class BoardingActionDecision {
		private BoardingActionType type;
		private CrewCompositionAPI party;
	}
	
	
	void advance(float amount);
	
	
	/*
	Whether *wants* to attack, if it were able to
	Engage, flee, or a "let them go, but engage if they engage" behavior
	After a battle, whether it wants to harry, salvage, or stand down	
	*/
	boolean isHostileTo(CampaignFleetAPI other);
	EncounterOption pickEncounterOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet);
	
	boolean wantsToJoin(BattleAPI battle, boolean playerInvolved);
	
	//PostEngagementOption pickPostEngagementOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet);
	
	PursuitOption pickPursuitOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet);
	
	InitialBoardingResponse pickBoardingResponse(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CampaignFleetAPI otherFleet);
	List<FleetMemberAPI> pickBoardingTaskForce(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CampaignFleetAPI otherFleet);
	BoardingActionDecision makeBoardingDecision(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CrewCompositionAPI maxAvailable);
	
	void performCrashMothballingPriorToEscape(FleetEncounterContextPlugin context, CampaignFleetAPI playerFleet);
	
	void reportNearbyAction(ActionType type, SectorEntityToken actor, SectorEntityToken target, String responseVariable);
	
	String getActionTextOverride();
	void setActionTextOverride(String actionTextOverride);
	
	FleetAssignmentDataAPI getCurrentAssignment();
	List<FleetAssignmentDataAPI> getAssignmentsCopy();
	
	
	void addAssignmentAtStart(FleetAssignment assignment,
			SectorEntityToken target, float maxDurationInDays,
			Script onCompletion);
	void removeFirstAssignment();
	void addAssignmentAtStart(FleetAssignment assignment,
			SectorEntityToken target, float maxDurationInDays,
			String actionText, Script onCompletion);
	void removeFirstAssignmentIfItIs(FleetAssignment assignment);
	boolean isCurrentAssignment(FleetAssignment assignment);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion);
	boolean isFleeing();
	void removeAssignment(FleetAssignmentDataAPI assignment);


	void clearAssignments();


	void dumpResourcesIfNeeded();

	void notifyInteractedWith(CampaignFleetAPI otherFleet);

	FleetAssignment getCurrentAssignmentType();
	
	void doNotAttack(SectorEntityToken other, float durDays);


	EncounterOption pickEncounterOption(FleetEncounterContextPlugin context,
			CampaignFleetAPI otherFleet, boolean pureCheck);


	FleetActionTextProvider getActionTextProvider();
	void setActionTextProvider(FleetActionTextProvider actionTextProvider);


	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
					   float maxDurationInDays, String actionText, boolean addTimeToNext,
					   Script onStart, Script onCompletion);


	boolean isMaintainingContact();
}










