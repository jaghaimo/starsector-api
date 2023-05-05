package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HostileActivityManager implements EveryFrameScript {

	protected IntervalUtil tracker = new IntervalUtil(0.5f, 1.5f);
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public void advance(float amount) {
		tracker.advance(amount);
		if (tracker.intervalElapsed()) {
			boolean playerHasColonies = !Misc.getPlayerMarkets(false).isEmpty();
			if (HostileActivityEventIntel.get() == null && playerHasColonies) {
				new HostileActivityEventIntel();
			} else if (HostileActivityEventIntel.get() != null && !playerHasColonies) {
				HostileActivityEventIntel.get().endImmediately();
			}
		}
	}

}
