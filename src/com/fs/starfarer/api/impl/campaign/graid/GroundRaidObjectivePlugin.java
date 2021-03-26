package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.IconGroupAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface GroundRaidObjectivePlugin {
	
	/**
	 * Credit value * this = XP gained from raids for valuables.
	 */
	public static float XP_GAIN_VALUE_MULT = 0.2f;
	
	// higher tiers show closer to the top of the list
	public static float QUANTITY_SORT_TIER_0 = 0;
	public static float QUANTITY_SORT_TIER_1 = 1000000;
	public static float QUANTITY_SORT_TIER_2 = 10000000;
	public static float QUANTITY_SORT_TIER_3 = 100000000;
	public static float QUANTITY_SORT_TIER_4 = 110000000;
	public static float QUANTITY_SORT_TIER_5 = 120000000;
	
	
//	CommoditySpecAPI getCommoditySpec();
//	SpecialItemSpecAPI getItemSpec();
	
	boolean withContinueBeforeResult();
	
	void addIcons(IconGroupAPI iconGroup);
	
	int getMarinesAssigned();
	void setMarinesAssigned(int marines);
	int getMarinesRequired();
	void setMarinesRequired(int marines);
	
	float getQuantitySortValue();
	String getQuantityString(int marines);
	Color getQuantityColor(int marines);
	
	float getValueSortValue();
	String getValueString(int marines);
	Color getValueColor(int marines);
	
	int getProjectedCreditsValue();
	int getCargoSpaceNeeded();
	int getFuelSpaceNeeded();
	
	int getDeficitCaused();
	RaidDangerLevel getDangerLevel();
	
	String getSourceString();

	String getId();
	void setId(String id);
	
	MarketAPI getMarket();
	void setMarket(MarketAPI market);

	String getName();
	String getIconName();
	CargoStackAPI getStackForIcon();
	String getNameOverride();
	void setNameOverride(String nameOverride);

	Industry getSource();
	void setSource(Industry source);
	String getCommodityIdForDeficitIcons();

	String getAssignedForcesColumnText();
	void setAssignedForcesColumnText(String assignedForcesColumnText);
	
	Color getAssignedForcesColumnColor();
	void setAssignedForcesColumnColor(Color assignedForcesColumnColor);
	
	boolean hasTooltip();
	void createTooltip(TooltipMakerAPI tooltip, boolean expanded);
	float getTooltipWidth();
	boolean isTooltipExpandable();

	/**
	 * @param loot
	 * @param random
	 * @param lootMult
	 * @return XP gained
	 */
	int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text);

	int getDisruptionDaysSort(int marines);
	String getDisruptionDaysString(int marines);
	Color getDisruptionDaysColor(int marines);
}



