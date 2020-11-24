package com.fs.starfarer.api.impl.campaign.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin.PriceUpdatePlugin;
import com.fs.starfarer.api.impl.campaign.shared.CommodityStatTracker;
import com.fs.starfarer.api.util.Misc;

public class PriceUpdate implements PriceUpdatePlugin {
	
	private String marketId;
	private String commodityId;
	private float supplyPrice;
	private float demandPrice;
	private PriceType type;
	private long timestamp;
	private float demand;//, available;
	public PriceUpdate(CommodityOnMarketAPI commodity) {
		//this.commodity = commodity;
		marketId = commodity.getMarket().getId();
		commodityId = commodity.getId();
		supplyPrice = Math.round(commodity.getMarket().getSupplyPrice(commodity.getId(), 1, true));
		demandPrice = Math.round(commodity.getMarket().getDemandPrice(commodity.getId(), 1, true));
		//priceMult = commodity.getPlayerPriceMult().getModifiedValue();
		
//		if (commodityId.equals("food")) {
//			System.out.println("dsfsdfsdf");
//		}
		updateType();
		timestamp = Global.getSector().getClock().getTimestamp();
		
//		float f = Global.getSettings().getFloat("economyAverageFractionOfStockpilePlayerCanBuy");
//		available = commodity.getAverageStockpileAfterDemand() * f;
		demand  = commodity.getDemand().getDemandValue();
	}
	
	public void updatePrices(float stockpileMult) {
		CommodityOnMarketAPI com = getCommodity();
		float stockpile = com.getStockpile();
		float after = stockpile * stockpileMult;
		
		float diff = (after - stockpile) * com.getUtilityOnMarket();
		
		supplyPrice = Math.round(com.getMarket().getSupplyPriceAssumingExistingTransaction(
				com.getId(), 1, diff, true));
		demandPrice = Math.round(com.getMarket().getDemandPriceAssumingExistingTransaction(
				com.getId(), 1, diff, true));
		
	}
	
	
	public int getRoundedPriceForDisplay() {
		return getRoundedPriceForDisplay(0, 0);
	}
	
	/**
	 * Only call this method when the bonus you want to ignore is not already applied.
	 * @param priceFlat
	 * @param pricePercent
	 * @return
	 */
	public int getRoundedPriceForDisplay(float priceFlat, float pricePercent) {
		float testSupplyPrice = supplyPrice;
		float testDemandPrice = demandPrice;
		//getCommodity().getMarket().getDemandPrice(getCommodity().getId(), 1, true)
		// if params are non-0, then the curr price may already include a player-facing modifier - so, discount it
		// this won't work for multiple sources modifying the same price, though
		// never mind that - only call this method when the bonus you want to ignore is not already applied.
		if (priceFlat != 0 || pricePercent != 0) {
			CommodityOnMarketAPI commodity = getCommodity();
			testSupplyPrice = Math.round(commodity.getMarket().getSupplyPrice(commodity.getId(), 1, true) * (1f + pricePercent / 100f) + priceFlat);
			testDemandPrice = Math.round(commodity.getMarket().getDemandPrice(commodity.getId(), 1, true) * (1f + pricePercent / 100f) + priceFlat);
		}
		if (true) {
			// for now, cheat and grab real prices from market
			CommodityOnMarketAPI commodity = getCommodity();
//			if (commodity.getId().equals(Commodities.HAND_WEAPONS) && commodity.getMarket().getId().equals("ogma")) {
//				System.out.println("q23sdfsdf");
//			}
			testSupplyPrice = Math.round(commodity.getMarket().getSupplyPrice(commodity.getId(), 1, true));
			testDemandPrice = Math.round(commodity.getMarket().getDemandPrice(commodity.getId(), 1, true));
			
			supplyPrice = testSupplyPrice;
			demandPrice = testDemandPrice;
			updateType();
		}
		
		int amt = 0;
		switch (getType()) {
		case NORMAL:
			amt = (int) (testSupplyPrice + testDemandPrice) / 2;
			break;
		case CHEAP:
			amt = (int) testSupplyPrice;
			break;
		case EXPENSIVE:
			amt = (int) testDemandPrice;
			break;
		}
		amt = (int) Misc.getRounded(amt);
		return amt;
	}
	
	
	
	public float getDemand() {
		return demand;
	}

	public float getAvailable() {
		//float f = Global.getSettings().getFloat("economyAverageFractionOfStockpilePlayerCanBuy");
		CommodityOnMarketAPI com = getMarket().getCommodityData(commodityId);
		//float available = com.getMaxPlayerFacingStockpile(f, com.getAverageStockpileAfterDemand());
		float available = com.getStockpile();
		return available;
	}

	public void updateType() {
		updateType(0f, 0f);
	}
	
	/**
	 * Only call this method when the bonus you want to ignore is not already applied.
	 * @param priceFlat
	 * @param pricePercent
	 * @return
	 */
	public void updateType(float priceFlat, float pricePercent) {
		CommodityOnMarketAPI commodity = getCommodity();
		//CommodityStatTracker stats = SharedData.getData().getActivityTracker().getCommodityTracker();
		CommodityStatTracker stats = new CommodityStatTracker();
		float testSupplyPrice = supplyPrice;
		float testDemandPrice = demandPrice;
		
		// if params are non-0, then the curr price may already include a player-facing modifier - so, discount it
		// this won't work for multiple sources modifying the same price, though
		// never mind - only call this method when the bonus you want to ignore is not already applied.
		if (priceFlat != 0 || pricePercent != 0) {
			testSupplyPrice = Math.round(commodity.getMarket().getSupplyPrice(commodity.getId(), 1, true) * (1f + pricePercent / 100f) + priceFlat);
			testDemandPrice = Math.round(commodity.getMarket().getDemandPrice(commodity.getId(), 1, true) * (1f + pricePercent / 100f) + priceFlat);
		}
		
		if (stats.isSupplyPriceSignificant(commodity, Misc.getRounded(testSupplyPrice))) {
			type = PriceType.CHEAP;
		} else if (stats.isDemandPriceSignificant(commodity, Misc.getRounded(testDemandPrice))) {
			type = PriceType.EXPENSIVE;
		} else {
			type = PriceType.NORMAL;
		}
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Checks whether supply/demand is non-trivial.
	 * @return
	 */
	public boolean isSignificant() {
		return isSignificant(0f, 0f);
	}
	
	/**
	 * Checks whether supply/demand is non-trivial.
	 * @param priceMult
	 * @return
	 */
	public boolean isSignificant(float priceFlat, float pricePercent) {
		updateType(priceFlat, pricePercent);
		CommodityOnMarketAPI commodity = getCommodity();
		if ((getType() == PriceType.CHEAP)
				&& commodity.getStockpile() < 100) return false;
		
		if ((getType() == PriceType.NORMAL)
				&& commodity.getStockpile() + commodity.getDemand().getDemandValue() < 100) return false;
		
		if (getType() == PriceType.EXPENSIVE && commodity.getDemand().getDemandValue() < 100) return false;
		
		return true;
	}
	
	public MarketAPI getMarket() {
		return Global.getSector().getEconomy().getMarket(marketId);
	}
	public CommodityOnMarketAPI getCommodity() {
		MarketAPI market = getMarket();
		if (market == null) return null;
		return market.getCommodityData(commodityId);
	}
	public float getSupplyPrice() {
		return supplyPrice;
	}
	public float getDemandPrice() {
		return demandPrice;
	}
	public PriceType getType() {
		return type;
	}
}
