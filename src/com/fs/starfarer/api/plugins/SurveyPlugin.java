package com.fs.starfarer.api.plugins;

import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;

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
	
	default int getSurveyDataScore(PlanetAPI planet) {
		if (planet.getMarket() == null) return 0;
		int count = 0;
		float value = 0;
		for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
			if (DerelictThemeGenerator.interestingConditionsWithRuins.contains(mc.getId())) {
				count++;
			}
			if (mc.getGenSpec() != null) {
				//value += mc.getGenSpec().getXpMult();
				value += mc.getGenSpec().getRank();
			}
		}
		
		if (planet.getMarket().hasCondition(Conditions.HABITABLE)) {
			value += 4f;
		}
		
		float hazard = planet.getMarket().getHazardValue();
		value -= (hazard - 1f) * 2f;
		
		return (int) value;
	};

	Map<String, Integer> getOutpostConsumed();
	
}
