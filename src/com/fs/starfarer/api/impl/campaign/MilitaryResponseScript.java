package com.fs.starfarer.api.impl.campaign;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class MilitaryResponseScript implements EveryFrameScript {
	
	public static String RESPONSE_ASSIGNMENT = "response"; // custom value added to assignments so we know which to clear
	
	public static class MilitaryResponseParams {
		public ActionType type;
		public String responseReason;
		public FactionAPI faction;
		
		public SectorEntityToken actor;
		public SectorEntityToken target;
		public float responseFraction;
		public float responseDuration;
		public String travelText;
		public String actionText;
		
		public MilitaryResponseParams(ActionType type, String responseReason,
									  FactionAPI faction, SectorEntityToken target,
									  float responseFraction, float responseDuration) {
			this.type = type;
			this.responseReason = responseReason;
			this.faction = faction;
			this.target = target;
			this.responseFraction = responseFraction;
			this.responseDuration = responseDuration;
		}
		
		
	}
	
	
	protected IntervalUtil tracker = new IntervalUtil(0.05f, 0.15f);
	protected MilitaryResponseParams params;
	protected float elapsed;
	
	public MilitaryResponseScript(MilitaryResponseParams params) {
		this.params = params;
		addToResponseTotal();
		initiateResponse();
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		tracker.advance(days);
		
		elapsed += days;
		
		
		if (tracker.intervalElapsed()) {
			initiateResponse();
		}
	}

	
	public void initiateResponse() {
		if (params.target.getContainingLocation() == null) return;
		
		List<CampaignFleetAPI> fleets = params.target.getContainingLocation().getFleets();
		for (CampaignFleetAPI fleet : fleets) {
			seeIfFleetShouldRespond(fleet);
		}
	}

	protected boolean isTemporarilyNotResponding(CampaignFleetAPI fleet) {
		if (fleet.getBattle() != null) return true;
		
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_BUSY)) return true;
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null && curr.getAssignment() == FleetAssignment.STANDING_DOWN) return true;
		
		MemoryAPI memory = fleet.getMemoryWithoutUpdate();
		if (memory.getBoolean(MemFlags.FLEET_MILITARY_RESPONSE)) return true;
		
		return false;
	}
	
	protected void seeIfFleetShouldRespond(CampaignFleetAPI fleet) {
//		if (fleet.getContainingLocation() == Global.getSector().getCurrentLocation()) {
//			System.out.println("fwefwef");
//		}
		
		if (!couldRespond(fleet)) return;
		
		if (isTemporarilyNotResponding(fleet)) return;
		
		List<CampaignFleetAPI> fleets = params.target.getContainingLocation().getFleets();
		float potentialFP = 0;
		float respondingFP = 0f;
		
		float closestDist = Float.MAX_VALUE;
		CampaignFleetAPI closestNonResponder = null;
		
		for (CampaignFleetAPI other : fleets) {
			if (!couldRespond(other)) continue;
			
			float fp = other.getFleetPoints();
			
			potentialFP += fp;
			boolean responding = isResponding(other);
			if (responding) {
				respondingFP += fp;
			}
			
			//if (other == fleet) continue;
			
			if (!responding && !isTemporarilyNotResponding(other)) {
				float distOther = Misc.getDistance(params.target, other);
				if (distOther < closestDist) {
					closestDist = distOther;
					closestNonResponder = other;
				}
			}
		}
		
		float fraction = params.responseFraction / getResponseTotal();
		
		//float dist = Misc.getDistance(params.target, fleet);
		if (potentialFP > 0 &&
				respondingFP / potentialFP < fraction &&
				closestNonResponder == fleet) {
			
			respond(fleet);
		}
	}
	
	protected void respond(CampaignFleetAPI fleet) {
		unrespond(fleet);
		
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
								MemFlags.FLEET_MILITARY_RESPONSE, params.responseReason, true, (1.5f + (float) Math.random()) * 0.2f);
		
		fleet.addAssignmentAtStart(FleetAssignment.PATROL_SYSTEM, params.target, 3f, params.actionText, null);
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null) {
			curr.setCustom(RESPONSE_ASSIGNMENT);
		}
		
		float dist = Misc.getDistance(params.target, fleet);
		if (dist > 2000f) {
			fleet.addAssignmentAtStart(FleetAssignment.GO_TO_LOCATION, params.target, 3f, params.travelText, null);
			//fleet.addAssignmentAtStart(FleetAssignment.DELIVER_CREW, params.target, 3f, params.travelText, null);
			curr = fleet.getCurrentAssignment();
			if (curr != null) {
				curr.setCustom(RESPONSE_ASSIGNMENT);
			}
		}
		
		//Global.getSector().addPing(fleet, Pings.DANGER);
	}
	
	protected void unrespond(CampaignFleetAPI fleet) {
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
							   MemFlags.FLEET_MILITARY_RESPONSE, params.responseReason, false, 0f);
		for (FleetAssignmentDataAPI curr : fleet.getAI().getAssignmentsCopy()) {
			if (RESPONSE_ASSIGNMENT.equals(curr.getCustom())) {
				fleet.getAI().removeAssignment(curr);
			}
		}
	}
	
	protected boolean isResponding(CampaignFleetAPI fleet) {
		return Misc.flagHasReason(fleet.getMemoryWithoutUpdate(), MemFlags.FLEET_MILITARY_RESPONSE, params.responseReason);
	}
	
	protected boolean couldRespond(CampaignFleetAPI fleet) {
		if (fleet.getFaction() != params.faction) return false;
		if (fleet.getAI() == null) return false;
		if (fleet.isPlayerFleet()) return false;
		if (fleet.isStationMode()) return false;
		
		// don't check for this here as it would skew proportiions of what's assigned where if a fleet is busy for a bit
		//if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_BUSY)) return false;
		
		if (fleet.getAI() instanceof ModularFleetAIAPI) {
			ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
			if (ai.getAssignmentModule().areAssignmentsFrozen()) return false;
		}
		
		if (fleet.getCurrentAssignment() != null && 
				fleet.getCurrentAssignment().getAssignment() == FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
			return false;
		}
		
		MemoryAPI memory = fleet.getMemoryWithoutUpdate();
		
		boolean patrol = memory.getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET);
		boolean warFleet = memory.getBoolean(MemFlags.MEMORY_KEY_WAR_FLEET);
		boolean pirate = memory.getBoolean(MemFlags.MEMORY_KEY_PIRATE);
		boolean noMilitary = memory.getBoolean(MemFlags.FLEET_NO_MILITARY_RESPONSE);
		if (!(patrol || warFleet || pirate) || noMilitary) return false;
		
		return true;
	}
	
	protected String getResponseTotalKey() {
		return "$mrs_" + params.responseReason;
	}
	
	protected void addToResponseTotal() {
		MemoryAPI memory = params.faction.getMemoryWithoutUpdate();
		String key = getResponseTotalKey();
		
		float curr = memory.getFloat(key);
		memory.set(key, curr + params.responseFraction, 60f);
	}
	
	protected void removeFromResponseTotal() {
		MemoryAPI memory = params.faction.getMemoryWithoutUpdate();
		String key = getResponseTotalKey();
		
		float curr = memory.getFloat(key);
		if (curr > params.responseFraction) {
			memory.set(key, Math.max(0, curr - params.responseFraction), 60f);
		} else {
			memory.unset(key);
		}
	}
	
	protected float getResponseTotal() {
		MemoryAPI memory = params.faction.getMemoryWithoutUpdate();
		String key = getResponseTotalKey();
		
		float curr = memory.getFloat(key);
		if (curr < params.responseFraction) curr = params.responseFraction;
		if (curr < 1) curr = 1;
		return curr;
	}
	
	public void forceDone() {
		if (params != null) {
			elapsed = params.responseDuration;
		}
	}
	
	public boolean isDone() {
		if (params == null || elapsed >= params.responseDuration) {
			removeFromResponseTotal();
			params = null;
			return true;
		}
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public MilitaryResponseParams getParams() {
		return params;
	}

	public float getElapsed() {
		return elapsed;
	}

	public void setElapsed(float elapsed) {
		this.elapsed = elapsed;
	}
	
	

}
