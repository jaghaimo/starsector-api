package com.fs.starfarer.api.impl.campaign.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.SaveableIterator;

public class CommodityStatTracker {

	public static class CommodityStats {
		private float minSupplyPrice;
		private float maxDemandPrice;
		private float averageSupplyPrice;
		private float averageDemandPrice;
		private float totalDemand;
		private float totalSupply;
		private float totalStockpiles;
		private final String commodityId;

		public CommodityStats(String commodityId) {
			this.commodityId = commodityId;
		}
		
//		public MarketAPI getMarket() {
//			return Global.getSector().getEconomy().getMarket(marketId);
//		}
//		public CommodityOnMarketAPI getCommodity() {
//			MarketAPI market = getMarket();
//			if (market == null) return null;
//			return market.getCommodityData(commodityId);
//		}
		
		public float getMinSupplyPrice() {
			return minSupplyPrice;
		}
		public float getMaxDemandPrice() {
			return maxDemandPrice;
		}
		public float getAverageSupplyPrice() {
			return averageSupplyPrice;
		}
		public float getAverageDemandPrice() {
			return averageDemandPrice;
		}
		public String getCommodityId() {
			return commodityId;
		}
		public void setMinSupplyPrice(float min) {
			this.minSupplyPrice = min;
		}
		public void setMaxDemandPrice(float max) {
			this.maxDemandPrice = max;
		}
		public void setAverageSupplyPrice(float average) {
			this.averageSupplyPrice = average;
		}
		public float getTotalDemand() {
			return totalDemand;
		}
		public void setTotalDemand(float totalDemand) {
			this.totalDemand = totalDemand;
		}
		public float getTotalSupply() {
			return totalSupply;
		}
		public void setTotalSupply(float totalSupply) {
			this.totalSupply = totalSupply;
		}
		public float getTotalStockpiles() {
			return totalStockpiles;
		}
		public void setTotalStockpiles(float totalStockpiles) {
			this.totalStockpiles = totalStockpiles;
		}
		public void setAverageDemandPrice(float weightedAveragePrice) {
			this.averageDemandPrice = weightedAveragePrice;
		}
	}
	
	private IntervalUtil timer = new IntervalUtil(0.25f, 0.75f);
	
	private Map<String, CommodityStats> priceData = new HashMap<String, CommodityStats>();
	private boolean firstFrame = true;
	
	public void advance(float days) {

		if (firstFrame) {
			firstFrame = false;
			EconomyAPI economy = Global.getSector().getEconomy();
			for (String id : economy.getAllCommodityIds()) {
				updateCommodityStats(id);
			}
		}
		
		timer.advance(days);
		if (timer.intervalElapsed()) {
			updateStatsNextStep();
		}
		
//		compute prices stuff here? min/max
//		add an "is price change significant" method; "is price significant" method
	}
	
	
	public boolean isSupplyPriceSignificant(CommodityOnMarketAPI com) {
		return isSupplyPriceWithMultSignificant(com, 1f);
	}
	public boolean isDemandPriceSignificant(CommodityOnMarketAPI com) {
		return isDemandPriceWithMultSignificant(com, 1f);
	}
	
	public boolean isSupplyPriceWithMultSignificant(CommodityOnMarketAPI com, float mult) {
		float supplyPrice = Math.round(com.getMarket().getSupplyPrice(com.getId(), 1, true));
		supplyPrice *= mult;
		return isSupplyPriceSignificant(com, supplyPrice);
	}
	
	public boolean isDemandPriceWithMultSignificant(CommodityOnMarketAPI com, float mult) {
		float demandPrice = Math.round(com.getMarket().getDemandPrice(com.getId(), 1, true));
		demandPrice *= mult;
		return isSupplyPriceSignificant(com, demandPrice);
	}
	
	public boolean isSupplyPriceSignificant(CommodityOnMarketAPI com, float price) {
		//if (com.getAverageStockpileAfterDemand() < 100) return false;
		CommodityStats stats = getStats(com.getId());
		float average = stats.getAverageSupplyPrice();
		//return price <= average * .5f || average - price > com.getCommodity().getBasePrice() * 0.5f;
		//return price <= average * .5f || average - price > 100f;
		//return price <= average * .5f || average - price > Math.max(100, com.getCommodity().getBasePrice() * 0.5f);
		
//		float margin = Misc.getProfitMargin();
//		margin = Math.min(margin * 0.5f, average * 0.75f);
		float flat = Misc.getProfitMarginFlat();
		float mult = Misc.getProfitMarginMult();
		
		return price <= average - flat || price <= average / mult;
		
//		if (margin < Misc.getProfitMargin() * 0.25f) {
//			margin = Misc.getProfitMargin() * 0.25f;
//		}
//		return price <= average - margin;
	}
	
