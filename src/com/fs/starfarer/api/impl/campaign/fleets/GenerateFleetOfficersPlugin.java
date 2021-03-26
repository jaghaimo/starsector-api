package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;

public interface GenerateFleetOfficersPlugin extends GenericPlugin {
	public static class GenerateFleetOfficersPickData {
		public CampaignFleetAPI fleet;
		public FleetParamsV3 params;
		public GenerateFleetOfficersPickData(CampaignFleetAPI fleet, FleetParamsV3 params) {
			this.fleet = fleet;
			this.params = params;
		}
	}
	
	
	void addCommanderAndOfficers(CampaignFleetAPI fleet, FleetParamsV3 params, Random random);
}
