package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.impl.campaign.ids.Stats;


public class Headquarters extends BaseMarketConditionPlugin {

	public void apply(String id) {
		market.getStability().modifyFlat(id, ConditionData.STABILITY_HEADQUARTERS, "Headquarters");
		
		market.getStats().getDynamic().getStat(Stats.OFFICER_NUM_MULT).modifyFlat(id, ConditionData.HEADQUARTERS_OFFICER_NUM_MULT_BONUS);
		market.getStats().getDynamic().getStat(Stats.OFFICER_LEVEL_MULT).modifyFlat(id, ConditionData.HEADQUARTERS_OFFICER_LEVEL_MULT_BONUS);
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
		
		market.getStats().getDynamic().getStat(Stats.OFFICER_NUM_MULT).unmodify(id);
		market.getStats().getDynamic().getStat(Stats.OFFICER_LEVEL_MULT).unmodify(id);
	}

}
