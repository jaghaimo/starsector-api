package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class BaseHostileActivityCause2 implements HostileActivityCause2 {

	protected HostileActivityEventIntel intel;
	
	public BaseHostileActivityCause2(HostileActivityEventIntel intel) {
		this.intel = intel;
	}

	public float getMagnitudeContribution(StarSystemAPI system) {
		return 0;
	}

	public int getProgress() {
		return 0;
	}

	public boolean shouldShow() {
		return getProgress() != 0;
	}

	public String getDesc() {
		return null;
	}

	public String getProgressStr() {
		int p = getProgress();
		if (p > 0) {
			return "+" + p;
		}
		return "" + p;
	}

	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress() == 0) {
			return Misc.getGrayColor();
		}
		return Misc.getTextColor();
	}

	public Color getProgressColor(BaseEventIntel intel) {
		return intel.getProgressColor(getProgress());
	}

	public TooltipCreator getTooltip() {
		return null;
	}
	
	public void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel) {
		
	}
}

