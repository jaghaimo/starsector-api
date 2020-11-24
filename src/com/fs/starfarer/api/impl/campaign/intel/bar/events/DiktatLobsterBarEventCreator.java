package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class DiktatLobsterBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new DiktatLobsterBarEvent();
	}

	@Override
	public float getBarEventFrequencyWeight() {
		return super.getBarEventFrequencyWeight() * 0.1f;
	}

	
}
