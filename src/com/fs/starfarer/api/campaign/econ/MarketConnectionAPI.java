package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.combat.StatBonus;

/**
 * DO NOT store references to market connections, as the actual connection may not be persisted and a copy
 * saved through a reference may not be the "real" connection after a savegame is loaded.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2016 Fractal Softworks, LLC
 */
public interface MarketConnectionAPI {
	String getId();
	
	float getModifiedPrice(float price);
	float getSmugglePrice(float price);
	StatBonus getSmugglingMod();
	StatBonus getPriceMod();
	boolean isAllTradeIsSmuggling();
	void setAllTradeIsSmuggling(boolean allTradeIsSmuggling);

	String getMarketIdOne();
	String getMarketIdTwo();

	boolean isEnabled();
	void setEnabled(boolean enabled);
}
