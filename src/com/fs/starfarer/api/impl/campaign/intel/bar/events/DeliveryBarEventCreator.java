package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class DeliveryBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new DeliveryBarEvent();
	}

	@Override
	public boolean isPriority() {
		return true;
	}

	
	// re-roll fairly often, with no timeout, so there's almost always something
	public float getBarEventActiveDuration() {
		return 15f + (float) Math.random() * 15f;
	}

	public float getBarEventTimeoutDuration() {
		return 0f; // unless the player accepts, always keep one going
	}

	@Override
	public float getBarEventAcceptedTimeoutDuration() {
		return Math.max(0, 30f - (float) Math.random() * 40f);
	}
}




