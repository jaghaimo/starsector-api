package com.fs.starfarer.api.campaign.econ;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.FactionAPI;

public interface CommodityMarketDataAPI {

	List<MarketAPI> getMarkets();
	int getExportMarketSharePercent(MarketAPI market);
	int getMarketValuePercent(MarketAPI market);
	MarketShareDataAPI getMarketShareData(MarketAPI market);
	int getMaxExport(String factionId);
	float getMarketValue(String factionId);
	String getCommodityId();
	String getEconGroup();
	int getMaxExportGlobal();
	float getMarketValue();
	float getMarketValueOutsideFaction(String factionId);
	int getExportIncome(CommodityOnMarketAPI com);
	int getDemandValue(CommodityOnMarketAPI com);
	int getMaxShipping(MarketAPI market, boolean inFaction);
	
	List<MarketShareDataAPI> getSortedProducers();
	List<MarketShareDataAPI> getSortedConsumers();
	int getMarketSharePercent(FactionAPI faction);
	Map<FactionAPI, Integer> getMarketSharePercentPerFaction();

}
