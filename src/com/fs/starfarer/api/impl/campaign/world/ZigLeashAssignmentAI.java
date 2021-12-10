package com.fs.starfarer.api.impl.campaign.world;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ZigLeashAssignmentAI extends BaseAssignmentAI {

	protected float elapsed = 0f;
	protected float dur = 30f + (float) Math.random() * 20f;
	protected SectorEntityToken toGuard;
	
	protected IntervalUtil moteSpawn = new IntervalUtil(0.01f, 0.1f);
	
	public ZigLeashAssignmentAI(CampaignFleetAPI fleet, SectorEntityToken toGuard) {
		super();
		this.fleet = fleet;
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
		
//		if (fleet.getCurrentAssignment() == null || fleet.getCurrentAssignment().getAssignment() != FleetAssignment.HOLD) {
//			fleet.addAssignmentAtStart(FleetAssignment.HOLD, null, 1000f, null);
//		}
		
		if (toGuard != null) {
			float dist = Misc.getDistance(fleet.getLocation(), toGuard.getLocation());
			if (dist > toGuard.getRadius() + fleet.getRadius() + 1500 && 
					fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_AGGRESSIVE) {
				fleet.addAssignmentAtStart(FleetAssignment.ORBIT_PASSIVE, toGuard, 1f, null);
				CampaignFleetAIAPI ai = fleet.getAI();
				if (ai instanceof ModularFleetAIAPI) {
					// needed to interrupt an in-progress pursuit
					ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
					m.getStrategicModule().getDoNotAttack().add(m.getTacticalModule().getTarget(), 1f);
					m.getTacticalModule().setTarget(null);
				}
			}
		}
		
		float days = Misc.getDays(amount);
		moteSpawn.advance(days * 1f);
		if (moteSpawn.intervalElapsed()) {
			spawnMote(fleet);
		}
	}

	
	public static void spawnMote(SectorEntityToken from) {
		if (!from.isInCurrentLocation()) return;
		
		float dur = 1f + 2f * (float) Math.random();
		dur *= 2f;
		float size = 3f + (float) Math.random() * 5f;
		size *= 3f;
		Color color = new Color(255,100,255,175);
		
		Vector2f loc = Misc.getPointWithinRadius(from.getLocation(), from.getRadius());
		Vector2f vel = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		vel.scale(5f + (float) Math.random() * 10f);
		Vector2f.add(vel, from.getVelocity(), vel);
		Misc.addGlowyParticle(from.getContainingLocation(), loc, vel, size, 0.5f, dur, color);
	}
}












