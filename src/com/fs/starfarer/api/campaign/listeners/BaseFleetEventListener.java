package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;

public class BaseFleetEventListener implements FleetEventListener {

	public void reportBattleOccurred(CampaignFleetAPI fleet,
			CampaignFleetAPI primaryWinner, BattleAPI battle) {
		// TODO Auto-generated method stub
		
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet,
			FleetDespawnReason reason, Object param) {
		// TODO Auto-generated method stub
		
	}

}
