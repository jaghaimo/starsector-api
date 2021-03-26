package com.fs.starfarer.api.impl.campaign.missions.hub;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
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
		if (isMissionEnded()) {
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










