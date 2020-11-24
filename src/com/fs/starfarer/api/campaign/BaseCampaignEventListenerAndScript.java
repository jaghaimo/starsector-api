package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.EveryFrameScript;

public class BaseCampaignEventListenerAndScript extends BaseCampaignEventListener implements EveryFrameScript {

	public BaseCampaignEventListenerAndScript() {
		super(true);
	}
	public BaseCampaignEventListenerAndScript(float days) {
		super(false);
		reRegister(days);
	}

	public void advance(float amount) {
		
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}
