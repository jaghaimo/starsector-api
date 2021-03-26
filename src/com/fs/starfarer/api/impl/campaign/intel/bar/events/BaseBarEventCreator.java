package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.GenericBarEventCreator;

public class BaseBarEventCreator implements GenericBarEventCreator {

	public PortsideBarEvent createBarEvent() {
		return null;
	}

	public float getBarEventActiveDuration() {
		return 30f + (float) Math.random() * 10f;
	}

	public float getBarEventFrequencyWeight() {
		return 10f;
	}

	public float getBarEventTimeoutDuration() {
		return 30f + (float) Math.random() * 10f;
	}

	public float getBarEventAcceptedTimeoutDuration() {
		return 60f + (float) Math.random() * 30f;
	}

	public boolean isPriority() {
		return false;
	}

	public String getBarEventId() {
		return getClass().getSimpleName();
	}

	public boolean wasAutoAdded() {
		return false;
	}

}
