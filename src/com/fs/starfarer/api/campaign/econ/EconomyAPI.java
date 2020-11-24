package com.fs.starfarer.api.campaign.econ;

import java.util.List;

import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;


public interface EconomyAPI {
	
	/**
	 * Called whenever economy data is recalculated, i.e. both when time passes and
	 * when the player opens the trade screen etc.
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
	void nextStep();
	
	
	/**
	 * Equivalent to calling nextStep() three times.
	 * Generally enough or almost enough to fully stabilize the economy. 
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
}



