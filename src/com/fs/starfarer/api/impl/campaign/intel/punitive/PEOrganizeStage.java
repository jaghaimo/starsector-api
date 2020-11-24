package com.fs.starfarer.api.impl.campaign.intel.punitive;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.raid.OrganizeStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;

public class PEOrganizeStage extends OrganizeStage {
	
	public PEOrganizeStage(RaidIntel raid, MarketAPI market, float durDays) {
		super(raid, market, durDays);
	}
	
	protected String getForcesString() {
		return "The expeditionary force";
	}
	
	protected String getRaidString() {
		return "expedition";
	}
}






