package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public interface ColonyDecivListener {
	void reportColonyAboutToBeDecivilized(MarketAPI market, boolean fullyDestroyed);
	void reportColonyDecivilized(MarketAPI market, boolean fullyDestroyed);
}
