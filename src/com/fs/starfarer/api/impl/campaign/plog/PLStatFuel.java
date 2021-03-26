package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;

public class PLStatFuel extends BasePLStat {

	@Override
	public long getCurrentValue() {
		return (int) Global.getSector().getPlayerFleet().getCargo().getFuel();
	}

	@Override
	public Color getGraphColor() {
		return Global.getSettings().getColor("progressBarFuelColor");
	}

	@Override
	public String getGraphLabel() {
		return "Fuel";
	}

	@Override
	public String getId() {
		return "fuel";
	}
	
	public long getGraphMax() {
		return CARGO_MAX;
	}
	
	public String getSharedCategory() {
		return "cargo_etc";
	}
	
}
