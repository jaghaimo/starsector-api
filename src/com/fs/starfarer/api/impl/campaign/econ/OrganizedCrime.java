package com.fs.starfarer.api.impl.campaign.econ;


public class OrganizedCrime extends BaseMarketConditionPlugin {

	public void apply(String id) {
		market.getStability().modifyFlat(id, ConditionData.STABILITY_ORGANIZED_CRIME, "Organized crime");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
