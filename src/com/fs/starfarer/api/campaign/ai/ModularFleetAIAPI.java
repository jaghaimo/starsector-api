package com.fs.starfarer.api.campaign.ai;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;

/**
 * 
 * Assignment module: keeps track of assignments and when they are completed (due to time or proximity), 
 * doesn't do any thinking.
 * 
 * Strategic module: figures out how to go about doing assignments - what the current
 * destination is, jump plans, etc.
 * 
 * Tactical module: takes strategic considerations and combines with local conditions,
 * i.e. terrain, hostile fleets, etc. Also handles encounter decisions, since those need to be consistent
 * with tactical ones (i.e. no pursuing a fleet it then won't want to engage).
 * The tactical module is the one that sets the fleet's movement destination.
 * 
 * Navigation module: figures out the best direction to move in, given what the tactical module
 * decides matters. Tactical module then may use the result if it wants to.
 * 
 * 
 * Ability AI plugins: communicate with the above using the fleet's memory.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public interface ModularFleetAIAPI extends CampaignFleetAIAPI {

	CampaignFleetAPI getFleet();
	
	NavigationModulePlugin getNavModule();
	void setNavModule(NavigationModulePlugin navModule);
	
	AssignmentModulePlugin getAssignmentModule();
	void setAssignmentModule(AssignmentModulePlugin assignmentModule);
	
	StrategicModulePlugin getStrategicModule();
	void setStrategicModule(StrategicModulePlugin strategicModule);
	
	TacticalModulePlugin getTacticalModule();
	void setTacticalModule(TacticalModulePlugin tacticalModule);
}








