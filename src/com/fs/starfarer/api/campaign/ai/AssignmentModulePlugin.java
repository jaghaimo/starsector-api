package com.fs.starfarer.api.campaign.ai;

import java.util.List;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface AssignmentModulePlugin {

	void advance(float days);
	FleetAssignmentDataAPI getCurrentAssignment();
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion);
	void addAssignmentAtStart(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion);
	void addAssignmentAtStart(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion);
	boolean isCurrentAssignment(FleetAssignment assignment);
	void removeFirstAssignmentIfItIs(FleetAssignment assignment);
	void removeFirstAssignment();
	void clearAssignments();
	List<FleetAssignmentDataAPI> getAssignmentsCopy();
	boolean areAssignmentsFrozen();
	void freezeAssignments();
	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
					   float maxDurationInDays, String actionText, boolean addTimeToNext,
					   Script onStart, Script onCompletion);
	void removeAssignment(FleetAssignmentDataAPI assignment);
	
	
}
