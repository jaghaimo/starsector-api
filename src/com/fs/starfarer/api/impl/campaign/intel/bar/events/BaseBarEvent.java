package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class BaseBarEvent implements PortsideBarEvent {

	transient protected InteractionDialogAPI dialog;


	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
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

	public void addPromptAndOption(InteractionDialogAPI dialog) {
		
	}

	protected MarketAPI shownAt = null;
	public void wasShownAtMarket(MarketAPI market) {
		shownAt = market;
	}

	public MarketAPI getShownAt() {
		return shownAt;
	}
	
}
