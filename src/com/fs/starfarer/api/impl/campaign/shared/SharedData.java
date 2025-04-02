/**
 * 
 */
package com.fs.starfarer.api.impl.campaign.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.impl.campaign.CoreScript;
import com.fs.starfarer.api.util.TimeoutTracker;

/**
 * Assorted bits of shared data within a single campaign. NOT across campaigns.
 * @author Alex
 *
 */
public class SharedData {
	
	public static class UniqueEncounterData {
		public List<String> interactedWith = new ArrayList<String>();
		public List<String> historianBlurbsShown = new ArrayList<String>();
		
		public boolean wasInteractedWith(String id) {
			return interactedWith.contains(id);
		}
		
		public void setWasInteractedWith(String id) {
			interactedWith.add(id);
		}
		
		protected Object readResolve() {
			if (historianBlurbsShown == null) {
				historianBlurbsShown = new ArrayList<String>();
			}
			return this;
		}
		
	}
	
	protected TimeoutTracker<String> marketsThatSentRelief = new TimeoutTracker<String>();
	protected TimeoutTracker<String> marketsThatSentTradeFleet = new TimeoutTracker<String>();
	//private TimeoutTracker<String> starSystemCustomsTimeout = new TimeoutTracker<String>();
	
	// faction, then star system
	protected Map<String, TimeoutTracker<String>> starSystemCustomsTimeout = new LinkedHashMap<String, TimeoutTracker<String>>();
	
	//protected SectorActivityTracker activityTracker = new SectorActivityTracker();
	protected PlayerActivityTracker playerActivityTracker = new PlayerActivityTracker();
	
	protected Set<String> marketsWithoutTradeFleetSpawn = new HashSet<String>();
	//protected Set<String> marketsWithoutPatrolSpawn = new HashSet<String>();
	
	private PersonBountyEventData personBountyEventData = new PersonBountyEventData();

	protected float playerPreLosingBattleFP = -1;
	protected float playerPreLosingBattleCrew = -1;
	protected long playerLosingBattleTimestamp = 0;
	
	protected MonthlyReport previousReport = new MonthlyReport();
	protected MonthlyReport currentReport = new MonthlyReport();
	
	protected UniqueEncounterData uniqueEncounterData = new UniqueEncounterData();
	
	public SharedData() {
	}
	
	public MonthlyReport getPreviousReport() {
		if (previousReport == null) previousReport = new MonthlyReport();
		return previousReport;
	}
	public MonthlyReport getCurrentReport() {
		if (currentReport == null) currentReport = new MonthlyReport();
		return currentReport;
	}
	public void setCurrentReport(MonthlyReport currentReport) {
		this.currentReport = currentReport;
	}
	public void setPreviousReport(MonthlyReport previousReport) {
		this.previousReport = previousReport;
	}
	
	public void rollOverReport() {
		previousReport = currentReport;
		currentReport = new MonthlyReport();
	}

	public long getPlayerLosingBattleTimestamp() {
		return playerLosingBattleTimestamp;
	}

	public void setPlayerLosingBattleTimestamp(long playerLosingBattleTimestamp) {
		this.playerLosingBattleTimestamp = playerLosingBattleTimestamp;
	}

	public float getPlayerPreLosingBattleFP() {
		return playerPreLosingBattleFP;
	}

	public void setPlayerPreLosingBattleFP(float playerPreLosingBattleFP) {
		this.playerPreLosingBattleFP = playerPreLosingBattleFP;
	}

	public float getPlayerPreLosingBattleCrew() {
		return playerPreLosingBattleCrew;
	}

	public void setPlayerPreLosingBattleCrew(float playerPreLosingBattleCrew) {
		this.playerPreLosingBattleCrew = playerPreLosingBattleCrew;
	}

	public UniqueEncounterData getUniqueEncounterData() {
		return uniqueEncounterData;
	}

	protected Object readResolve() {
		if (starSystemCustomsTimeout == null) {
			starSystemCustomsTimeout = new LinkedHashMap<String, TimeoutTracker<String>>();
		}
		if (personBountyEventData == null) {
			personBountyEventData = new PersonBountyEventData();
		}
		if (uniqueEncounterData == null) {
			uniqueEncounterData = new UniqueEncounterData();
		}
		return this;
	}

	public PersonBountyEventData getPersonBountyEventData() {
		return personBountyEventData;
	}

	public void advance(float amount) {
		
		SectorAPI sector = Global.getSector();
		if (sector.isPaused()) {
			return;
		}
		
		float days = sector.getClock().convertToDays(amount);
		
		marketsThatSentRelief.advance(days);
		marketsThatSentTradeFleet.advance(days);
		
		for (TimeoutTracker<String> curr : starSystemCustomsTimeout.values()) {
			curr.advance(days);
		}
		
		//activityTracker.advance(days);
		playerActivityTracker.advance(days);
	}
	

	
//	public SectorActivityTracker getActivityTracker() {
//		return activityTracker;
//	}

	public TimeoutTracker<String> getMarketsThatSentRelief() {
		return marketsThatSentRelief;
	}
	public TimeoutTracker<String> getMarketsThatSentTradeFleet() {
		return marketsThatSentTradeFleet;
	}
	
	public PlayerActivityTracker getPlayerActivityTracker() {
		return playerActivityTracker;
	}

	public static SharedData getData() {
		Object data = Global.getSector().getPersistentData().get(CoreScript.SHARED_DATA_KEY);
		if (data == null) {
			data = new SharedData();
			Global.getSector().getPersistentData().put(CoreScript.SHARED_DATA_KEY, data);
		}
		return (SharedData) data;
	}

	public Set<String> getMarketsWithoutTradeFleetSpawn() {
		return marketsWithoutTradeFleetSpawn;
	}


	public void resetCustomsTimeouts() {
		starSystemCustomsTimeout.clear();
	}
	
	public TimeoutTracker<String> getStarSystemCustomsTimeout(String factionId) {
		TimeoutTracker<String> tracker = starSystemCustomsTimeout.get(factionId);
		if (tracker == null) {
			tracker = new TimeoutTracker<String>();
			starSystemCustomsTimeout.put(factionId, tracker);
		}
		return tracker;
	}
	
}




