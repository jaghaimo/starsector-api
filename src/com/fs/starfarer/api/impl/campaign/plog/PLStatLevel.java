package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class PLStatLevel extends BasePLStat {

	@Override
	public long getCurrentValue() {
		return (int) Global.getSector().getPlayerStats().getLevel();
	}

	@Override
	public Color getGraphColor() {
		//return Misc.getBrightPlayerColor();
		return Misc.getStoryOptionColor();
	}

	@Override
	public String getGraphLabel() {
		return "Level";
	}

	@Override
	public String getId() {
		return "level";
	}
	
	public long getGraphMax() {
		return (long) Global.getSettings().getFloat("playerMaxLevel");
	}
	
}
