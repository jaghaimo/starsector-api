package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;

public class TTCRCommerceRaidersDestroyedFactorHint extends TTCRCommerceRaidersDestroyedFactor {
	
	public TTCRCommerceRaidersDestroyedFactorHint() {
		super(0);
		timestamp = 0; // makes it not expire
	}

	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return !hasOtherFactorsOfClass(intel, TTCRCommerceRaidersDestroyedFactor.class);
	}
}
