package com.fs.starfarer.api.impl.campaign.intel.raid;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class OrganizeStage extends BaseRaidStage {
	
	protected MarketAPI market;
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
	
	public OrganizeStage(RaidIntel raid, MarketAPI market, float durDays) {
		super(raid);
		this.market = market;
		this.maxDays = durDays;
	}
	
	
	public void advance(float amount) {
		if (status == RaidStageStatus.ONGOING && 
				(!market.isInEconomy() || !market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY))) {
			abort();
			return;
		}
		super.advance(amount);
	}
	
	protected void updateStatus() {
		if (maxDays <= elapsed) {
			status = RaidStageStatus.SUCCESS;
		}
	}
	
	public void abort() {
		status = RaidStageStatus.FAILURE;
	}
	
	public MarketAPI getMarket() {
		return market;
	}

	public void showStageInfo(TooltipMakerAPI info) {
		int curr = intel.getCurrentStage();
		int index = intel.getStageIndex(this);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		int days = Math.round(maxDays - elapsed);
		String strDays = RaidIntel.getDaysString(days);
		
		String timing = getForcesString() + " should begin assembling in %s " + strDays + ".";
		if (days < 2) {
			timing = getForcesString() + " should begin assembling shortly.";
		}
		
		String raid = getRaidString();
		if (status == RaidStageStatus.FAILURE) {
			info.addPara("The " + raid + " has been disrupted in the planning stages and will not happen.", opad);
		} else if (curr == index) {
			boolean known = !market.isHidden() || !market.getPrimaryEntity().isDiscoverable();
			if (known) {
				info.addPara("The " + raid + " is currently being planned " + 
						market.getOnOrAt() + " " + market.getName() + ". " + timing,
						opad, h, "" + days);
			} else {
				info.addPara("The " + raid + " is currently in the planning stages. " + timing,
						opad, h, "" + days);
			}
		}
	}
	
	protected String getForcesString() {
		return "The raiding forces";
	}
	
	protected String getRaidString() {
		return "raid";
	}
}






