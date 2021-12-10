package com.fs.starfarer.api.impl.campaign.population;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

public class CoreImmigrationPluginImpl implements ImmigrationPlugin {
	
	public static float GROWTH_NO_INDUSTRIES = 0.01f; 
	public static float IMMIGRATION_PER_HAZARD = Global.getSettings().getFloat("immigrationPerHazard");
	public static float HAZARD_SIZE_MULT = Global.getSettings().getFloat("immigrationHazardMultExtraPerColonySizeAbove3");
	
	public static float INCENTIVE_CREDITS_PER_POINT = Global.getSettings().getFloat("immigrationIncentiveCostPerPoint");
	public static float INCENTIVE_POINTS_EXTRA = Global.getSettings().getFloat("immigrationIncentivePointsAboveHazardPenalty");
	
	public static final float FACTION_HOSTILITY_IMPACT = 2f;
	
	protected MarketAPI market;
	
	
	public CoreImmigrationPluginImpl(MarketAPI market) {
		this.market = market;
	}
	
	public void advance(float days, boolean uiUpdateOnly) {
//		if (market.getName().equals("Umbra")) {
//			System.out.println("ewfwefew");
//		}
		
//		if (market.getName().equals("Jangala")) {
//			System.out.println("wefwefwe");
//		}
		
		float f = days / 30f; // incoming is per month
		
		boolean firstTime = !market.wasIncomingSetBefore();
		Global.getSettings().profilerBegin("Computing incoming");
		market.setIncoming(computeIncoming(uiUpdateOnly, f));
		Global.getSettings().profilerEnd();
		
//		if (market.getName().equals("Jangala")) {
//			System.out.println("wefwefwe");
//		}
		
//		if (market.isPlayerOwned()) {
//			//market.setSize(2);
//			while (market.getSize() < 7) {
//				increaseMarketSize();
//			}
//		}
		
		if (uiUpdateOnly) return;
		
		
		int iter = 1;
		if (firstTime) {
			iter = 100;
		}
		
		for (int i = 0; i < iter; i++) {
			
		if (iter > 1) {
			f = (iter - i) * 0.1f;
		}
		
		PopulationComposition pop = market.getPopulation();
		PopulationComposition inc = market.getIncoming();
		
		for (String id : inc.getComp().keySet()) {
			pop.add(id, inc.get(id) * f);
		}
		
//		if (market.getName().equals("Ang")) {
//			System.out.println("efwefwefew");
//		}
		float min = getWeightForMarketSize(market.getSize());
		float max = getWeightForMarketSize(market.getSize() + 1);
		//if (market.getSize() >= 10) max = min;
		
		
		float newWeight = pop.getWeightValue() + inc.getWeightValue() * f;
		//newWeight = pop.getWeightValue() + inc.getWeightValue() * f + 2000;
		if (newWeight < min || Global.getSector().isInNewGameAdvance()) newWeight = min;
		if (newWeight > max) {
			increaseMarketSize();
			newWeight = max;
		}
		pop.setWeight(newWeight);
		pop.normalize();
		
		// up to 5% of the non-faction population gets converted to faction, per month, more or less
		float conversionFraction = 0.05f * market.getStabilityValue() / 10f;
		conversionFraction *= f;
		if (conversionFraction > 0) {
			pop.add(market.getFactionId(), (pop.getWeightValue() - pop.get(market.getFactionId())) * conversionFraction);
		}
		
		
		// add some poor/pirate population at stability below 5 
		float pirateFraction = 0.01f * Math.max(0, (5f - market.getStabilityValue()) / 5f);
		pirateFraction *= f;
		if (pirateFraction > 0) {
			pop.add(Factions.PIRATES, pop.getWeightValue() * pirateFraction);
			pop.add(Factions.POOR, pop.getWeightValue() * pirateFraction);
		}

		
		for (String fid : new ArrayList<String>(pop.getComp().keySet())) {
			if (Global.getSector().getFaction(fid) == null) {
				pop.getComp().remove(fid);
			}
		}
		
		pop.normalize();
		
		}
	}
	
