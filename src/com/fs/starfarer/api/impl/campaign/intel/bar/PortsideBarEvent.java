package com.fs.starfarer.api.impl.campaign.intel.bar;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public interface PortsideBarEvent {

	void init(InteractionDialogAPI dialog);
	
	boolean isDialogFinished();
	boolean endWithContinue();
	void optionSelected(String optionText, Object optionData);
	
	
	boolean shouldRemoveEvent();
	boolean shouldShowAtMarket(MarketAPI market);

	void advance(float amount);

	void addPromptAndOption(InteractionDialogAPI dialog);

	void wasShownAtMarket(MarketAPI market);


}
