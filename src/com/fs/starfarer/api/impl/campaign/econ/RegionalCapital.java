package com.fs.starfarer.api.impl.campaign.econ;


public class RegionalCapital extends BaseMarketConditionPlugin {

	public void apply(String id) {
		market.getStability().modifyFlat(id, ConditionData.STABILITY_REGIONAL_CAPITAL, "Regional capital");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
