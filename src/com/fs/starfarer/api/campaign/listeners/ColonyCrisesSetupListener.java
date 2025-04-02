package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;

public interface ColonyCrisesSetupListener {
	void finishedAddingCrisisFactors(HostileActivityEventIntel intel);
}
