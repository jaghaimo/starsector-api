package com.fs.starfarer.api.impl.campaign.shared;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.RollingAverageTracker;
import com.fs.starfarer.api.util.TimeoutTracker;

public class StarSystemActivityTracker extends BaseCampaignEventListener {

	public static Logger log = Global.getLogger(StarSystemActivityTracker.class);
	public static final float ROLLING_AVG_FACTOR = 0.5f;
	
	private float econInterval = Global.getSettings().getFloat("economyIntervalnGameDays");
	
	private IntervalUtil timer = new IntervalUtil(0.25f, .75f);
	private TimeoutTracker<String> seen = new TimeoutTracker<String>();
	private StarSystemAPI system;
	
	private RollingAverageTracker points, fleets, ships;
	
	public StarSystemActivityTracker(StarSystemAPI system) {
		super(false);
		
		this.system = system;
		
		points = new RollingAverageTracker(econInterval - Math.min(econInterval * 0.5f, 2f),
										   econInterval +  - Math.min(econInterval * 0.5f, 2f),
										   ROLLING_AVG_FACTOR);
		fleets = new RollingAverageTracker(econInterval - Math.min(econInterval * 0.5f, 2f),
										   econInterval +  - Math.min(econInterval * 0.5f, 2f),
										   ROLLING_AVG_FACTOR);
		ships = new RollingAverageTracker(econInterval - Math.min(econInterval * 0.5f, 2f),
										   econInterval +  - Math.min(econInterval * 0.5f, 2f),
										   ROLLING_AVG_FACTOR);
	}

	
	Object readResolve() {
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	public void advance(float days) {
		seen.advance(days);
		
		timer.advance(days);
		if (timer.intervalElapsed()) {
			for (CampaignFleetAPI fleet : system.getFleets()) {
				if (!seen.contains(fleet.getId())) {
					seen.set(fleet.getId(), econInterval);
					
					points.add(fleet.getFleetPoints());
					ships.add(fleet.getFleetData().getMembersListCopy().size());
					fleets.add(1);
				}
			}
			
			for (CampaignFleetAPI fleet : Global.getSector().getHyperspace().getFleets()) {
				float dist = Misc.getDistance(fleet.getLocation(), system.getLocation());
				//if (!seen.contains(fleet) && dist < 1000) {
				if (!seen.contains(fleet.getId()) && dist < Global.getSettings().getFloat("commRelayRangeAroundSystem")) {
					seen.set(fleet.getId(), econInterval);
					
					points.add(fleet.getFleetPoints());
					ships.add(fleet.getFleetData().getMembersListCopy().size());
					fleets.add(1);
				}
			}
		}

		points.advance(days);
		fleets.advance(days);
		ships.advance(days);

	}
	
	

	public StarSystemAPI getSystem() {
		return system;
	}

	public float getPointsSeen() {
		return points.getAverage();
	}

	public float getFleetsSeen() {
		return fleets.getAverage();
	}

	public float getShipsSeen() {
		return ships.getAverage();
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {

//		if (!primaryWinner.isInOrNearSystem(system)) return;
//		
//		int minSize = 4;
//		
//		//for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//		for (MarketAPI market : Misc.getMarketsInLocation(system)) {
//			if (market.getSize() < minSize) continue;
//			
//			if (market.getFaction().getCustom().optBoolean(Factions.CUSTOM_POSTS_NO_BOUNTIES)) {
//				continue;
//			}
//			
//			for (CampaignFleetAPI winner : battle.getSnapshotSideFor(primaryWinner)) {
//				increaseBountyProbability(winner, market);
//			}
//			
//			for (CampaignFleetAPI loser : battle.getOtherSideSnapshotFor(primaryWinner)) {
//				increaseBountyProbability(loser, market);
//			}
//		}
	}
	
	
//	private void increaseBountyProbability(CampaignFleetAPI fleetWithLosses, MarketAPI market) {
//		CampaignEventManagerAPI eventManager = Global.getSector().getEventManager();
//		EventProbabilityAPI ep = eventManager.getProbability(Events.SYSTEM_BOUNTY, market);
//		if (!eventManager.isOngoing(ep)) {
//			float fpLost = Misc.getSnapshotFPLost(fleetWithLosses);
//			if (fpLost < 10) fpLost = 10;
//			float fpSeen = points.getAverage();
//			if (fpSeen < 1) fpSeen = 1;
//			float f = fpLost / fpSeen;
//			if (f > 1) f = 1;
//			//float probabilityIncrease = f * 10f;
//			float probabilityIncrease = f * 0.05f;
//			if (probabilityIncrease > 0) {
//				ep.increaseProbability(probabilityIncrease);
//				log.info("Increasing system bounty probability for " + market.getName() + " by " + probabilityIncrease + ", is now " + ep.getProbability());
//			}
//		}
//	}
}









