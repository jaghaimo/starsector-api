package com.fs.starfarer.api.impl.campaign.tutorial;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.Misc;

public class TutorialLeashAssignmentAI extends BaseAssignmentAI {

	protected StarSystemAPI system;
	protected SectorEntityToken jumpPoint;
	
	protected float elapsed = 0f;
	protected float dur = 30f + (float) Math.random() * 20f;
	protected SectorEntityToken toGuard;
	
	public TutorialLeashAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI system, SectorEntityToken toGuard) {
		super();
		this.fleet = fleet;
		this.system = system;
		this.toGuard = toGuard;
		
		giveInitialAssignments();
	}

	@Override
	protected void giveInitialAssignments() {
		pickNext();
	}

	@Override
	protected void pickNext() {
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, toGuard, 100f);
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		
		float dist = Misc.getDistance(fleet.getLocation(), toGuard.getLocation());
//		if (fleet.getFaction().getId().equals(Factions.HEGEMONY)) {
//			System.out.println(fleet.getCurrentAssignment().getAssignment().name() + ", dist: " + dist);
//		}
		
		if (dist > 1500 && fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_AGGRESSIVE) {
			fleet.addAssignmentAtStart(FleetAssignment.ORBIT_PASSIVE, toGuard, 3f, null);
			CampaignFleetAIAPI ai = fleet.getAI();
			if (ai instanceof ModularFleetAIAPI) {
				// needed to interrupt an in-progress pursuit
				ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
				m.getStrategicModule().getDoNotAttack().add(m.getTacticalModule().getTarget(), 1f);
				m.getTacticalModule().setTarget(null);
			}
		}
	}

}












