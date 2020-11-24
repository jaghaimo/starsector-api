package com.fs.starfarer.api.plugins;

import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.combat.MutableStat;

public interface SurveyPlugin {
	void init(CampaignFleetAPI fleet, PlanetAPI planet);
	
	Map<String, Integer> getRequired();
	Map<String, Integer> getConsumed();
	
	String getImageCategory();
	String getImageKey();

	MutableStat getCostMult();

	/**
	 * Total XP.
	 * @return
	 */
	long getXP();
	
	
	/**
	 * XP for a specific condition.
	 * @param conditionId
	 * @return
	 */
	long getBaseXPForCondition(String conditionId);
	
	
	/**
	 * Overall XP multipliers, based on hazard level/planet size/etc.
	 * @return
	 */
	MutableStat getXPMult();
	
	String getSurveyDataType(PlanetAPI planet);

	Map<String, Integer> getOutpostConsumed();
	
}
