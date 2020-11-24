package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

public class ConditionManager implements EconomyUpdateListener {

	public static final String KEY = "$core_marketConditionManager";
	
	public static ConditionManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (test == null) {
			test = new ConditionManager();
			Global.getSector().getMemoryWithoutUpdate().set(KEY, test);
		}
		return (ConditionManager) test; 
	}
	
	protected Object readResolve() {
		return this;
	}
	
	public ConditionManager() {
		Global.getSector().getEconomy().addUpdateListener(this);
	}
	
	
	public void economyUpdated() {
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			
			int urban = 0;
			int industrial = 0;
			int rural = 0;
			int pollution = 0;
			
			for (Industry curr : market.getIndustries()) {
				if (!curr.isIndustry()) continue;
				
				if (curr.getSpec().hasTag(Industries.TAG_URBAN)) {
					urban++;
				}
				if (curr.getSpec().hasTag(Industries.TAG_RURAL)) {
					rural++;
				}
				if (curr.getSpec().hasTag(Industries.TAG_INDUSTRIAL)) {
					industrial++;
					pollution++;
				}
			}
			
			if (market.hasCondition(Conditions.URBANIZED_POLITY)) {
				if (urban <= 0 || industrial > 0 || rural > 0) {
					market.removeCondition(Conditions.URBANIZED_POLITY);
				}
			}
			if (market.hasCondition(Conditions.RURAL_POLITY)) {
				if (rural <= 0 || urban > 0 || industrial > 0) {
					market.removeCondition(Conditions.RURAL_POLITY);
				}
			}
			if (market.hasCondition(Conditions.INDUSTRIAL_POLITY)) {
				if (industrial <= 0 || urban > 0 || rural > 0) {
					market.removeCondition(Conditions.INDUSTRIAL_POLITY);
				}
			}
			
			if (market.getSize() <= 3) {
				continue;
			}
			
			if (!market.hasCondition(Conditions.URBANIZED_POLITY)) {
				if (urban > 0 && industrial + rural <= 0) {
					market.addCondition(Conditions.URBANIZED_POLITY);
				}
			}
			if (!market.hasCondition(Conditions.RURAL_POLITY)) {
				if (rural > 0 && industrial + urban <= 0) {
					market.addCondition(Conditions.RURAL_POLITY);
				}
			}
			if (!market.hasCondition(Conditions.INDUSTRIAL_POLITY)) {
				if (industrial > 0 && rural + urban <= 0) {
					market.addCondition(Conditions.INDUSTRIAL_POLITY);
				}
			}
			
			if (!market.hasCondition(Conditions.POLLUTION) && market.hasCondition(Conditions.HABITABLE) && 
					market.getSize() >= 5 && pollution >= 3) {
				market.addCondition(Conditions.POLLUTION);
			}
			
		}
		
	}
	
	public void commodityUpdated(String commodityId) {
		
	}

	public boolean isEconomyListenerExpired() {
		return false;
	}
	
}















