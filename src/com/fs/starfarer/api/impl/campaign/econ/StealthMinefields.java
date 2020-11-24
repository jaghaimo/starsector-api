package com.fs.starfarer.api.impl.campaign.econ;


public class StealthMinefields extends BaseMarketConditionPlugin {

	public void apply(String id) {
		market.getStability().modifyFlat(id, ConditionData.STABILITY_STEALTH_MINEFIELDS, "Stealth minefields");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}

}
