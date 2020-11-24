package com.fs.starfarer.api.impl.campaign.econ;


public class LargeRefugeePopulation extends BaseMarketConditionPlugin {

	public void apply(String id) {
		market.getStability().modifyFlat(id, ConditionData.STABILITY_REFUGEE_POPULATION, "Refugee population");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
