package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;

public class AutoDespawnScript implements EveryFrameScript {
	protected CampaignFleetAPI fleet;
	protected float elapsed = 0f;
	
	public AutoDespawnScript(CampaignFleetAPI fleet) {
		this.fleet = fleet;
	}

	public void advance(float amount) {
		if (!fleet.isInCurrentLocation()) {
			elapsed += Global.getSector().getClock().convertToDays(amount);
			if (elapsed > 30 && fleet.getBattle() == null) {
				fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
				elapsed = -1;
			}
		} else {
			elapsed = 0f;
		}
	}

	public boolean isDone() {
		return elapsed < 0;
	}

	public boolean runWhilePaused() {
		return false;
	} 
}
