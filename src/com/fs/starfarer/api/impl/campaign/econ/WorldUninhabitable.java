package com.fs.starfarer.api.impl.campaign.econ;


public class WorldUninhabitable extends BaseMarketConditionPlugin {
	
	public void apply(String id) {
//		float pop = getPopulation(market);
//		market.getDemand(Commodities.ORGANICS).getDemand().modifyFlat(id, pop * ConditionData.WORLD_UNINHABITABLE_ORGANICS_MULT);
	}

	public void unapply(String id) {
//		market.getDemand(Commodities.ORGANICS).getDemand().unmodify(id);
	}

}
