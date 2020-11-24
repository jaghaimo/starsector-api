package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface StabilizeMarketPlugin extends GenericPlugin {
	
	boolean canStabilize(MarketAPI market);
	
	boolean stabilizeButtonHasTooltip(MarketAPI market);
	void createStabilizeButtonTooltip(TooltipMakerAPI info, float width, boolean expanded, MarketAPI market);
	boolean isStabilizeButtonTooltipExpandable(MarketAPI market);
	
	int getCostPerStabilityPoint(MarketAPI market);

	/**
	 * *Has* to be <= RecentUnrest.getPenalty(market).
	 * @param market
	 * @return
	 */
	int getMaxStabilizeAmount(MarketAPI market);
}
