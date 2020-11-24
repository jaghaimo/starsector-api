package com.fs.starfarer.api.impl.campaign.intel.punitive;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.TravelStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PETravelStage extends TravelStage {

	public PETravelStage(RaidIntel raid, SectorEntityToken from, SectorEntityToken to, boolean requireNearTarget) {
		super(raid, from, to, requireNearTarget);
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
			info.addPara("The expeditionary force has failed to successfully reach the " +
					intel.getSystem().getNameWithLowercaseType() + ".", opad);
		} else if (curr == index) {
			info.addPara("The expeditionary force is currently travelling to the " + 
					intel.getSystem().getNameWithLowercaseType() + ".", opad);
		}
	}
}



