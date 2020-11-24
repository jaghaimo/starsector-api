package com.fs.starfarer.api.campaign;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

/**
 * NOT USED, UNFINISHED.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2017 Fractal Softworks, LLC
 */
public interface FleetStubAPI extends FleetOrStubAPI {

	LocationAPI getContainingLocation();
	void setContainingLocation(LocationAPI containingLocation);
	FleetAssignmentDataAPI getCurrentAssignment();
	List<FleetAssignmentDataAPI> getAssignmentsCopy();
	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
			float maxDurationInDays, Script onCompletion);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
			float maxDurationInDays, String actionText, Script onCompletion);
	void addAssignmentAtStart(FleetAssignment assignment,
			SectorEntityToken target, float maxDurationInDays,
			String actionText, Script onCompletion);
	void addAssignmentAtStart(FleetAssignment assignment,
			SectorEntityToken target, float maxDurationInDays,
			Script onCompletion);
	boolean isCurrentAssignment(FleetAssignment assignment);
	void removeFirstAssignmentIfItIs(FleetAssignment assignment);
	void removeFirstAssignment();
	void clearAssignments();
	
	
	void setMemory(MemoryAPI memory);
	Object getParams();
	void setParams(Object params);
	Vector2f getLocation();
	List<String> getCargoList();
	String getId();
	void setId(String fleetId);
	
	void advance(float amount);
	MemoryAPI getMemoryWithoutUpdate();
	FleetStubConverterPlugin getConverter();
	void repickConverter();
	List<FleetEventListener> getEventListeners();
	String getAdmiralRank();
	void setAdmiralRank(String admiralRank);
	String getAdmiralPost();
	void setAdmiralPost(String admiralPost);
	
	CampaignFleetAPI getFleet();
	void setFleet(CampaignFleetAPI fleet);

}
