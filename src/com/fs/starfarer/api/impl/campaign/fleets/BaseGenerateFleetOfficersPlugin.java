package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;

public class BaseGenerateFleetOfficersPlugin implements GenerateFleetOfficersPlugin {

	public void addCommanderAndOfficers(CampaignFleetAPI fleet, FleetParamsV3 params, Random random) {
		
	}

	public int getHandlingPriority(Object params) {
		return -1;
	}

}
