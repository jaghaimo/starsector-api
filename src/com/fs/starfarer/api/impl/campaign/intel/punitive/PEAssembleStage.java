package com.fs.starfarer.api.impl.campaign.intel.punitive;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.raid.AssembleStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PEAssembleStage extends AssembleStage {
	
	public PEAssembleStage(RaidIntel raid, SectorEntityToken gatheringPoint) {
		super(raid, gatheringPoint);
	}

	public void showStageInfo(TooltipMakerAPI info) {
		int curr = intel.getCurrentStage();
		int index = intel.getStageIndex(this);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (status == RaidStageStatus.FAILURE) {
			info.addPara("The expeditionary force has failed to successfully assemble at the rendezvous point.", opad);
		} else if (curr == index) {
			if (isSourceKnown()) {
				info.addPara("The expeditionary force is currently assembling in the " + gatheringPoint.getContainingLocation().getNameWithLowercaseType() + ".", opad);
			} else {
				info.addPara("The expeditionary force is currently assembling at an unknown location.", opad);
			}
		}
	}
	

	@Override
	protected String pickNextType() {
		return FleetTypes.TASK_FORCE;
	}
	
	@Override
	protected float getFP(String type) {
		float fp = getLargeSize(true);
		spawnFP -= fp;
		return fp;
//		float base = 100f;
//		if (spawnFP < base * 1.5f) {
//			base = spawnFP;
//		}
//		if (base > spawnFP) base = spawnFP;
//		
//		spawnFP -= base;
//		return base;
	}
}





