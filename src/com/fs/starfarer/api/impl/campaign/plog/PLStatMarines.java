package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;

public class PLStatMarines extends BasePLStat {

	@Override
	public long getCurrentValue() {
		return (int) Global.getSector().getPlayerFleet().getCargo().getMarines();
	}

	@Override
	public Color getGraphColor() {
		return Global.getSettings().getColor("graphMarinesColor");
	}

	@Override
	public String getGraphLabel() {
		return "Marines";
	}

	@Override
	public String getId() {
		return "marines";
	}
	
	public long getGraphMax() {
		return CARGO_MAX;
	}
	
	public String getSharedCategory() {
		return "cargo_etc";
	}
}
