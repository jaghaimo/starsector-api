package com.fs.starfarer.api.campaign.events;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.CallEvent.CallableEvent;
import com.fs.starfarer.api.util.Misc.Token;


public interface CampaignEventPlugin extends CallableEvent {

	//public static final String SUBJECT = "cep_subject";
	
	public static enum CampaignEventCategory {
		EVENT,
		BOUNTY,
		MISSION,
		DO_NOT_SHOW_IN_MESSAGE_FILTER,
	}
	
	public static interface PriceUpdatePlugin {
		public static enum PriceType {
			CHEAP,
			EXPENSIVE,
			NORMAL,
		}
		
		MarketAPI getMarket();
		CommodityOnMarketAPI getCommodity();
		float getSupplyPrice();
		float getDemandPrice();
		PriceType getType();
		long getTimestamp();
		void updateType();
		float getDemand();
		float getAvailable();
		int getRoundedPriceForDisplay();
	}
	
	
	
	/**
	 * Unique ID for this instance of the event.
	 * @return
	 */
	String getId();
	
	/**
	 * Called when the EventProbability for this event is accessed.
	 * Doesn't mean the event will actually happen.
	 * @param eventType
	 * @param eventTarget
	 */
	void init(String eventType, CampaignEventTarget eventTarget);
	
	
	/**
	 * Called when this instance of the event is removed from the event manager (either due to event being over,
	 * or due to event probability dropping to 0). 
	 */
	void cleanup();
	
	
	/**
	 * The probability that the event had of happening, set right before startEvent() is called.
	 * Set to 1 by events started using CampaignEventManagerAPI.startEvent().
	 * @param p
	 */
	void setProbability(float p);
	
	/**
	 * Called when the event starts.
	 */
	void startEvent();
	
	void advance(float amount);
	boolean isDone();
	
	CampaignEventTarget getEventTarget();
	String getEventType();
	
	
	/**
	 * Token values for filling out descriptions from reports.csv.
	 * @return
	 */
	Map<String, String> getTokenReplacements();
	
	/**
	 * Since multiple reports (possibly from different channels) are possible per stage:
	 * 1) All reports must have the highlighted text occur in the same order, which is the order
	 * this method returns them in, and
	 * 2) Not all highlights have to occur in every report.
	 * @param stageId
	 * @return
	 */
	String[] getHighlights(String stageId);
	
	/**
	 * Since multiple reports (possibly from different channels) are possible per stage:
	 * 1) All reports must have the highlighted text occur in the same order, which is the order
	 * this method returns them in, and
	 * 2) Not all highlights have to occur in every report.
	 * @param stageId
	 * @return
	 */
	Color[] getHighlightColors(String stageId);
	
	
	/**
	 * event_stage for when the event is possible, but hasn't happened yet.
	 * @return
	 */
	String getStageIdForPossible();
	
	
	/**
	 * Message priority for the "event is possible" report.
	 * @return
	 */
	MessagePriority getWarningWhenPossiblePriority();
	
	
	/**
	 * event_stage for when the event is likely, but hasn't happened yet.
	 * @return
	 */
	String getStageIdForLikely();
	
	/**
	 * Message priority for the "event is likely" report.
	 * @return
	 */
	MessagePriority getWarningWhenLikelyPriority();
	
	
	/**
	 * Only called when an event is started via CampaignEventManagerAPI.startEvent().
	 * @param param
	 */
	void setParam(Object param);
	
	/**
	 * DO NOT USE, DOES NOT WORK.
	 * 
	 * Should always return false.
	 * 
	 * @return
	 */
	@Deprecated boolean allowMultipleOngoingForSameTarget();
	
	String getEventName();
	boolean useEventNameAsId();
	
	CampaignEventCategory getEventCategory();
	
	List<String> getRelatedCommodities();
	List<PriceUpdatePlugin> getPriceUpdates();
	
	/**
	 * Will be called by SectorAPI.reportEventStage(). Can change between calls.
	 * 
	 * The priority for icons is:
	 * 1) Icon configured in reports.csv for the specific report
	 * 2) Return value of this method
	 * 3) Event's icon (via getEventIcon() and then from events.json)
	 * 4) Channel's icon, if message isn't related to an event or the event has no icon.
	 *  
	 * @return
	 */
	String getCurrentMessageIcon();


	/**
	 * The larger image in the message detail.
	 * @return
	 */
	String getCurrentImage();


	/**
	 * Override for the "image" normally specified in events.json. Small.
	 * @return
	 */
	String getEventIcon();
	
	
	/**
	 * If true, messages for an ongoing event will be shown in the intel UI even if
	 * they don't meet the "last week/month/cycle" criteria.
	 * @return
	 */
	boolean showAllMessagesIfOngoing();
	
	
	/**
	 * Called by the CallEvent command (called from rules.csv).
	 * @param memoryMap
	 * @param params
	 */
	boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap);
	
	MemoryAPI getMemory();

	boolean showLatestMessageIfOngoing();
}




