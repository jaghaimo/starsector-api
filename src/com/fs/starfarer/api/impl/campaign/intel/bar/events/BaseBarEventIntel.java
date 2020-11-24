package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

/**
 * Unused.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public class BaseBarEventIntel extends BaseIntelPlugin implements PortsideBarEvent {
	transient protected InteractionDialogAPI dialog;


	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
	}
	
	public boolean endWithContinue() {
		return true;
	}

	public boolean isDialogFinished() {
		return false;
	}

	public void optionSelected(String optionText, Object optionData) {
		
	}

	public void advance(float amount) {
		
	}

	public boolean shouldRemoveEvent() {
		return false;
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		return false;
	}

	public void addPromptAndOption(InteractionDialogAPI dialog) {
		
	}

	public void wasShownAtMarket(MarketAPI market) {
		
	}
}
