package com.fs.starfarer.api.impl.campaign.events.nearby;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.Misc;

public class DistressCallPirateAmbushTrapAssignmentAI extends BaseAssignmentAI {

	protected StarSystemAPI system;
	protected SectorEntityToken jumpPoint;
	
	protected float elapsed = 0f;
	protected float dur = 30f + (float) Math.random() * 20f;
	
	public DistressCallPirateAmbushTrapAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI system, SectorEntityToken jumpPoint) {
		super();
		this.fleet = fleet;
		this.system = system;
		this.jumpPoint = jumpPoint;
		
		giveInitialAssignments();
	}

	@Override
	protected void giveInitialAssignments() {
		pickNext();
	}

	@Override
	protected void pickNext() {
		float angle = Misc.getAngleInDegrees(system.getCenter().getLocation(), jumpPoint.getLocation());
		float dist = Misc.getDistance(system.getCenter().getLocation(), jumpPoint.getLocation()) + 3000f;
		
		angle += (float) Math.random() * 10f - 20f;
		dist += (float) Math.random() * 500f;
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
		loc.scale(dist);
		Vector2f.add(system.getCenter().getLocation(), loc, loc);
		
		SectorEntityToken token = system.createToken(loc.x, loc.y);
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, token, 3f, "laying in wait");
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












