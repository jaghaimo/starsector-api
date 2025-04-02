package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface RefitScreenListener {
	void reportFleetMemberVariantSaved(FleetMemberAPI member, MarketAPI dockedAt);
}
