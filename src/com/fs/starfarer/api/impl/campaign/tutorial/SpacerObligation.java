package com.fs.starfarer.api.impl.campaign.tutorial;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SpacerObligation implements EconomyTickListener, TooltipCreator {

	public static int DEBT_BASE = 1000;
	public static int DEBT_PER_LEVEL = 1000;
	
	protected long startTime = 0;
	public SpacerObligation() {
		Global.getSector().getListenerManager().addListener(this);
		startTime = Global.getSector().getClock().getTimestamp();
	}
	
	public void reportEconomyTick(int iterIndex) {
		int lastIterInMonth = (int) Global.getSettings().getFloat("economyIterPerMonth") - 1;
		if (iterIndex != lastIterInMonth) return;
		
		MonthlyReport report = SharedData.getData().getCurrentReport();


		int debt = getDebt();
		FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		
		FDNode stipendNode = report.getNode(fleetNode, "SpacerObligation");
		stipendNode.upkeep = debt;
		stipendNode.name = "An obligation from your past";
		stipendNode.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
		stipendNode.tooltipCreator = this;
	}
	
	protected int getDebt() {
		return DEBT_BASE + (Global.getSector().getPlayerStats().getLevel() - 1) * DEBT_PER_LEVEL;
	}

	public void reportEconomyMonthEnd() {
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		tooltip.addPara("You never talk about it.", 
				0f, Misc.getHighlightColor(), Misc.getDGSCredits(getDebt()));
	}

	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}
}



