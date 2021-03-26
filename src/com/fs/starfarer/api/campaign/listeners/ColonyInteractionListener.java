package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public interface ColonyInteractionListener {
	void reportPlayerOpenedMarket(MarketAPI market);
	void reportPlayerClosedMarket(MarketAPI market);
	void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market);
	void reportPlayerMarketTransaction(PlayerMarketTransaction transaction);
}
