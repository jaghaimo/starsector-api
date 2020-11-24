package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.StabilizeMarketPlugin;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class StabilizeMarketPluginImpl implements StabilizeMarketPlugin {

	public boolean canStabilize(MarketAPI market) {
		return getMaxStabilizeAmount(market) > 0;
	}

	public void createStabilizeButtonTooltip(TooltipMakerAPI info, float width, boolean expanded, MarketAPI market) {
		info.addPara("Can only stabilize colonies with at least %s points of recent unrest.", 0f,
				Misc.getHighlightColor(), "" + 2);
	}

	public int getMaxStabilizeAmount(MarketAPI market) {
		return Math.max(0, RecentUnrest.getPenalty(market) - 1);
	}
	
	public int getCostPerStabilityPoint(MarketAPI market) {
		int base = Global.getSettings().getInt("baseStabilizeCost");
		return Math.max(1, market.getSize() - 2) * base;
	}

	public boolean isStabilizeButtonTooltipExpandable(MarketAPI market) {
		return false;
	}

	public boolean stabilizeButtonHasTooltip(MarketAPI market) {
		return !canStabilize(market);
	}

	public int getHandlingPriority(Object params) {
		return 0;
	}
}
