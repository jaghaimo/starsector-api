package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class HistorianBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new HistorianBarEvent();
	}

	@Override
	public boolean isPriority() {
		return false;
	}
	
	public float getBarEventActiveDuration() {
		return 15f + (float) Math.random() * 15f;
	}

	public float getBarEventTimeoutDuration() {
		return Math.max(0, 30f - (float) Math.random() * 40f);
	}

	@Override
	public float getBarEventAcceptedTimeoutDuration() {
		return 30f + (float) Math.random() * 30f;
	}
	
}
