package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface AbandonMarketPlugin extends GenericPlugin {
	
	boolean canAbandon(MarketAPI market);
	//int getAbandonCost(MarketAPI market);
	
	void createAbandonButtonTooltip(TooltipMakerAPI info, float width, boolean expanded, MarketAPI market);
	boolean abandonButtonHasTooltip(MarketAPI market);
	boolean isAbandonButtonTooltipExpandable(MarketAPI market);
	float getConfirmationPromptWidth(MarketAPI market);
	void createConfirmationPrompt(MarketAPI market, TooltipMakerAPI prompt);
	void abandonConfirmed(MarketAPI market);
	boolean isConfirmEnabled(MarketAPI market);
	
}
