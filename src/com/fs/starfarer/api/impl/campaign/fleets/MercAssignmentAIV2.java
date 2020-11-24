package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;

public class MercAssignmentAIV2 extends RouteFleetAssignmentAI {

	public MercAssignmentAIV2(CampaignFleetAPI fleet, RouteData route) {
		super(fleet, route);
	}

	
	protected String getTravelActionText(RouteSegment segment) {
		SectorEntityToken dest = segment.getDestination();
		if (segment.getId() == MercFleetManagerV2.ROUTE_TRAVEL) {
			return "traveling to " + dest.getMarket().getName();
		}
		if (segment.getId() == MercFleetManagerV2.ROUTE_RETURN) {
			return "returning to " + dest.getMarket().getName();
		}
		return "traveling";
	}
	
	protected String getInSystemActionText(RouteSegment segment) {
		return "patrolling";
	}
	
	protected String getStartingActionText(RouteSegment segment) {
		return "orbiting " + route.getMarket().getName();
	}
	
	protected String getEndingActionText(RouteSegment segment) {
		return "orbiting " + route.getMarket().getName();
	}
	
}





