package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetInflater;

public interface FleetInflationListener {
	void reportFleetInflated(CampaignFleetAPI fleet, FleetInflater inflater);
}
