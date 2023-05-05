package com.fs.starfarer.api.impl.campaign.econ;

import java.util.Arrays;

public class LuddicMajority extends BaseMarketConditionPlugin {

	public static String [] luddicFactions = new String [] {
		"knights_of_ludd",
		"luddic_church",
		"luddic_path",
	};
	public void apply(String id) {
		if (Arrays.asList(luddicFactions).contains(market.getFactionId())) {
			market.getStability().modifyFlat(id, ConditionData.STABILITY_LUDDIC_MAJORITY_BONUS, "Luddic majority");
		} else {
			market.getStability().modifyFlat(id, ConditionData.STABILITY_LUDDIC_MAJORITY_PENALTY, "Luddic majority");
		}
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
