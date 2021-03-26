package com.fs.starfarer.api.impl.campaign;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class CargoPodsResponse implements EveryFrameScript {
	
	public static String PODS_BUSY_REASON = "pods";
	
	
	protected IntervalUtil tracker = new IntervalUtil(0.05f, 0.15f);
	protected float elapsed;
	protected CustomCampaignEntityAPI pods;
	
	public CargoPodsResponse(CustomCampaignEntityAPI pods) {
		this.pods = pods;
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		tracker.advance(days);
		
		elapsed += days;
		
		
		if (tracker.intervalElapsed()) {
			checkResponse();
		}
	}

	public boolean isDone() {
		return elapsed >= 3f || !pods.isAlive() || pods.hasTag(Tags.NON_CLICKABLE);
	}

	public boolean runWhilePaused() {
		return false;
	}

	
	public void checkResponse() {
		List<CampaignFleetAPI> fleets = pods.getContainingLocation().getFleets();
		
		CampaignFleetAPI closest = null;
		float minDist = Float.MAX_VALUE;
		
		for (CampaignFleetAPI fleet : fleets) {
			if (!couldInvestigatePods(fleet)) continue;
			
			float dist = Misc.getDistance(pods, fleet);
			if (dist < minDist) {
				minDist = dist;
				closest = fleet;
			}
		}
		
		if (closest != null && minDist < 500f) {
			respond(closest);
			elapsed = 10f; // make it "done"
		}
	}
	
	protected float getBaseDur(CampaignFleetAPI fleet) {
		float dur = pods.getCargo().getFuel() + pods.getCargo().getSpaceUsed() + pods.getCargo().getTotalPersonnel();
		dur = dur / fleet.getCargo().getMaxCapacity();
		
		return dur;
	}

	
	protected void respond(final CampaignFleetAPI fleet) {
		unrespond(fleet);
		
		//float dur = (1.5f + (float) Math.random());
		float dur = getBaseDur(fleet);
		dur *= 5f;
		if (dur > 1f) dur = 1f;
		
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
								MemFlags.FLEET_BUSY, PODS_BUSY_REASON, true, dur);
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(
				Misc.getAngleInDegrees(pods.getLocation(), fleet.getLocation()));
		loc.scale(fleet.getRadius() + pods.getRadius());
		Vector2f.add(loc, pods.getLocation(), loc);
		final SectorEntityToken holdLoc = pods.getContainingLocation().createToken(loc);
		
		fleet.addAssignmentAtStart(FleetAssignment.HOLD, holdLoc, dur, "investigating " + pods.getName().toLowerCase(), 
				new Script() {
					public void run() {
						Misc.fadeAndExpire(pods);
						fleet.getCargo().addAll(pods.getCargo());
						pods.getCargo().clear();
					}
				});
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null) {
			curr.setCustom(MilitaryResponseScript.RESPONSE_ASSIGNMENT);
		}
		fleet.addScript(new EveryFrameScript() {
			private boolean done = false;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return done || !Misc.isBusy(fleet) || pods.hasTag(Tags.NON_CLICKABLE) || !pods.isAlive();
			}
			public void advance(float amount) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(
						Misc.getAngleInDegrees(pods.getLocation(), fleet.getLocation()));
				loc.scale(fleet.getRadius() + pods.getRadius());
				Vector2f.add(loc, pods.getLocation(), loc);
				holdLoc.setLocation(loc.x, loc.y);
				
				if (!pods.isAlive()) {
					if (Misc.isBusy(fleet)) {
						unrespond(fleet);
					}
					done = true;
				}
			}
		});
	}
	
	protected void unrespond(CampaignFleetAPI fleet) {
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
							   MemFlags.FLEET_BUSY, PODS_BUSY_REASON, false, 0f);
		for (FleetAssignmentDataAPI curr : fleet.getAI().getAssignmentsCopy()) {
			if (MilitaryResponseScript.RESPONSE_ASSIGNMENT.equals(curr.getCustom())) {
				fleet.getAI().removeAssignment(curr);
			}
		}
	}
	
	protected boolean couldInvestigatePods(CampaignFleetAPI fleet) {
		//if (fleet.getFaction() != params.faction) return false;
		if (fleet.getAI() == null) return false;
		if (fleet.isPlayerFleet()) return false;
		
		if (!fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PIRATE)) return false;
		
		if (fleet.getBattle() != null) return false;
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_BUSY)) return false;
		
		if (fleet.getAI() instanceof ModularFleetAIAPI) {
			ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
			if (ai.getAssignmentModule().areAssignmentsFrozen()) return false;
			if (ai.isFleeing() || ai.isMaintainingContact()) return false;
			if (ai.isCurrentAssignment(FleetAssignment.INTERCEPT)) return false;
		}
		
		
		VisibilityLevel level = pods.getVisibilityLevelTo(fleet);
		if (level == VisibilityLevel.NONE) return false;
		
		if (fleet.getCurrentAssignment() != null && 
				fleet.getCurrentAssignment().getAssignment() == FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
			return false;
		}
		
		float dur = getBaseDur(fleet);
		if (dur < 0.05f) return false;
		
		//MemoryAPI memory = fleet.getMemoryWithoutUpdate();
//		boolean patrol = memory.getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET);
//		boolean noMilitary = memory.getBoolean(MemFlags.FLEET_NO_MILITARY_RESPONSE);
//		if (!patrol || noMilitary) return false;
		
		return true;
	}
	
	
	

}
