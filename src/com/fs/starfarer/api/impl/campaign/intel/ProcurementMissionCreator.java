package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager.GenericMissionCreator;

public class ProcurementMissionCreator implements GenericMissionCreator {

	public EveryFrameScript createMissionIntel() {
		return new ProcurementMissionIntel();
	}
	
	public float getMissionFrequencyWeight() {
		return 15f;
	}
}
