package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;

public class PLStatSupplies extends BasePLStat {

	@Override
	public long getCurrentValue() {
		return (int) Global.getSector().getPlayerFleet().getCargo().getSupplies();
	}

	@Override
	public Color getGraphColor() {
		return Global.getSettings().getColor("graphSuppliesColor");
	}

	@Override
	public String getGraphLabel() {
		return "Supplies";
	}

	@Override
	public String getId() {
		return "supplies";
	}
	
	public long getGraphMax() {
		return CARGO_MAX;
	}
	
	public String getSharedCategory() {
		return "cargo_etc";
	}
	
}
