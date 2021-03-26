package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;

public class PLStatCrew extends BasePLStat {

	@Override
	public long getCurrentValue() {
		return (int) Global.getSector().getPlayerFleet().getCargo().getCrew();
	}

	@Override
	public Color getGraphColor() {
		return Global.getSettings().getColor("progressBarCrewColor");
	}

	@Override
	public String getGraphLabel() {
		return "Crew";
	}

	@Override
	public String getId() {
		return "crew";
	}

	public long getGraphMax() {
		return CARGO_MAX;
	}
	
	public String getSharedCategory() {
		return "cargo_etc";
	}
}
