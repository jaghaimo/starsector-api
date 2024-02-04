package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.util.IntervalUtil;

//public abstract class BaseLimitedFleetManager extends BaseCampaignEventListener implements EveryFrameScript {
/**
 * Default spawn rate is on average one fleet every month. 
 * Deriving classes should boost this as necessary via getSpawnRateMult().
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public abstract class BaseLimitedFleetManager implements EveryFrameScript, FleetEventListener {

	public static Logger log = Global.getLogger(BaseLimitedFleetManager.class);
	
	public static class ManagedFleetData {
		//public float startingFleetPoints = 0;
		public CampaignFleetAPI fleet;
		public LocationAPI spawnedFor;
		public ManagedFleetData(CampaignFleetAPI fleet, LocationAPI spawnedFor) {
			this.fleet = fleet;
			this.spawnedFor = spawnedFor;
			//startingFleetPoints = fleet.getFleetPoints();
		}
	}
	
	protected List<ManagedFleetData> active = new ArrayList<ManagedFleetData>();
	protected IntervalUtil tracker;

	public BaseLimitedFleetManager() {
		float interval = 30f;
		tracker = new IntervalUtil(interval * 0.75f, interval * 1.25f);
		readResolve();
	}
	
	protected Object readResolve() {
		return this;
	}
	
	protected abstract int getMaxFleets();
	protected abstract CampaignFleetAPI spawnFleet();
	
	protected float getNextInterval() {
		return 30f * (0.75f + (float) Math.random() * 0.5f); 
	}
	
	protected float getSpawnRateMult() {
		return 1f;
	}
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		tracker.advance(days * getSpawnRateMult());
		if (!tracker.intervalElapsed()) return;
		
		float next = getNextInterval();
		tracker.setInterval(next, next);
		
		Global.getSettings().profilerBegin(this.getClass().getSimpleName() + ".advance()");
		List<ManagedFleetData> remove = new ArrayList<ManagedFleetData>();
		for (ManagedFleetData data : active) {
			if (data.fleet.getContainingLocation() == null ||
				!data.fleet.getContainingLocation().getFleets().contains(data.fleet)) {
				remove.add(data);
				log.info("Cleaning up orphaned fleet [" + data.fleet.getNameWithFaction() + "]");
			}
		}
		active.removeAll(remove);
		
		int max = getMaxFleets();
		
		if (active.size() < max) {
			//log.info(active.size() + " out of a maximum " + max + " fleets in play for [" + getClass().getName() + "]");
			CampaignFleetAPI fleet = spawnFleet();
			if (fleet != null) {
				fleet.addEventListener(this);
				LocationAPI spawnLoc = null;
				if (this instanceof DisposableFleetManager) {
					spawnLoc = ((DisposableFleetManager)this).getCurrSpawnLoc();
				}
				ManagedFleetData data = new ManagedFleetData(fleet, spawnLoc);
				active.add(data);
				log.info("Spawned fleet [" + fleet.getNameWithFaction() + "] at hyperloc " + fleet.getLocationInHyperspace());
			} else {
				//log.info("Could not spawn fleet - returned null");
			}
		} else {
			log.debug("Maximum number of " + max + " fleets already in play for [" + getClass().getName() + "]");
		}
		Global.getSettings().profilerEnd();
	}
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		for (ManagedFleetData data : active) {
			if (data.fleet == fleet) {
				active.remove(data);
				break;
			}
		}
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
	}

}














