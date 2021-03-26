package com.fs.starfarer.api.impl.campaign.graid;

import java.util.Random;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class SolarArrayGroundRaidObjectivePluginImpl extends AbstractGoalGroundRaidObjectivePluginImpl {

	public static int XP_GAIN = 10000;
	
	protected MarketConditionAPI condition = null;
	public SolarArrayGroundRaidObjectivePluginImpl(MarketAPI market) {
		super(market, RaidDangerLevel.EXTREME);
		condition = market.getCondition(Conditions.SOLAR_ARRAY);
	}
	
	public String getName() {
		return "Destroy the " + condition.getName();
	}
	@Override
	public String getIconName() {
		return condition.getSpec().getIcon();
	}

	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;
		
		market.removeCondition(Conditions.SOLAR_ARRAY);
		
		int xpGained = XP_GAIN;
		return xpGained;
	}
	
	@Override
	public boolean hasTooltip() {
		return true;
	}

	@Override
	public void createTooltip(TooltipMakerAPI t, boolean expanded) {
		t.addPara("Functionally destroy the solar array orbiting " + market.getName() + ". While much of the superstructure " +
				"would remain intact, repairing the damage inflicted would be far beyond " +
				"the current state-of-the-art in the Sector.", 0f);
	}

}









