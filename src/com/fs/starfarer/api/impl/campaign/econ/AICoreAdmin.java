package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;



public class AICoreAdmin extends BaseMarketConditionPlugin2 {

	private float daysThreshold = 200 + (float) Math.random() * 200;
	
	public static AICoreAdmin get(MarketAPI market) {
		MarketConditionAPI mc = market.getCondition(Conditions.AI_CORE_ADMIN);
		if (mc != null && mc.getPlugin() instanceof AICoreAdmin) {
			return (AICoreAdmin) mc.getPlugin();
		}
		return null;
	}
	
	public boolean canRemove() {
		//if (true) return false;
		return daysActive < daysThreshold;
	}
	
	@Override
	public void advance(float amount) {
		if (market.getAdmin().getAICoreId() != null) {
			float days = Global.getSector().getClock().convertToDays(amount);
			daysActive += days;
		}
	}
	
	public void apply(String id) {
	}
	
	public void unapply(String id) {
	}

	@Override
	public boolean showIcon() {
		return false;
	}
}





