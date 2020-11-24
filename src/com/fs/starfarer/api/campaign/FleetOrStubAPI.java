package com.fs.starfarer.api.campaign;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

public interface FleetOrStubAPI {

	String getId();
	LocationAPI getContainingLocation();
	Vector2f getLocation();
	FleetAssignmentDataAPI getCurrentAssignment();
	List<FleetAssignmentDataAPI> getAssignmentsCopy();
	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
			float maxDurationInDays, Script onCompletion);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
			float maxDurationInDays, String actionText, Script onCompletion);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
			float maxDurationInDays, String actionText);
	void addAssignmentAtStart(FleetAssignment assignment,
			SectorEntityToken target, float maxDurationInDays,
			String actionText, Script onCompletion);
	void addAssignmentAtStart(FleetAssignment assignment,
			SectorEntityToken target, float maxDurationInDays,
			Script onCompletion);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays);
	boolean isCurrentAssignment(FleetAssignment assignment);
	void removeFirstAssignmentIfItIs(FleetAssignment assignment);
	void removeFirstAssignment();
	void clearAssignments();
	MemoryAPI getMemoryWithoutUpdate();
	
	void despawn();
	void despawn(FleetDespawnReason reason, Object param);
	boolean isFleet();
	//boolean isStub();
	void addScript(EveryFrameScript script);
	List<FleetEventListener> getEventListeners();
	void addEventListener(FleetEventListener listener);
	void removeEventListener(FleetEventListener listener);
	List<EveryFrameScript> getScripts();
	Vector2f getLocationInHyperspace();

	
	
}
