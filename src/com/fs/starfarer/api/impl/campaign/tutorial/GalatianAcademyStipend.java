package com.fs.starfarer.api.impl.campaign.tutorial;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class GalatianAcademyStipend implements EconomyTickListener, TooltipCreator {

	public static float DURATION = 365 + 365 + 365 + 20;
	public static int STIPEND = 15000;
	
	protected long startTime = 0;
	public GalatianAcademyStipend() {
		Global.getSector().getListenerManager().addListener(this);
		startTime = Global.getSector().getClock().getTimestamp();
		Global.getSector().getMemoryWithoutUpdate().set("$playerReceivingGAStipend", true);
	}
	
	public void reportEconomyTick(int iterIndex) {
		if (!Global.getSettings().getBoolean("enableStipend")) return;
		
		int lastIterInMonth = (int) Global.getSettings().getFloat("economyIterPerMonth") - 1;
		if (iterIndex != lastIterInMonth) return;
		
		float daysActive = Global.getSector().getClock().getElapsedDaysSince(startTime);
		MarketAPI ancyra = Global.getSector().getEconomy().getMarket("ancyra_market");
		if (daysActive > DURATION || ancyra == null) {
			Global.getSector().getListenerManager().removeListener(this);
			Global.getSector().getMemoryWithoutUpdate().unset("$playerReceivingGAStipend");
			return;
		}
		
		
		
		MonthlyReport report = SharedData.getData().getCurrentReport();


		int stipend = getStipend();
		FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		
		FDNode stipendNode = report.getNode(fleetNode, "GA_stipend");
		stipendNode.income = stipend;
		stipendNode.name = "Stipend from Galatia Academy";
		stipendNode.icon = Global.getSettings().getSpriteName("income_report", "generic_income");
		stipendNode.tooltipCreator = this;
	}
	
	protected int getStipend() {
		return STIPEND;
	}

	public void reportEconomyMonthEnd() {
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		float daysActive = Global.getSector().getClock().getElapsedDaysSince(startTime);
		tooltip.addPara("A monthly stipend of %s from the Galatian Academy, " +
				"for the aid you've recently rendered.", 
				0f, Misc.getHighlightColor(), Misc.getDGSCredits(getStipend()));
		
		float rem = DURATION - daysActive;
		int months = (int) (rem / 30f);
		//if (months > 0 && months <= 12) {
		if (months > 0) {
			tooltip.addPara("You should continue receiving the stipend for another %s months.", 10f,
					Misc.getHighlightColor(), "" + months);
		} else if (months <= 0) {
			tooltip.addPara("This month's payment was the last.", 10f);
		}
	}

	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}
}



