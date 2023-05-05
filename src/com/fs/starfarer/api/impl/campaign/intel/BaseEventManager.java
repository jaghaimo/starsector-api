package com.fs.starfarer.api.impl.campaign.intel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.util.IntervalUtil;

public abstract class BaseEventManager implements EveryFrameScript {

	
	protected List<EveryFrameScript> active = new ArrayList<EveryFrameScript>();
	protected IntervalUtil tracker;
	protected IntervalUtil trackerMax;
	protected int currMax = 0;
	protected Random randomBase = new Random();

	public BaseEventManager() {
		float interval = getBaseInterval();
		tracker = new IntervalUtil(interval * 0.75f, interval * 1.25f);
		
		interval = getUpdateMaxInterval();
		trackerMax = new IntervalUtil(interval * 0.75f, interval * 1.25f);
		updateMax();
		readResolve();
	}
	
	protected void updateMax() {
		int min = getMinConcurrent();
		int max = getMaxConcurrent();
		
		currMax = min + randomBase.nextInt(max - min + 1);
	}
	
	protected Object readResolve() {
		if (randomBase == null) randomBase = new Random();
		return this;
	}
	
	protected abstract int getMinConcurrent();
	protected abstract int getMaxConcurrent();
	protected abstract EveryFrameScript createEvent();
	
	
	protected float getUpdateMaxInterval() {
		return 10f;
	}
	protected float getBaseInterval() {
		return 1f;
	}
	
	protected float getIntervalRateMult() {
		return 1f;
	}
	
	protected int getHardLimit() {
		return Global.getSector().getEconomy().getNumMarkets() * 2;
	}
	
	
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
//		if (this instanceof GenericMissionManager) {
//			CountingMap<String> counts = new CountingMap<String>();
//			for (EveryFrameScript script : active) {
//				counts.add(script.getClass().getSimpleName());
//			}
//			System.out.println("-------------------------------");
//			System.out.println("MAX: " + getCurrMax());
//			for (String key : counts.keySet()) {
//				System.out.println("" + counts.getCount(key) + " <- " + key);
//			}
//			System.out.println("-------------------------------");
//		}
		
		trackerMax.advance(days);
		if (trackerMax.intervalElapsed()) {
			int count = getActiveCount();
			// if we reduced the count before, wait until the number of active events
			// drops down to match the reduction before updating currMax again
			// otherwise, since events can last a while, will usually get closer to max active
			// despite currMax periodically being low
			if (count <= currMax || count == 0) {
				if (randomBase.nextFloat() < 0.05f) {
					updateMax();
				} else {
					if (randomBase.nextFloat() < 0.5f) {
						currMax--;
					} else {
						currMax++;
					}
					
					int min = getMinConcurrent();
					int max = getMaxConcurrent();
					if (currMax < min) currMax = min;
					if (currMax > max) currMax = max;
				}
				
				int limit = getHardLimit();
				if (currMax > limit) currMax = limit;
			}
		}
		
		
		List<EveryFrameScript> remove = new ArrayList<EveryFrameScript>();
		for (EveryFrameScript event : active) {
			event.advance(amount);
			if (event.isDone()) {
				remove.add(event);
			}
		}
		active.removeAll(remove);
		//days *= 1000f;
		if (Global.getSettings().isDevMode()) {
			int count = getActiveCount();
			if (count <= 0) {
				days *= 1000f;
			}
		}
		tracker.advance(days * getIntervalRateMult());
		if (this instanceof PirateBaseManager && DebugFlags.RAID_DEBUG) {
			tracker.advance(days * 1000000f);
		}
		if (this instanceof LuddicPathBaseManager && DebugFlags.PATHER_BASE_DEBUG) {
			tracker.advance(days * 1000000f);
		}
		if (!tracker.intervalElapsed()) return;
		
		
//		if (this instanceof FactionHostilityManager) {
//			System.out.println("wefwefe");
//		}
//		if (this instanceof PirateBaseManager) {
//			System.out.println("wefwefwef");
//		}
		
		int count = getActiveCount();
		
		if (count < currMax) {
			EveryFrameScript event = createEvent();
			addActive(event);
		}
	}
	
	public int getActiveCount() {
		int count = 0;
		for (EveryFrameScript s : active) {
			if (s instanceof BaseIntelPlugin) {
				BaseIntelPlugin intel = (BaseIntelPlugin) s;
				if (intel.isEnding()) continue;
			}
			count++;
		}
		return count;
	}
	
	public int getOngoing() {
		return active.size();
	}
	
	public int getCurrMax() {
		return currMax;
	}
	
	public boolean belowMax() {
		return getCurrMax() > getOngoing();
	}
	
	public void addActive(EveryFrameScript event) {
		if (event != null) {
			active.add(event);
		} 
	}

	public boolean isDone() {
		return false;
	}
	public boolean runWhilePaused() {
		return false;
	}
	
	public List<EveryFrameScript> getActive() {
		return active;
	}
	
	public IntervalUtil getTracker() {
		return tracker;
	}



}



