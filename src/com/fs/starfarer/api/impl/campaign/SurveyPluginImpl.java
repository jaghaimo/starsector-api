package com.fs.starfarer.api.impl.campaign;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.util.Misc;

public class SurveyPluginImpl implements SurveyPlugin {
	
	public static int FLAT_SUPPLIES = 10;
	
	public static int BASE_MACHINERY = 10;
	public static int BASE_CREW = 50;
	public static int BASE_SUPPLIES = 10;
	public static int MIN_SUPPLIES_OR_MACHINERY = 5;
	
	public static float MIN_PLANET_RADIUS = 70;
	public static float MAX_PLANET_RADIUS = 250;
	public static float MULT_AT_MAX_PLANET_RADIUS = 5;
	
	
	private CampaignFleetAPI fleet;
	private PlanetAPI planet;
	
	private MutableStat costMult = new MutableStat(1f);
	private MutableStat xpMult = new MutableStat(1f);

	public void init(CampaignFleetAPI fleet, PlanetAPI planet) {
		this.fleet = fleet;
		this.planet = planet;
		
		float hazard = getHazardMultiplier();
		if (hazard != 1f) {
			costMult.modifyMult("planet_hazard", hazard, "Hazard rating");
		}
		float size = getSizeMultiplier();
		if (size != 1f) {
			costMult.modifyMult("planet_size", size, "Planet size");
		}
		
		xpMult.applyMods(costMult);
		
		if (fleet != null) {
			MutableFleetStatsAPI stats = fleet.getStats();
			MutableStat stat = stats.getDynamic().getStat(Stats.SURVEY_COST_MULT);
			for (StatMod mod : stat.getMultMods().values()) {
				costMult.modifyMult(mod.source, mod.value, mod.desc);
			}
		}
		
	}
	
	protected float getHazardMultiplier() {
		float hazard = planet.getMarket().getHazardValue();
		return hazard;
	}
	
	protected float getSizeMultiplier() {
		float radius = planet.getRadius();
		float range = MAX_PLANET_RADIUS - MIN_PLANET_RADIUS;
		if (range <= 0) return 1f;
		if (radius < MIN_PLANET_RADIUS) radius = MIN_PLANET_RADIUS;
		if (radius > MAX_PLANET_RADIUS) radius = MAX_PLANET_RADIUS;
		
		float mult = 1f + ((radius - MIN_PLANET_RADIUS) / range) * (MULT_AT_MAX_PLANET_RADIUS - 1f);
		
		mult = (int)(mult * 20) / 20f;
		
		return mult;
	}
	

	public Map<String, Integer> getRequired() {
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		
		float mult = getCostMult().getModifiedValue();
		
		int machinery = (int)Math.round(BASE_MACHINERY * mult);
		machinery -= (int) Misc.getFleetwideTotalMod(fleet, Stats.getSurveyCostReductionId(Commodities.HEAVY_MACHINERY), 0);
		machinery = Math.round(machinery / 10f) * 10;
		if (machinery < MIN_SUPPLIES_OR_MACHINERY) machinery = MIN_SUPPLIES_OR_MACHINERY;
		
		result.put(Commodities.CREW, (int)Math.round((int)(BASE_CREW * mult) / 10f) * 10);
		result.put(Commodities.HEAVY_MACHINERY, machinery);
		
		return result;
	}
	
	public Map<String, Integer> getConsumed() {
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		
		float mult = getCostMult().getModifiedValue();
		int supplies = (int)Math.round(BASE_SUPPLIES * mult);
		supplies += FLAT_SUPPLIES;
		supplies = Math.round((int) supplies / 10f) * 10;
		supplies -= (int) Misc.getFleetwideTotalMod(fleet, Stats.getSurveyCostReductionId(Commodities.SUPPLIES), 0);
		if (supplies < MIN_SUPPLIES_OR_MACHINERY) supplies = MIN_SUPPLIES_OR_MACHINERY;
		result.put(Commodities.SUPPLIES, supplies);
		
		return result;
	}
	
	public MutableStat getCostMult() {
		return costMult;
	}

	public long getXP() {
		float xp = 0;
		
		if (planet.getMarket() != null) {
			for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
				xp += getBaseXPForCondition(mc.getId());
			}
		}
		xp *= getXPMult().getModifiedValue();
		
		return (long) xp;
	}
	
	public long getBaseXPForCondition(String conditionId) {
		long xp = 0;
		
		float base = Global.getSettings().getFloat("baseSurveyXP");
		ConditionGenDataSpec data = (ConditionGenDataSpec) Global.getSettings().getSpec(ConditionGenDataSpec.class, conditionId, true);
		if (data != null) {
			xp += base * data.getXpMult();
		}
		return xp;
	}
	
	public MutableStat getXPMult() {
		return xpMult;
	}
	
	
	public String getImageCategory() {
		return "illustrations";
	}

	public String getImageKey() {
		return "survey";
	}

	public String getSurveyDataType(PlanetAPI planet) {
		if (planet.getMarket() == null) return null;
		
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
		
		if (value <= 5) return Commodities.SURVEY_DATA_1;
		if (value <= 8) return Commodities.SURVEY_DATA_2;
		if (value <= 11 && count <= 1) return Commodities.SURVEY_DATA_3;
		if (value <= 14 && count <= 2) return Commodities.SURVEY_DATA_4;
		return Commodities.SURVEY_DATA_5;
		
//		if (value <= 10 && count <= 0) return Commodities.SURVEY_DATA_1;
//		if (value <= 20 && count <= 0) return Commodities.SURVEY_DATA_2;
//		if (value <= 30 && count <= 1) return Commodities.SURVEY_DATA_3;
//		if (value <= 40 && count <= 2) return Commodities.SURVEY_DATA_4;
//		return Commodities.SURVEY_DATA_5;

// 		too few class V with below approach
//		if (count <= 0) return Commodities.SURVEY_DATA_1;
//		if (count <= 1) return Commodities.SURVEY_DATA_2;
//		if (count <= 2) return Commodities.SURVEY_DATA_3;
//		if (count <= 3) return Commodities.SURVEY_DATA_4;
//		return Commodities.SURVEY_DATA_5;
	}

	
	public Map<String, Integer> getOutpostConsumed() {
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		
		result.put(Commodities.CREW, 1000);
		result.put(Commodities.HEAVY_MACHINERY, 100);
		result.put(Commodities.SUPPLIES, 200);
		
		return result;
	}
	
}