	public void increaseMarketSize() {
		if (market.getSize() >= Misc.MAX_COLONY_SIZE || !market.isPlayerOwned()) {
			market.getPopulation().setWeight(getWeightForMarketSizeStatic(market.getSize()));
			market.getPopulation().normalize();
			return;
		}
		
		increaseMarketSize(market);
		
		if (market.isPlayerOwned()) {
			MessageIntel intel = new MessageIntel("Colony Growth - " + market.getName(), Misc.getBasePlayerColor());
			intel.addLine(BaseIntelPlugin.BULLET + "Size increased to %s",
					Misc.getTextColor(), 
					new String[] {"" + (int)Math.round(market.getSize())},
					Misc.getHighlightColor());
			
			intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
			intel.setSound(BaseIntelPlugin.getSoundMajorPosting());
			Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.COLONY_INFO, market);
		}
	}
	
	public static void increaseMarketSize(MarketAPI market) {
		if (market.getSize() >= Misc.MAX_COLONY_SIZE) return;
		
		for (int i = 0; i <= 10; i++) {
			market.removeCondition("population_" + i);
		}
		market.removeCondition("population_" + market.getSize());
		market.addCondition("population_" + (market.getSize() + 1));
		
		market.setSize(market.getSize() + 1);
		market.reapplyConditions();
		market.reapplyIndustries();
		
		if (market.getSize() >= Misc.MAX_COLONY_SIZE) {
			market.setImmigrationIncentivesOn(false);
		}
	}
	
	public static void reduceMarketSize(MarketAPI market) {
		if (market.getSize() <= 3) {
			return;
		}
		
		market.removeCondition("population_" + market.getSize());
		market.addCondition("population_" + (market.getSize() - 1));
		
		market.setSize(market.getSize() - 1);
		
		market.getPopulation().setWeight(getWeightForMarketSizeStatic(market.getSize()));
		market.getPopulation().normalize();
		
		market.reapplyConditions();
		market.reapplyIndustries();
	}
	
	
	public static final float ZERO_STABILITY_PENALTY = -5;
	public static final float MAX_DIST_PENALTY = -5;
	
	public PopulationComposition computeIncoming(boolean uiUpdateOnly, float f) {
		PopulationComposition inc = new PopulationComposition();
		
		float stability = market.getStabilityValue();
		
//		if (stability > 0) {
//			inc.getWeight().modifyFlat("inc_st", stability, "Stability");
//		} else {
//			inc.getWeight().modifyFlat("inc_st", ZERO_STABILITY_PENALTY, "Stability");
//		}
		if (stability < 5) {
			inc.getWeight().modifyFlat("inc_st", stability - 5, "Instability");
		}
		
		int numInd = Misc.getNumIndustries(market);
		if (numInd <= 0 && GROWTH_NO_INDUSTRIES != 0 && market.getSize() > 3) {
			float weight = getWeightForMarketSize(market.getSize());
			float penalty = -Math.round(weight * GROWTH_NO_INDUSTRIES);
			inc.getWeight().modifyFlat("inc_noInd", penalty, "No industries");
		}
		
		
		//inc.getWeight().modifyFlat("inc_size", -market.getSize(), "Colony size");
		
		float a = Math.round(market.getAccessibilityMod().computeEffective(0f) * 100f) / 100f;
		int accessibilityMod = (int) (a / Misc.PER_UNIT_SHIPPING);
		inc.getWeight().modifyFlat("inc_access", accessibilityMod, "Accessibility");
		
		
		float hazMod = getImmigrationHazardPenalty(market);
		if (hazMod != 0) {
			float hazardSizeMult = getImmigrationHazardPenaltySizeMult(market);
			inc.getWeight().modifyFlat("inc_hazard", hazMod, 
					"Hazard rating (" + Strings.X + Misc.getRoundedValueMaxOneAfterDecimal(hazardSizeMult) + 
					" based on colony size)");
		}
		
//		float dMult = getDistFromCoreMult(market);
//		float dPenalty = Math.round(dMult * MAX_DIST_PENALTY);
//		if (dPenalty > 0) {
//			inc.getWeight().modifyFlat("inc_dist", -dPenalty, "Distance from core worlds");
//		}
		
		MarketAPI biggestInSystem = null;
		List<MarketAPI> inReach = Global.getSector().getEconomy().getMarketsWithSameGroup(market);
		//Global.getSettings().profilerEnd();
		for (MarketAPI curr : inReach) {
			if (curr == market) continue;

			if (curr.getFaction().isHostileTo(market.getFaction())) continue;
			
			if (Misc.getDistanceLY(curr.getLocationInHyperspace(), market.getLocationInHyperspace()) <= 0) {
				if (biggestInSystem == null || curr.getSize() > biggestInSystem.getSize()) {
					biggestInSystem = curr;
				}
			}
		}
		
//		float hostileFactions = 0;
//		for (FactionAPI faction : Global.getSector().getAllFactions()) {
//			if (faction.getCustomBoolean(Factions.CUSTOM_HOSTILITY_IMPACT_ON_GROWTH)) {
//				if (faction.isHostileTo(market.getFaction())) {
//					hostileFactions++;
//				}
//			}
//		}
//		
//		if (hostileFactions > 0) {
//			inc.getWeight().modifyFlat("inc_hosfac", -hostileFactions * FACTION_HOSTILITY_IMPACT,
//					"Open hostilities with major factions");
//		}
		
		if (biggestInSystem != null) {
			float sDiff = biggestInSystem.getSize() - market.getSize();
			sDiff *= 2;
			if (sDiff > 0) {
				inc.getWeight().modifyFlat("inc_insys", sDiff, "Larger non-hostile colony in same system");
			} else if (sDiff < 0) {
				//inc.getWeight().modifyFlat("inc_insys", sDiff, "Smaller non-hostile market in same system");
			}
		}
//		else if (biggestInReach != null) {
//			float sDiff = biggestInReach.getSize() - market.getSize();
//			if (sDiff > 0) {
//				inc.getWeight().modifyFlat("inc_inreach", sDiff, "Larger non-hostile market within reach");
//			} else if (sDiff < 0) {
//				//inc.getWeight().modifyFlat("inc_inreach", sDiff, "Smaller non-hostile market within reach");
//			}
//		}
		
		// so that the "baseline" incoming composition is based on the number of industries
		// thus each industry can use a per-industry modifier without having outsize influence
		// for example Farming can bring in X more Luddic Church immigration, and the impact
		// this has will be diminished if there are more industries beyond Farming
		float numIndustries = market.getIndustries().size();
		inc.add(Factions.PIRATES, 1f * numIndustries);
		inc.add(Factions.POOR, 1f * numIndustries);
		
		String bulkFaction = Factions.INDEPENDENT;
		if (market.getFaction().isHostileTo(bulkFaction)) {
			bulkFaction = market.getFactionId();
		}
		inc.add(bulkFaction, 10f * numIndustries);
		
		applyIncentives(inc, uiUpdateOnly, f);
		
		
		for (MarketImmigrationModifier mod : market.getAllImmigrationModifiers()) {
			mod.modifyIncoming(market, inc);
		}
		
//		if (market.getName().equals("Mazalot")) {
//			System.out.println("wefwefwe");
//		}
		
		for (String fid : new ArrayList<String>(inc.getComp().keySet())) {
			if (Global.getSector().getFaction(fid) == null) {
				inc.getComp().remove(fid);
			}
		}
		
		inc.normalizeToPositive();
		
		return inc;
	}
	
	
	public static float getImmigrationHazardPenalty(MarketAPI market) {
		float hazMod = Math.round((market.getHazardValue() - 1f) / IMMIGRATION_PER_HAZARD);
		if (hazMod < 0) hazMod = 0;
		float hazardSizeMult = getImmigrationHazardPenaltySizeMult(market);
		return -hazMod * hazardSizeMult; 
	}
	
	public static float getImmigrationHazardPenaltySizeMult(MarketAPI market) {
		float hazardSizeMult = 1f + (market.getSize() - 3f) * HAZARD_SIZE_MULT;
		return hazardSizeMult; 
	}
	
	
	
	
	protected void applyIncentives(PopulationComposition inc, boolean uiUpdateOnly, float f) {
//		if (market.getName().equals("Jangala")) {
//			System.out.println("ewfwfew");
//		}
		if (!market.isImmigrationIncentivesOn()) return;
		if (market.getSize() >= Misc.MAX_COLONY_SIZE) {
			market.setImmigrationIncentivesOn(false);
			return;
		}
		
		
		float points = -getImmigrationHazardPenalty(market) + INCENTIVE_POINTS_EXTRA;
		//float cost = INCENTIVE_CREDITS_PER_POINT * points * f;
		float cost = market.getImmigrationIncentivesCost() * f;
		
		if (points > 0) {
			inc.getWeight().modifyFlat("inc_incentives", points, "Hazard pay");
			if (!uiUpdateOnly) {
				market.setIncentiveCredits(market.getIncentiveCredits() + cost);
			}
		}

	}
	
	public float getPopulationPointsForFraction(float fraction) {
		float min = getWeightForMarketSize(market.getSize());
		float max = getWeightForMarketSize(market.getSize() + 1);
		
		return (max - min) * fraction;
	}
	
	public float getFractionForPopulationPoints(float points) {
		float min = getWeightForMarketSize(market.getSize());
		float max = getWeightForMarketSize(market.getSize() + 1);
		
		return points / (max - min);
	}
	
	public static float getWeightForMarketSizeStatic(float size) {
		//return (float) (100f * Math.pow(2, size - 3));
		return (float) (300f * Math.pow(2, size - 3));
	}
	public float getWeightForMarketSize(float size) {
		return getWeightForMarketSizeStatic(size);
//		if (size <= 1) return 100;
//		if (size == 2) return 200;
//		if (size == 3) return 400;
//		if (size == 4) return 800;
//		if (size == 5) return 1600;
//		if (size == 6) return 3200;
//		if (size == 7) return 6400;
//		if (size == 8) return 12800;
//		if (size == 9) return 25600;
//		if (size == 10) return 51200;
//		if (size == 11) return 102400;
//		return 100000;
	}
	
}






