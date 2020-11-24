package com.fs.starfarer.api.impl.campaign.intel.raid;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ReturnStage extends BaseRaidStage {

	public ReturnStage(RaidIntel raid) {
		super(raid);
	}

	@Override
	public void notifyStarted() {
		updateRoutes();
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
	}

	protected void updateRoutes() {
		giveReturnOrdersToStragglers(getRoutes());
		maxDays = 3f;
		//intel.setExtraDays(0f);
	}
	
	protected void updateStatus() {
		status = RaidStageStatus.SUCCESS;
	}
	
	public void showStageInfo(TooltipMakerAPI info) {
		int curr = intel.getCurrentStage();
		int index = intel.getStageIndex(this);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (curr >= index) {
			info.addPara("The raid is over, and the fleets involved are returning to their home bases.", opad);
		}
	}
}
