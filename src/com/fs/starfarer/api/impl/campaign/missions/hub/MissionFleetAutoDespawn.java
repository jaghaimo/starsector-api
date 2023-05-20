package com.fs.starfarer.api.impl.campaign.missions.hub;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

public class MissionFleetAutoDespawn implements EveryFrameScript {

	protected CampaignFleetAPI fleet;
	protected float elapsedWaitingForDespawn = 0f;
	protected HubMission mission;
	
	public MissionFleetAutoDespawn(HubMission mission, CampaignFleetAPI fleet) {
		this.mission = mission;
		this.fleet = fleet;
	}
	
	protected int framesWithNoAssignment = 0;
	public void advance(float amount) {
		
//		System.out.println("MFAD: " + fleet.getName());
//		if (fleet.getName().equals("Courier")) {
//			System.out.println("fwefwf23f");
//		}
//		System.out.println(fleet.getName());
//		if (fleet.getName().equals("Mercenary Bounty Hunter")) {
//			System.out.println("efwefweew");
//		}
		boolean missionImportant = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.ENTITY_MISSION_IMPORTANT);
		FleetAssignmentDataAPI ad = fleet.getCurrentAssignment();
		// make a hopefully reasonable guess that the fleet should stop chasing the player, if that's what it's doing
		// it it's not "important" (i.e. likely wrong mission stage) and not in the same location and far away
		if (ad != null && 
				(ad.getAssignment() == FleetAssignment.INTERCEPT || ad.getAssignment() == FleetAssignment.FOLLOW) &&
				ad.getTarget() == Global.getSector().getPlayerFleet() &&
				!missionImportant) {
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			if (!fleet.isInCurrentLocation()) { 
				float dist = Misc.getDistanceLY(fleet, Global.getSector().getPlayerFleet());
				if (dist > 4f) {
					fleet.removeFirstAssignment();
					if (fleet.getCurrentAssignment() == null) {
						Misc.giveStandardReturnToSourceAssignments(fleet);
					}
				}
			}
			else if (fleet.isHostileTo(pf) && fleet.getFaction() != null && 
					!fleet.getFaction().isHostileTo(Factions.PLAYER)) {
				fleet.removeFirstAssignment();
				if (fleet.getCurrentAssignment() == null) {
					Misc.giveStandardReturnToSourceAssignments(fleet);
				}
			}
		}
		
//		clean up, figure out details of exactly how to do this 
//			- don't want to do it super quick when still during the mission
//			- probably want to give some kind of assignment if there's nothing left after removing the intercept
//			- and also when the mission's ended, give return assignments too? still auto-despawn! just, have it
//					be returning instead of sitting around waiting
		
		if (isMissionEnded()) {
			if (fleet.getCurrentAssignment() == null) {
				Misc.giveStandardReturnToSourceAssignments(fleet);
			}
			
			if (!fleet.isInCurrentLocation() && Misc.getDistanceToPlayerLY(fleet) > 3f) {
				elapsedWaitingForDespawn += Global.getSector().getClock().convertToDays(amount);
				if (elapsedWaitingForDespawn > 30f && fleet.getBattle() == null) {
					fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
					elapsedWaitingForDespawn = 0f;
				}
			} else {
				elapsedWaitingForDespawn = 0f;
			}
			
		} else if (fleet.isInCurrentLocation() && fleet.getCurrentAssignment() == null) {
			framesWithNoAssignment++;
			if (framesWithNoAssignment >= 10) {
				Misc.giveStandardReturnToSourceAssignments(fleet);
			}
		} else {
			framesWithNoAssignment = 0;
		}
	}
	
	public boolean isMissionEnded() {
		return mission instanceof IntelInfoPlugin && (((IntelInfoPlugin)mission).isEnded() || ((IntelInfoPlugin)mission).isEnding());
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	

}










