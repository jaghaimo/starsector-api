package com.fs.starfarer.api.impl.campaign.intel.raid;

import com.fs.starfarer.api.campaign.SectorEntityToken;

public class PirateRaidAssembleStage2 extends AssembleStage {

	public PirateRaidAssembleStage2(RaidIntel raid, SectorEntityToken gatheringPoint) {
		super(raid, gatheringPoint);
	}

	@Override
	public boolean isSourceKnown() {
		return true;
		//return base.isPlayerVisible();
	}

	
}






