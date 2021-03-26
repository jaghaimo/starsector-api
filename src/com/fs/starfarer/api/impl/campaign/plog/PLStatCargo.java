package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;

public class PLStatCargo extends BasePLStat {

	@Override
	public long getCurrentValue() {
		return (int) Global.getSector().getPlayerFleet().getCargo().getSpaceUsed();
	}

	@Override
	public Color getGraphColor() {
		return Global.getSettings().getColor("progressBarCargoColor");
	}

	@Override
	public String getGraphLabel() {
		return "Cargo";
	}

	@Override
	public String getId() {
		return "cargo";
	}
	
	public long getGraphMax() {
		return CARGO_MAX;
	}
	
	public String getSharedCategory() {
		return "cargo_etc";
	}
	
}
