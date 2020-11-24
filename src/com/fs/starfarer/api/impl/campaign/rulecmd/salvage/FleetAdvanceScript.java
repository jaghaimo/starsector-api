package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;

public class FleetAdvanceScript implements EveryFrameScript {
	private CampaignFleetAPI fleet;

	public FleetAdvanceScript(CampaignFleetAPI fleet) {
		this.fleet = fleet;
	}

	
	public void advance(float amount) {
		fleet.advance(amount);
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}
