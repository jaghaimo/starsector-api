package com.fs.starfarer.api.impl.campaign.population;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.ImmigrationPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

public class CoreImmigrationPluginImpl implements ImmigrationPlugin {
	
	//public static float INCENTIVE_USE_FRACTION = 0.15f; 
	//public static float INCENTIVE_MIN_PERCENT = 1f;
	public static float INCENTIVE_MIN_PERCENT = 0.2f;
	
	public static final float FACTION_HOSTILITY_IMPACT = 2f; 
	
	protected MarketAPI market;
	
	public static float IMMIGRATION_PER_HAZARD = Global.getSettings().getFloat("immigrationPerHazard");
	
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
		
		boolean firstTime = !market.wasIncomingSetBefore();
		Global.getSettings().profilerBegin("Computing incoming");
		market.setIncoming(computeIncoming());
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
		
		float f = days / 30f; // incoming is per month
		
		int iter = 1;
		if (firstTime) {
			iter = 100;
		}
		
		for (int i = 0; i < iter; i++) {
			
		if (iter > 1) {
			f = (iter - i) * 0.1f;
		}
		
//		if (market.isPlayerOwned()) {
//			System.out.println("ewfwefew");
//		}
		float incentiveCreditsUsed = getIncentivePercentPerMonth() * getCreditsForOnePercentPopulationIncrease();
		incentiveCreditsUsed *= f;
		if (incentiveCreditsUsed <= 0) {
			market.setIncentiveCredits(0f);
		} else {
			market.setIncentiveCredits(Math.max(0, market.getIncentiveCredits() - incentiveCreditsUsed));
		}
		
		PopulationComposition pop = market.getPopulation();
		PopulationComposition inc = market.getIncoming();
		
		
		for (String id : inc.getComp().keySet()) {
			pop.add(id, inc.get(id) * f);
		}
		
		float min = getWeightForMarketSize(market.getSize());
		float max = getWeightForMarketSize(market.getSize() + 1);
		//if (market.getSize() >= 10) max = min;
		
		
		float newWeight = pop.getWeightValue() + inc.getWeightValue() * f;
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
//		market.getPopulation().setWeight(getWeightForMarketSize(market.getSize()));
//		market.getPopulation().normalize();
	}
	
