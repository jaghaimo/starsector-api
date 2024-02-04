package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;

public interface CommissionEndedListener {
	void reportCommissionEnded(FactionCommissionIntel intel);
}
