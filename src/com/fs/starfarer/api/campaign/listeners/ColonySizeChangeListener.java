package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public interface ColonySizeChangeListener {
	void reportColonySizeChanged(MarketAPI market, int prevSize);
}