	public boolean isDemandPriceSignificant(CommodityOnMarketAPI com, float price) {
		//if (com.getDemand().getDemandValue() < 100) return false;
		CommodityStats stats = getStats(com.getId());
		float average = stats.getAverageDemandPrice();
		//return price >= average * 1.75f || price - average > com.getCommodity().getBasePrice() * 1f;
		//return price >= average * 1.75f || price - average > 100f;
		//return price >= average * 1.75f || price - average > Math.max(100, com.getCommodity().getBasePrice() * 1f);
		
		float flat = Misc.getProfitMarginFlat();
		float mult = Misc.getProfitMarginMult();
		
		return price >= average + flat || price >= average * mult;
		
//		float margin = Misc.getProfitMargin();
//		margin = margin - Math.min(margin * 0.5f, average * 0.5f);
//		
//		return price >= average + margin;
	}

	
//	public boolean isCommodityPriceSpreadInteresting(String commodityId) {
//		CommodityStats stats = getStats(commodityId);
//
//		float diff = Math.abs(stats.getMaxDemandPrice() - stats.getMinSupplyPrice());
//		return isCommodityPriceSpreadInteresting(commodityId, diff);
//	}
//	
//	public boolean isCommodityPriceSpreadInteresting(String commodityId, float diff) {
//		CommodityStats stats = getStats(commodityId);
//		if (diff > 100) return true;
//		if (diff > stats.getMinSupplyPrice() * .75f) return true;
//		return false;
//	}
	
	
	
	private SaveableIterator<String> commodityIterator = null;
	private void updateStatsNextStep() {
		EconomyAPI economy = Global.getSector().getEconomy();
		
		if (commodityIterator == null || !commodityIterator.hasNext()) {
			commodityIterator = new SaveableIterator<String>(economy.getAllCommodityIds());
		}
		if (!commodityIterator.hasNext()) return; // no commodities exist, yyeah.
		
		String commodityId = commodityIterator.next();
		
		updateCommodityStats(commodityId);
	}
	
	public void updateCommodityStats(String commodityId) {
		EconomyAPI economy = Global.getSector().getEconomy();
		List<MarketAPI> markets = economy.getMarketsCopy();

		float totalSupply = 0;
		float totalDemand = 0;
		float totalStockpiles = 0;
		float minSupplyPrice = Float.MAX_VALUE;
		float maxDemandPrice = 0;
		
		float totalSupplyPrice = 0;
		float totalDemandPrice = 0;
		
		for (MarketAPI market : markets) {
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			
			//float supply = com.getSupplyValue();
			float demand = com.getDemand().getDemandValue();
			float supply = demand;
			float stockpile = com.getStockpile();
			
			if (supply < 10 && demand < 10 && stockpile < 10) continue;
			
			float supplyPrice = Math.round(market.getSupplyPrice(com.getId(), 1, true));
			float demandPrice = Math.round(market.getDemandPrice(com.getId(), 1, true)); 
		
			if (supplyPrice < minSupplyPrice) {
				minSupplyPrice = supplyPrice;
			}
			if (demandPrice > maxDemandPrice) {
				maxDemandPrice = demandPrice;
			}
			
			totalSupply += supply;
			totalDemand += demand;
			totalStockpiles += stockpile;
			
			totalSupplyPrice += stockpile * supplyPrice;
			totalDemandPrice += demand * demandPrice;
		}
		
		CommodityStats stats = getStats(commodityId);
		
		stats.setTotalSupply(totalSupply);
		stats.setTotalDemand(totalDemand);
		stats.setTotalStockpiles(totalStockpiles);
		stats.setMaxDemandPrice(maxDemandPrice);
		stats.setMinSupplyPrice(minSupplyPrice);
		
		if (totalStockpiles > 0) {
			stats.setAverageSupplyPrice(totalSupplyPrice / totalStockpiles);
		} else {
			stats.setAverageSupplyPrice(0);
		}
		
		if (totalDemand > 0) {
			stats.setAverageDemandPrice(totalDemandPrice / totalDemand);
		} else {
			stats.setAverageDemandPrice(0);
		}
	}
	
	
	public CommodityStats getStats(String commodityId) {
		CommodityStats data = priceData.get(commodityId);
		if (data == null) {
			data = new CommodityStats(commodityId);
			priceData.put(commodityId, data);
		}
		return data;
	}
}
























