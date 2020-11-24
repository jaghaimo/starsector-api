package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;

public interface FleetEventListener {
	void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param);
	
	/**
	 * "fleet" will be null if the listener is registered with the ListenerManager, and non-null
	 * if the listener is added directly to a fleet.
	 * @param fleet
	 * @param primaryWinner
	 * @param battle
	 */
	void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle);
}
