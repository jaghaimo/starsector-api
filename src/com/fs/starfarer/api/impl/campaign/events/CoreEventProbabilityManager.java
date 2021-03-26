package com.fs.starfarer.api.impl.campaign.events;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.impl.campaign.ids.Events;

public class CoreEventProbabilityManager extends BaseCampaignEventListener implements EveryFrameScript {
	public static Logger log = Global.getLogger(CoreEventProbabilityManager.class);
	
	protected CampaignEventManagerAPI eventManager;
	
	public CoreEventProbabilityManager() {
		super(true);
	}
	
	protected Object readResolve() {
		return this;
	}
	
	protected boolean firstFrame = true;
	public void advance(float amount) {
		if (eventManager == null) eventManager = Global.getSector().getEventManager();
		//float days = Global.getSector().getClock().convertToDays(amount);
		
		if (firstFrame) {
			eventManager.startEvent(null, Events.REP_TRACKER, null);
			//eventManager.startEvent(null, Events.TRADE_INFO, null);
			//eventManager.startEvent(null, Events.OFFICER_MANAGER, null);
			eventManager.startEvent(null, Events.NEARBY_EVENTS, null);
			firstFrame = false;
		}
	}

	public boolean isDone() {
		return !firstFrame;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
}


















