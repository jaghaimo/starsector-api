package com.fs.starfarer.api.impl.campaign.econ;


public class TradeCenter extends BaseMarketConditionPlugin {

	public void apply(String id) {
		market.getStability().modifyFlat(id, ConditionData.STABILITY_TRADE_CENTER, "Trade center");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
