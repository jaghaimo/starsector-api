package com.fs.starfarer.api.impl.campaign.econ;


public class Outpost extends BaseMarketConditionPlugin {

	public void apply(String id) {
		market.getStability().modifyFlat(id, ConditionData.STABILITY_OUTPOST, "Outpost");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
