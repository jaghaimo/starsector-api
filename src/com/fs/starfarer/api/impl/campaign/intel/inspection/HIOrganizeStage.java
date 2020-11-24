package com.fs.starfarer.api.impl.campaign.intel.inspection;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.raid.OrganizeStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;

public class HIOrganizeStage extends OrganizeStage {
	
	public HIOrganizeStage(RaidIntel raid, MarketAPI market, float durDays) {
		super(raid, market, durDays);
	}
	
	protected String getForcesString() {
		return "The inspection task force";
	}
	
	protected String getRaidString() {
		return "inspection";
	}
}






