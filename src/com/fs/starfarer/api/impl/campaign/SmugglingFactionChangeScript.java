package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class SmugglingFactionChangeScript implements EveryFrameScript {

	private CampaignFleetAPI fleet;
	private String origFaction;
	private IntervalUtil tracker = new IntervalUtil(0.1f, 0.3f);
	public SmugglingFactionChangeScript(CampaignFleetAPI fleet) {
		this.fleet = fleet;
		origFaction = fleet.getFaction().getId();
	}
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		tracker.advance(days);
		if (tracker.intervalElapsed() && fleet.getAI() != null) {
//			if (fleet.getContainingLocation().equals(Global.getSector().getPlayerFleet().getContainingLocation())) {
//				float dist = Misc.getDistance(fleet.getLocation(), Global.getSector().getPlayerFleet().getLocation());
//				if (dist < 500 && fleet.getFaction().getId().equals(Factions.INDEPENDENT)) {
//					System.out.println("sf3r2f");
//				}
//			}
			FleetAssignmentDataAPI assignment = fleet.getAI().getCurrentAssignment();
			if (assignment != null && assignment.getAssignment() != FleetAssignment.STANDING_DOWN) {
				SectorEntityToken target = assignment.getTarget();
				if (target != null && target.getFaction() != null) {
					boolean targetHostile = target.getFaction().isHostileTo(origFaction);
					boolean mathchesTarget = fleet.getFaction().getId().equals(target.getFaction().getId());
					boolean mathchesOrig = fleet.getFaction().getId().equals(origFaction);
					float dist = Misc.getDistance(fleet.getLocation(), target.getLocation());
					if (dist < target.getRadius() + fleet.getRadius() + 1000) {
						if (targetHostile && !mathchesTarget) {
							fleet.setFaction(target.getFaction().getId(), true);
						}
					} else {
						if (!mathchesOrig) {
							fleet.setFaction(origFaction, true);
						}
					}
				}
			}
		}
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
