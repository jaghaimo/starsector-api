package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.SubmarketAPI;

public interface SubmarketUpdateListener {
	void reportSubmarketCargoAndShipsUpdated(SubmarketAPI submarket);
	
}
