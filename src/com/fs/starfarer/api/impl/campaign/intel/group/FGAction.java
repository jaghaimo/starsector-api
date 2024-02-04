package com.fs.starfarer.api.impl.campaign.intel.group;

import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;

public interface FGAction {

	String getId();
	/**
	 * Not required for every action.
	 * @param id
	 */
	void setId(String id);
	
	void addRouteSegment(RouteData route);
	void notifySegmentFinished(RouteSegment segment);
	
	void notifyFleetsSpawnedMidSegment(RouteSegment segment);
	void directFleets(float amount);
	boolean isActionFinished();
	void setActionFinished(boolean finished);
	
	FleetGroupIntel getIntel();
	void setIntel(FleetGroupIntel intel);
	
	float getEstimatedDaysToComplete();

}
