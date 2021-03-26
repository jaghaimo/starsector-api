package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface GateTransitListener {
	/**
	 * gateFrom may be null. Called at the start of the transition.
	 * 
	 * @param fleet
	 * @param gateFrom
	 * @param gateTo
	 */
	public void reportFleetTransitingGate(CampaignFleetAPI fleet, SectorEntityToken gateFrom, SectorEntityToken gateTo);
}
