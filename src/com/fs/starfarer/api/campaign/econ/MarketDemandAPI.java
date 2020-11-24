package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.combat.MutableStat;

public interface MarketDemandAPI {
	
	MutableStat getDemand();
	float getDemandValue();
	CommoditySpecAPI getBaseCommodity();
	float getStockpileUtility();
}



