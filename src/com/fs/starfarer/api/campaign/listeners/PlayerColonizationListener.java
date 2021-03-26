package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;


public interface PlayerColonizationListener {
	void reportPlayerColonizedPlanet(PlanetAPI planet);
	void reportPlayerAbandonedColony(MarketAPI colony);
}
