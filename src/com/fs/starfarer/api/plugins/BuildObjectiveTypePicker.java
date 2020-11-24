package com.fs.starfarer.api.plugins;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface BuildObjectiveTypePicker {
	
	public static class BuildObjectiveParams {
		public CampaignFleetAPI fleet;
		public FactionAPI faction;
		public SectorEntityToken stableLoc;
	}
	
	String pickObjectiveToBuild(BuildObjectiveParams params);
}
