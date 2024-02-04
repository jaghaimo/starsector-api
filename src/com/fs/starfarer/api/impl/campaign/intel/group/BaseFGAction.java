package com.fs.starfarer.api.impl.campaign.intel.group;

import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.util.Misc;

public class BaseFGAction implements FGAction {

	protected FleetGroupIntel intel;
	protected String id = null;
	protected boolean finished = false;
	protected float elapsed = 0f;
	
	public BaseFGAction() {
	}


	public void addRouteSegment(RouteData route) {
		
	}


	public FleetGroupIntel getIntel() {
		return intel;
	}
	public void setIntel(FleetGroupIntel intel) {
		this.intel = intel;
	}


	public void directFleets(float amount) {
		elapsed += Misc.getDays(amount);
	}

	public boolean isActionFinished() {
		return finished;
	}

	public void setActionFinished(boolean finished) {
		this.finished = finished;
		
//		if (finished) {
//			intel.notifyActionFinished(this);
//		}
	}


	public void notifySegmentFinished(RouteSegment segment) {

	}


	public void notifyFleetsSpawnedMidSegment(RouteSegment segment) {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public float getElapsed() {
		return elapsed;
	}

	public void setElapsed(float elapsed) {
		this.elapsed = elapsed;
	}


	public float getEstimatedDaysToComplete() {
		return 0;
	}
	
	
	
}
