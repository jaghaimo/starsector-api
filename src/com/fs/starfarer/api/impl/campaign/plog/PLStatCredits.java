package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class PLStatCredits extends BasePLStat {

	@Override
	public long getCurrentValue() {
		return (long) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
	}

	@Override
	public Color getGraphColor() {
		return Misc.getHighlightColor();
	}

	@Override
	public String getGraphLabel() {
		return "Credits";
	}

	@Override
	public String getId() {
		return "credits";
	}
	
	public long getGraphMax() {
		return CREDITS_MAX;
	}
	
	public String getHoverText(long value) {
		return Misc.getDGSCredits(value);
	}
}
