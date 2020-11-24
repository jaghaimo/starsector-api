package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class ScientistAICoreBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new ScientistAICoreBarEvent();
	}

	@Override
	public float getBarEventAcceptedTimeoutDuration() {
		//return 120f + (float) Math.random() * 120f;
		return 10000000000f; // will reset when intel ends... or not, if keeping this one-time-only
	}
	
}
