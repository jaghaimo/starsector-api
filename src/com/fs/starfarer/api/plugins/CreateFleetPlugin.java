package com.fs.starfarer.api.plugins;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;

public interface CreateFleetPlugin extends GenericPlugin {
	public CampaignFleetAPI createFleet(FleetParamsV3 params);
}