	public void increaseMarketSize() {
		if (market.getSize() >= 10 || !market.isPlayerOwned()) {
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
		if (market.getSize() >= 10) return;
		
		for (int i = 0; i <= 10; i++) {
			market.removeCondition("population_" + i);
		}
		market.removeCondition("population_" + market.getSize());
		market.addCondition("population_" + (market.getSize() + 1));
		
		market.setSize(market.getSize() + 1);
		market.reapplyConditions();
		market.reapplyIndustries();
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
	
	public PopulationComposition computeIncoming() {
		PopulationComposition inc = new PopulationComposition();
		
		float stability = market.getStabilityValue();
		
//		if (stability > 0) {
//			inc.getWeight().modifyFlat("inc_st", stability, "Stability");
//		} else {
//			inc.getWeight().modifyFlat("inc_st", ZERO_STABILITY_PENALTY, "Stability");
//		}
		if (stability < 10) {
			inc.getWeight().modifyFlat("inc_st", stability - 10, "Instability");
		}
		
		int numInd = Misc.getNumIndustries(market);
		if (numInd <= 0) {
			float weight = getWeightForMarketSize(market.getSize());
			float penalty = -Math.round(weight * 0.01f);
			inc.getWeight().modifyFlat("inc_noInd", penalty, "No industries");
		}
		
		
		//inc.getWeight().modifyFlat("inc_size", -market.getSize(), "Colony size");
		
		float a = Math.round(market.getAccessibilityMod().computeEffective(0f) * 100f) / 100f;
		int accessibilityMod = (int) (a / Misc.PER_UNIT_SHIPPING);
		inc.getWeight().modifyFlat("inc_access", accessibilityMod, "Accessibility");
		
		
		float hazMod = Math.round((market.getHazardValue() - 1f) / IMMIGRATION_PER_HAZARD);
		if (hazMod != 0) {
			inc.getWeight().modifyFlat("inc_hazard", -hazMod, "Hazard rating");
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
				inc.getWeight().modifyFlat("inc_insys", sDiff, "Larger non-hostile market in same system");
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
		
		applyIncentives(inc);
		
		
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
	
	
//	public PopulationComposition computeIncomingOld() {
//		PopulationComposition inc = new PopulationComposition();
//		
//		float s1 = market.getStabilityValue();
//		
//		float negative = 0;
//		//Global.getSettings().profilerBegin("Getting in-reach markets");
//		List<MarketAPI> inReach = Global.getSector().getEconomy().getMarketsWithinReach(market);
//		//Global.getSettings().profilerEnd();
//		for (MarketAPI curr : inReach) {
//			if (curr == market) continue;
//			if (curr.isWaystation()) continue;
//			
//			String fid = curr.getFactionId();
//			float s2 = curr.getStabilityValue();
//			
//			float sDiff = s1 - s2;
//						
//			float incomingFromMarket = curr.getSize() * sDiff;
//			if (incomingFromMarket > 0) {
//				float rel = curr.getFaction().getRelationship(market.getFactionId());
//				float mult = 0.5f + (rel + 1f) / 2f;
//				if (mult <= 0.75f) mult = 0.5f;
//				//if (mult >= 1.25f) mult = 1.25f;
//				
//				incomingFromMarket *= mult;
//				
//				float c = 0.33f * Math.max(0, 0.5f - curr.getStabilityValue() / 10f);
//
//				inc.add(fid, incomingFromMarket * (1f - c * 2f));
//				if (c > 0) {
//					inc.add(Factions.PIRATES, incomingFromMarket * c);
//					inc.add(Factions.POOR, incomingFromMarket * c);
//				}
//				//inc.add(fid, incomingFromMarket);
//			} else {
//				negative += Math.abs(incomingFromMarket);
//			}
//		}
//		
//		float maxNegative = (getWeightForMarketSize(market.getSize() + 1) - getWeightForMarketSize(market.getSize())) * 0.05f;
//		if (negative > maxNegative) negative = maxNegative;
//		
//		float maxMarketImmigration = 50f + negative;
//		
//		inc.updateWeight();
//		if (inc.getWeight() > maxMarketImmigration) {
//			inc.setWeight(maxMarketImmigration);
//			inc.normalize();
//		}
//		
//		//Global.getSettings().profilerEnd();
//		
//		float f = getDistFromCoreMult(market);
//		
//		int coreIndependent = (int) Math.round(f * 10);
//		
//		inc.add(Factions.PIRATES, 1);
//		inc.add(Factions.POOR, 1);
//		inc.add(Factions.INDEPENDENT, coreIndependent);
//
//		inc.updateWeight();
//		
//		applyIncentives(inc);
//		
//		
//		float hazMult = (float) Math.pow(0.75f, market.getHazardValue() / 0.25f);
//		market.getIncomingImmigrationMod().modifyMult("core_hazard", hazMult, "Hazard rating");
//		
//		for (MarketImmigrationModifier mod : market.getAllImmigrationModifiers()) {
//			mod.modifyIncoming(market, inc);
//		}
//		
//		inc.normalize();
//		
////		if (market.getName().equals("Jangala")) {
////			System.out.println("wefwefwe");
////		}
//		
//		float in = market.getIncomingImmigrationMod().computeEffective(inc.getWeight());
//		inc.setWeight(in);
//		inc.normalize();
//		
//		negative = market.getOutgoingImmigrationMod().computeEffective(negative);
//		inc.setWeight(in - negative);
//		inc.setLeaving(negative);
//		
//		return inc;
//	}
	
	protected void applyIncentives(PopulationComposition inc) {
//		if (market.getName().equals("Jangala")) {
//			System.out.println("ewfwfew");
//		}
		float percent = getIncentivePercentPerMonth();
		if (percent <= 0) return;
		
		float pts = getPopulationPointsForFraction(0.01f * percent);
		
		//inc.addWeight(percent * pts);
		inc.getWeight().modifyFlat("inc_incentives", pts, "Growth incentives");
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
	
	public float getCreditsForIncreaseFraction(float fraction) {
		float points = getPopulationPointsForFraction(fraction);
		float cost = points * getCreditsPerPopulationPoint();
		return cost;
	}
	
	public int getCreditsForOnePercentPopulationIncrease() {
		return (int) getCreditsForIncreaseFraction(0.01f);
	}
	
	
	public float getIncentivePercentPerMonth() {
		float incentives = market.getIncentiveCredits();
		if (incentives <= 0) return 0f;
		
		float per = getCreditsForOnePercentPopulationIncrease();
		
		float percent = incentives / per;
		
		float usePercent = percent * getIncentiveUseRate();
		
		if (usePercent <= INCENTIVE_MIN_PERCENT) {
			//return Math.min(percent, INCENTIVE_MIN_PERCENT);
			return INCENTIVE_MIN_PERCENT;
		}
		
		return usePercent;
	}
	
	
	private static float base = Global.getSettings().getFloat("immigrationIncentiveBaseCost");
	public float getCreditsPerPopulationPoint() {
		//float f = getIncentiveCostDistMult();
		float f = 1f;
		//f /= market.getIncomingImmigrationMod().getMult();
		return base * f;
	}
	
//	public float getIncentiveCostDistMult() {
//		float f = getDistFromCoreMult(market);
//		f = 0.5f + (f - 0.1f) / 0.9f * 0.5f;
//		f = Math.round(f * 20f) / 20f;
//		f = 1/f;
//		return f;
//	}
//
//	public static float getDistFromCoreMult(MarketAPI market) {
//		float distFromCore = Misc.getDistanceLY(market.getLocationInHyperspace(), new Vector2f());
//		float threshold = 10;
//		float maxDist = 30 + threshold;
//		
//		float min = 0.1f;
//		float f = 1f;
//		if (distFromCore > threshold) {
//			f = 1f - Math.min(1f, (distFromCore - threshold) / (maxDist - threshold));
//		}
//		if (f < min) f = min;
//		return f;
//	}
	
	
	public static float getWeightForMarketSizeStatic(float size) {
		//return (float) (100f * Math.pow(2, size - 3));
		return (float) (200f * Math.pow(2, size - 3));
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
	
	public float getIncentiveUseRate() {
		return getIncentiveUseRate(market.getSize());
	}
	
	public static float [] INCENTIVE_USE_RATE = null;
	public static float getIncentiveUseRate(int size) {
		if (INCENTIVE_USE_RATE == null) {
			try {
				INCENTIVE_USE_RATE = new float [10];
				JSONArray a = Global.getSettings().getJSONArray("immigrationIncentiveUseRatePerMonth");
				for (int i = 0; i < INCENTIVE_USE_RATE.length; i++) {
					INCENTIVE_USE_RATE[i] = (float) a.getDouble(i);
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		size--;
		if (size < 0) size = 0;
		if (size > 9) size = 9;
		return INCENTIVE_USE_RATE[size];
//		if (size <= 3) return 1;
//		if (size <= 5) return 2;
//		if (size <= 7) return 3;
//		return 4;
	}
}






