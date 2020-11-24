package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;

public class PlanetaryShieldBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new PlanetaryShieldBarEvent();
	}

	@Override
	public float getBarEventAcceptedTimeoutDuration() {
		return 10000000000f; // one-time-only
	}

	@Override
	public float getBarEventFrequencyWeight() {
		if (!Global.getSector().getMemoryWithoutUpdate().contains(MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET_KEY)) {
			return 0f;
		}
		return super.getBarEventFrequencyWeight();
	}
	
	
	
}
