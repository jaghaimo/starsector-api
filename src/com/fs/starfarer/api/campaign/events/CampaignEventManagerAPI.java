package com.fs.starfarer.api.campaign.events;

import java.util.List;

import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public interface CampaignEventManagerAPI {
	EventProbabilityAPI getProbability(String eventType, SectorEntityToken eventTargetEntity);
	EventProbabilityAPI getProbability(String eventType, LocationAPI eventTargetLocation);
	EventProbabilityAPI getProbability(String eventType, CampaignEventTarget target);
	EventProbabilityAPI getProbability(String eventType, MarketAPI market);
	EventProbabilityAPI getProbability(String eventType, Object custom);
	
	/**
	 * Whether an event associated with this probability is already ongoing.
	 * Only one event of a given type can occur at a time for a given event target.
	 * @param ep
	 * @return
	 */
	boolean isOngoing(EventProbabilityAPI ep);
	boolean isOngoing(CampaignEventTarget eventTarget, String eventType);
	
	/**
	 * Starts an event immediately, bypassing any probability checks. Returns null
	 * if failed to start the event because there's already an ongoing event of the
	 * same type, or the event plugin otherwise.
	 * 
	 * param gets passed in to the event via CampaignEventPlugin.setParam().
	 * 
	 * @param eventTarget can be null if the event doesn't have a specific target.
	 * @param eventType
	 * @param param
	 * @return
	 */
	CampaignEventPlugin startEvent(CampaignEventTarget eventTarget, String eventType, Object param);
	
	
	CampaignEventPlugin getOngoingEvent(CampaignEventTarget eventTarget, String eventType);
	void endEvent(CampaignEventPlugin event);
	
	/**
	 * Initialize the event plugin, but don't actually start the event.
	 * Useful if the event is needed for doing token replacement in reports, for example, but
	 * it's not desired for the event to start.
	 * 
	 * Will return null if the event can't be created for any reason - for example, if another event
	 * of the same type is ongoing for this target and the event doesn't allow multiple ongoing
	 * at the same time.
	 * 
	 * @param eventTarget
	 * @param eventType
	 * @param param
	 * @return
	 */
	CampaignEventPlugin primeEvent(CampaignEventTarget eventTarget, String eventType, Object param);
	
	/**
	 * Start an event previously created with primeEvent()
	 * @param primedEvent
	 */
	void startEvent(CampaignEventPlugin primedEvent);
	int getNumOngoing(String eventType);
	List<CampaignEventPlugin> getOngoingEvents();
}



