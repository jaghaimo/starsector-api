package com.fs.starfarer.api.impl.campaign.intel.raid;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;

public class PirateRaidAssembleStage extends AssembleStage {

	protected BaseIntelPlugin base;

	public PirateRaidAssembleStage(RaidIntel raid, SectorEntityToken gatheringPoint, BaseIntelPlugin base) {
		super(raid, gatheringPoint);
		this.base = base;
	}

	@Override
	public boolean isSourceKnown() {
		return base.isPlayerVisible();
	}

	
}






