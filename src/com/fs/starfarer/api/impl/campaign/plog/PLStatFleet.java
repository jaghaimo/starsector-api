package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class PLStatFleet extends BasePLStat {

	@Override
	public long getCurrentValue() {
		//return (int) Global.getSector().getPlayerFleet().getFleetPoints();
		float total = 0f;
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			total += member.getDeploymentPointsCost();
		}
		return (int) Math.round(total);
	}

	@Override
	public Color getGraphColor() {
		return Global.getSettings().getColor("progressBarFleetPointsColor");
	}

	@Override
	public String getGraphLabel() {
		return "Fleet";
	}

	@Override
	public String getId() {
		return "fleet";
	}
	
	public long getGraphMax() {
		return FLEET_MAX;
	}
	
	public String getHoverText(long value) {
		//return getGraphLabel();
		return super.getHoverText(value);
	}
}
