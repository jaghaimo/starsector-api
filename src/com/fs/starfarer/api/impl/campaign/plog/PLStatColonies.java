package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

public class PLStatColonies extends BasePLStat {

	@Override
	public long getCurrentValue() {
		int total = 0;
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			if (curr.isPlayerOwned()) {
				total += curr.getSize();
			}
		}
		return total;
	}

	@Override
	public Color getGraphColor() {
		return Misc.getBasePlayerColor();
	}

	@Override
	public String getGraphLabel() {
		return "Colony size";
	}

	@Override
	public String getId() {
		return "colonies";
	}
	
	public long getGraphMax() {
		return COLONY_MAX;
	}
}
