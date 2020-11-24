package com.fs.starfarer.api.impl.campaign.econ;


public class Dissident extends BaseMarketConditionPlugin {
	
	public void apply(String id) {
//		
//		float pop = getPopulation(market);
//		market.getDemand(Commodities.HAND_WEAPONS).getDemand().modifyFlat(id, Math.max(10f, pop * ConditionData.DISSIDENT_WEAPONS_MULT));
//		
//		market.getDemand(Commodities.MARINES).getDemand().modifyFlat(id, pop * ConditionData.DISSIDENT_MARINES_MULT);
//		//market.getDemand(Commodities.MARINES).getNonConsumingDemand().modifyFlat(id, ConditionData.DISSIDENT_MARINES_MULT * ConditionData.CREW_MARINES_NON_CONSUMING_FRACTION);
		
		market.getStability().modifyFlat(id, ConditionData.STABILITY_DISSIDENT, "Dissident population");
	}

	public void unapply(String id) {
//		market.getCommodityData(Commodities.CREW).getSupply().unmodify(id);
//		
//		market.getDemand(Commodities.HAND_WEAPONS).getDemand().unmodify(id);
//		market.getDemand(Commodities.MARINES).getDemand().unmodify(id);
		
		market.getStability().unmodify(id);
	}

}
