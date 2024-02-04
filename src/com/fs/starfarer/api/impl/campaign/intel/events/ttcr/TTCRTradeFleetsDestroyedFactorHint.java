package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;

public class TTCRTradeFleetsDestroyedFactorHint extends TTCRTradeFleetsDestroyedFactor {
	
	public TTCRTradeFleetsDestroyedFactorHint() {
		super(0);
		timestamp = 0; // makes it not expire
	}

	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return !hasOtherFactorsOfClass(intel, TTCRTradeFleetsDestroyedFactor.class);
	}
}
