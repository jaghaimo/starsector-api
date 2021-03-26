package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

public class MissionFleetStopPursuingPlayer implements EveryFrameScript {

	protected CampaignFleetAPI fleet;
	protected BaseHubMission mission;
	protected Set<Object> stages = new HashSet<Object>();
	protected boolean done = false;
	
	
	public MissionFleetStopPursuingPlayer(CampaignFleetAPI fleet, BaseHubMission mission, List<Object> stages) {
		this.mission = mission;
		this.fleet = fleet;
		this.stages.addAll(stages);
	}
	
	public void advance(float amount) {
		if (done) return;
		Object stage = mission.getCurrentStage();
		
		if (!stages.contains(stage)) {
			if (fleet.getAI() instanceof ModularFleetAIAPI) {
				ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
				if (ai.getAssignmentModule() != null) {
					FleetAssignmentDataAPI curr = ai.getAssignmentModule().getCurrentAssignment();
					if (curr != null && curr.getTarget() == Global.getSector().getPlayerFleet() &&
							(curr.getAssignment() == FleetAssignment.INTERCEPT ||
							curr.getAssignment() == FleetAssignment.FOLLOW)) {
						ai.getAssignmentModule().removeFirstAssignment();
					}
				}
			}
			if (fleet.getAI() != null && fleet.getAI().getAssignmentsCopy().isEmpty()) {
				Misc.giveStandardReturnToSourceAssignments(fleet, true);
			}
			Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE,
					mission.getReason(), false, -1f);
			done = true;
		}

	}
	
	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}
	

}










