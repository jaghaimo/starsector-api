/**
 * 
 */
package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableStat;

public class MutableCommodityQuantity {
	private String commodityId;
	private MutableStat quantity = new MutableStat(0);
	
	public MutableCommodityQuantity(String commodityId) {
		this.commodityId = commodityId;
	}

	public CommoditySpecAPI getSpec() {
		return Global.getSettings().getCommoditySpec(commodityId);
	}

	public String getCommodityId() {
		return commodityId;
	}

	public MutableStat getQuantity() {
		return quantity;
	}
}