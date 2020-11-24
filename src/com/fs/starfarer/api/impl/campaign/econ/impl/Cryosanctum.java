package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;


public class Cryosanctum extends BaseIndustry {

	public void apply() {
		super.apply(false);
		
		int size = 6;
		
		applyIncomeAndUpkeep(size);
		
		demand(Commodities.SUPPLIES, size - 3);
		demand(Commodities.ORGANICS, size - 3);
		
		supply(Commodities.ORGANS, size);
		
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS, Commodities.SUPPLIES);
		// that's right.
		if (deficit.two > 0) deficit.two = -1;
		
		applyDeficitToProduction(1, deficit, Commodities.ORGANS);
		
		if (!isFunctional()) {
			supply.clear();
		}
	}

	
	@Override
	public void unapply() {
		super.unapply();
	}


	@Override
	public boolean isAvailableToBuild() {
		return false;
	}

	public boolean showWhenUnavailable() {
		return false;
	}
	
}
