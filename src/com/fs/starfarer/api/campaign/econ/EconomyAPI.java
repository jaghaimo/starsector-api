package com.fs.starfarer.api.campaign.econ;

import java.util.List;

import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;


public interface EconomyAPI {
	
	/**
	 * Called whenever economy data is recalculated, i.e. both when time passes and
	 * when the player opens the trade screen etc.
	 * 
	 * Needs to be added via EconomyAPI.addUpdateListener(); should not be added to SectorAPI.getListenerManager(). 
	 *
	 * Copyright 2018 Fractal Softworks, LLC
	 */
	public static interface EconomyUpdateListener {
		void commodityUpdated(String commodityId);
		void economyUpdated();
		boolean isEconomyListenerExpired();
	}
	
	void addMarket(MarketAPI market, boolean withJunkAndChatter);
	void removeMarket(MarketAPI market);
	
	MarketAPI getMarket(String id);
	
	void advance(float amount);
	
	List<MarketAPI> getMarketsCopy();
	
	boolean isSimMode();
	List<String> getAllCommodityIds();
	CommoditySpecAPI getCommoditySpec(String commodityId);
//	void clearReachMap();
//	void restoreReachMap();
	int getNumMarkets();
	
	/**
	 * Should only be called from UI interactions, not from scripts that run in the background.
	 * Calling this method is likely to take longer than the time to render a single frame.
	 */
	void nextStep();
	
	
	/**
	 * Equivalent to calling nextStep() three times.
	 * Generally enough or almost enough to fully stabilize the economy.
	 * 
	 * Should only be called from UI interactions, not from scripts that run in the background.
	 * Calling this method is likely to take longer than the time to render a single frame.
	 */
	void tripleStep();
	
	void addUpdateListener(EconomyUpdateListener listener);
	void removeUpdateListener(EconomyUpdateListener listener);
	List<EconomyUpdateListener> getUpdateListeners();
	
	
	void forceStockpileUpdate(MarketAPI market);
	List<MarketAPI> getMarketsWithSameGroup(MarketAPI market);
	List<MarketAPI> getMarketsWithSameGroup(MarketAPI market, List<MarketAPI> markets);
	
	//WaystationBonus getWaystationBonus(String marketId, String otherId);
	
	List<MarketAPI> getMarkets(LocationAPI loc);
	List<LocationAPI> getLocationsWithMarkets();
	List<StarSystemAPI> getStarSystemsWithMarkets();
	List<MarketAPI> getMarketsInGroup(String group);
	
	/**
	 * Equivalent to calling nextStep() two times.
	 * 
	 * Should only be called from UI interactions, not from scripts that run in the background.
	 * Calling this method is likely to take longer than the time to render a single frame.
	 */
	void doubleStep();
}



