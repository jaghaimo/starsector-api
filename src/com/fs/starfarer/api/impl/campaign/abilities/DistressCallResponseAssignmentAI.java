package com.fs.starfarer.api.impl.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.Misc;

public class DistressCallResponseAssignmentAI extends BaseAssignmentAI {

	protected StarSystemAPI system;
	
	protected float elapsed = 0f;
	protected float dur = 30f + (float) Math.random() * 20f;
	protected boolean contactedPlayer = false;

	protected final JumpPointAPI inner;
	protected final JumpPointAPI outer;
	
	public DistressCallResponseAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI system, JumpPointAPI inner, JumpPointAPI outer) {
		super();
		this.fleet = fleet;
		this.system = system;
		this.inner = inner;
		this.outer = outer;
		
		giveInitialAssignments();
	}

	@Override
	protected void giveInitialAssignments() {
		if (fleet.isInHyperspace()) {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, outer, 20f);
		}
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, inner, 10f + 5f * (float) Math.random());
	}

	@Override
	protected void pickNext() {
		MemoryAPI memory = fleet.getMemoryWithoutUpdate();
		memory.unset("$distressResponse");
		Misc.makeUnimportant(fleet, "distressResponse");
//		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
//    			   			   "distressResponse", false, 1000f);
		Misc.giveStandardReturnToSourceAssignments(fleet);
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (fleet.isInCurrentLocation() && !contactedPlayer) {
			VisibilityLevel level = fleet.getVisibilityLevelOfPlayerFleet();
			if (level != VisibilityLevel.NONE && level != VisibilityLevel.SENSOR_CONTACT) {
				contactedPlayer = true;
				fleet.addAssignmentAtStart(FleetAssignment.INTERCEPT, Global.getSector().getPlayerFleet(), 3f, "approaching your fleet", null);
			}
		}
	}

}












