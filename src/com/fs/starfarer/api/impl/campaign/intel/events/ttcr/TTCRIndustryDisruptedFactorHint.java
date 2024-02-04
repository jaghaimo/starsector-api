package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;

public class TTCRIndustryDisruptedFactorHint extends TTCRIndustryDisruptedFactor {
	
	public TTCRIndustryDisruptedFactorHint() {
		super("Industries on Tri-Tachyon colonies disrupted", 0);
		timestamp = 0; // makes it not expire
	}

	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return !hasOtherFactorsOfClass(intel, TTCRIndustryDisruptedFactorHint.class);
	}
}
