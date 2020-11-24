package com.fs.starfarer.api.impl.campaign.econ;


/**
 * Unused.
 */
public class TradeDisruption extends BaseMarketConditionPlugin {
	
	public TradeDisruption() {
	}

	public void apply(String id) {
//		for (AffectedCommodity com : event.getDisruptedCommodities()) {
//			com.commodity.removeFromAverageStockpile(com.disruptionQuantity);
//			com.commodity.setAverageStockpileAfterDemand(0f);
//		}
//		for (AffectedCommodity com : event.getDisruptedCommodities()) {
//			com.commodity.getPlayerPriceMod().modifyPercent(id, com.pricePercent);
//			com.commodity.getPlayerPriceMod().modifyFlat(id, com.priceFlat);
//		}
	}

	public void unapply(String id) {
//		for (AffectedCommodity com : event.getDisruptedCommodities()) {
//			float dq = Math.min(com.disruptionQuantity, com.preDisruptionStockpile - com.commodity.getAverageStockpile());
//			if (dq < 0) dq = 0;
//			com.commodity.addToAverageStockpile(dq);
//			com.disruptionQuantity = dq;
//		}
//		for (CommodityOnMarketAPI com : market.getAllCommodities()) {
//			com.getPlayerPriceMod().unmodify(id);
//		}
	}
	
	
	@Override
	public boolean isTransient() {
		return false;
	}
}





