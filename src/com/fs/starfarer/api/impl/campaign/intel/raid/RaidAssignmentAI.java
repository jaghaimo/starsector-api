package com.fs.starfarer.api.impl.campaign.intel.raid;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetActionTextProvider;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.Misc;

public class RaidAssignmentAI extends RouteFleetAssignmentAI implements FleetActionTextProvider {

	public RaidAssignmentAI(CampaignFleetAPI fleet, RouteData route, FleetActionDelegate delegate) {
		super(fleet, route, delegate);
		fleet.getAI().setActionTextProvider(this);
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount, false);
		
		RouteSegment curr = route.getCurrent();
		//if (!Misc.isBusy(fleet) && 
		if (curr != null && 
				(
					BaseRaidStage.STRAGGLER.equals(route.getCustom()) || 
					AssembleStage.WAIT_STAGE.equals(curr.custom) || 
					curr.isTravel())) {
			Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, "raid_wait", true, 1);
		}
		
		checkCapture(amount);
		//checkBuild(amount);
		
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_RAIDER)) {
			checkRaid(amount);
		}
	}

	@Override
	protected String getInSystemActionText(RouteSegment segment) {
		if (AssembleStage.WAIT_STAGE.equals(segment.custom)) {
			return "waiting at rendezvous point";
		}
		String s = null;
		if (delegate != null) s = delegate.getRaidInSystemText(fleet);
		if (s == null) s = "raiding"; 
		return s;
	}

	@Override
	protected String getEndingActionText(RouteSegment segment) {
		return super.getEndingActionText(segment);
	}

	@Override
	protected String getStartingActionText(RouteSegment segment) {
		if (AssembleStage.PREP_STAGE.equals(segment.custom)) {
			String s = null;
			if (delegate != null) s = delegate.getRaidPrepText(fleet, segment.from);
			if (s == null) s = "preparing for raid"; 
			return s;
		}
		if (segment.from == route.getMarket().getPrimaryEntity()) {
			return "orbiting " + route.getMarket().getName();
		}
		
		String s = null;
		if (delegate != null) s = delegate.getRaidDefaultText(fleet);
		if (s == null) s = "raiding"; 
		return s;
	}

	@Override
	protected String getTravelActionText(RouteSegment segment) {
		return super.getTravelActionText(segment);
	}

	public String getActionText(CampaignFleetAPI fleet) {
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null && curr.getAssignment() == FleetAssignment.PATROL_SYSTEM &&
				curr.getActionText() == null) {
			
			String s = null;
			if (delegate != null) s = delegate.getRaidDefaultText(fleet);
			if (s == null) s = "raiding"; 
			return s;
			
		}
		return null;
	}



	
	
}
