package com.fs.starfarer.api.impl.campaign.intel.group;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.util.IntervalUtil;

public class FGWaitAction extends FGDurationAction {

	protected SectorEntityToken where;
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	protected String waitText;
	protected boolean doNotGetSidetracked = true;
	
	public FGWaitAction(SectorEntityToken where, float waitDays) {
		this(where, waitDays, "orbiting " + where.getName());
	}

	public FGWaitAction(SectorEntityToken where, float waitDays, String waitText) {
		super(waitDays);
		this.where = where;
		this.waitText = waitText;
		
		interval.forceIntervalElapsed();
	}
	
	@Override
	public void addRouteSegment(RouteData route) {
		RouteSegment segment = new RouteSegment(getDurDays(), where);
		route.addSegment(segment);
	}


	@Override
	public void directFleets(float amount) {
		super.directFleets(amount);
		if (isActionFinished()) return;
		
		List<CampaignFleetAPI> fleets = intel.getFleets();
		if (fleets.isEmpty()) {
			setActionFinished(true);
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		interval.advance(days);
		
		if (!interval.intervalElapsed()) return;

		
		for (CampaignFleetAPI fleet : fleets) {				
			fleet.clearAssignments();
			
			if (where.getStarSystem() != null && where == where.getStarSystem().getCenter()) {
				fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, null, 3f, waitText);
			} else {
				if (doNotGetSidetracked) {
					fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, where, 3f, waitText);
				} else {
					fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, where, 3f, waitText);
				}
			}
		}
	}


	
	public SectorEntityToken getWhere() {
		return where;
	}

	public void setWhere(SectorEntityToken where) {
		this.where = where;
	}

	public String getWaitText() {
		return waitText;
	}

	public void setWaitText(String waitText) {
		this.waitText = waitText;
	}

	public boolean isDoNotGetSidetracked() {
		return doNotGetSidetracked;
	}

	public void setDoNotGetSidetracked(boolean doNotGetSidetracked) {
		this.doNotGetSidetracked = doNotGetSidetracked;
	}
	
}




