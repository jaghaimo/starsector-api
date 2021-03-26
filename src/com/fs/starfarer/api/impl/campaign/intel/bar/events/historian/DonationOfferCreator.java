package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;

public class DonationOfferCreator extends BaseHistorianOfferCreator {

	@Override
	public HistorianOffer createOffer(Random random, List<HistorianOffer> soFar) {
		HistorianData hd = HistorianData.getInstance();
		
		if (hd.isRecentlyDonated() || hd.isMaxTier()) return null;
		
		return new DonationOffer();
	}

	@Override
	public boolean ignoresLimit() {
		return true;
	}
	
	
}







