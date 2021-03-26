package com.fs.starfarer.api.impl.campaign.intel.bar;

import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

public interface PortsideBarEvent {

	void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	
	boolean isDialogFinished();
	boolean endWithContinue();
	void optionSelected(String optionText, Object optionData);
	
	
	boolean shouldRemoveEvent();
	boolean shouldShowAtMarket(MarketAPI market);

	void advance(float amount);

	void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);

	void wasShownAtMarket(MarketAPI market);

	String getBarEventId();

	boolean isAlwaysShow();


}
