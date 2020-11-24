package com.fs.starfarer.api.impl.campaign.events.nearby;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.Misc;

public class DistressCallPirateAmbushAssignmentAI extends BaseAssignmentAI {

	protected StarSystemAPI system;
	protected SectorEntityToken jumpPoint;
	
	protected float elapsed = 0f;
	protected float dur = 30f + (float) Math.random() * 20f;
	
	public DistressCallPirateAmbushAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI system, SectorEntityToken jumpPoint) {
		super();
		this.fleet = fleet;
		this.system = system;
		this.jumpPoint = jumpPoint;
		
		giveInitialAssignments();
	}

	@Override
	protected void giveInitialAssignments() {
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, jumpPoint, 1000f, "laying in wait");
	}

	@Override
	protected void pickNext() {
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, jumpPoint, 1000f, "laying in wait");
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		elapsed += days;
		
		if (elapsed >= dur) {
			Misc.giveStandardReturnToSourceAssignments(fleet);
		}
	}

}












