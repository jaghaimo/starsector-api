package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class BaseBarEvent implements PortsideBarEvent {

	transient protected InteractionDialogAPI dialog;
	transient protected TextPanelAPI text;
	transient protected OptionPanelAPI options;
	transient protected Map<String, MemoryAPI> memoryMap;

	public boolean isAlwaysShow() {
		return false;
	}

	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		if (dialog != null) {
			text = dialog.getTextPanel();
			options = dialog.getOptionPanel();
		}
	}
	
	public String getBarEventId() {
		return getClass().getSimpleName();
	}
	
	transient protected boolean noContinue = false;
	public boolean endWithContinue() {
		return !noContinue;
	}

	transient protected boolean done = false;
	public boolean isDialogFinished() {
		return done;
	}

	public void optionSelected(String optionText, Object optionData) {
		
	}

	public void advance(float amount) {
		
	}

	public boolean shouldRemoveEvent() {
		return false;
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		if (shownAt != null && shownAt != market) return false;
		return true;
	}

	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
	}

	protected MarketAPI shownAt = null;
	public void wasShownAtMarket(MarketAPI market) {
		shownAt = market;
	}

	public MarketAPI getShownAt() {
		return shownAt;
	}
	
}
