package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.util.IntervalUtil;

public abstract class BaseRouteFleetManager implements EveryFrameScript, RouteFleetSpawner {

	protected IntervalUtil interval;
	
	public BaseRouteFleetManager(float minInterval, float maxInterval) {
		interval = new IntervalUtil(minInterval, maxInterval);
	}
	
	protected abstract String getRouteSourceId();
	protected abstract int getMaxFleets();
	protected abstract void addRouteFleetIfPossible();
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
//		boolean econ = EconomyFleetRouteManager.class.isInstance(this);
//		if (econ) {
//			days *= 1000f;
//		}
		
		interval.advance(days);
		if (interval.intervalElapsed()) {
			String id = getRouteSourceId();
			int max = getMaxFleets();
//			if (econ) {
//				max = 1;
//			}
			int curr = RouteManager.getInstance().getNumRoutesFor(id);
			if (curr >= max) return;
			
			addRouteFleetIfPossible();
		}
	}
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
