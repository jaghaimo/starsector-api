package com.fs.starfarer.api.impl.campaign.intel.deciv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class DecivTracker implements EveryFrameScript {

	public static final String KEY = "$core_decivTracker";
	
	public static final String NO_DECIV_KEY = "$core_noDeciv";
	
	public static class MarketDecivData {
		MarketAPI market;
		List<Float> stabilityHistory = new ArrayList<Float>();
	}
	
	
	public static DecivTracker getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (DecivTracker) test; 
	}
	
	public DecivTracker() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	protected LinkedHashMap<MarketAPI, MarketDecivData> decivData = new LinkedHashMap<MarketAPI, MarketDecivData>();
	protected IntervalUtil sampler = new IntervalUtil(20f, 40f);
	protected IntervalUtil checker = new IntervalUtil(5f, 15f);
	protected TimeoutTracker<String> sentWarning = new TimeoutTracker<String>();
	protected Random random = new Random();
	
	
	protected Object readResolve() {
		if (sentWarning == null) {
			sentWarning = new TimeoutTracker<String>();
		}
		return this;
	}
	
	public void advance(float amount) {
		
		float days = Misc.getDays(amount);
		if (DebugFlags.DECIV_DEBUG) {
			days *= 1000f;
		}
		
		sentWarning.advance(days);
		
		sampler.advance(days);
		if (sampler.intervalElapsed()) {
			updateSamples();
		}
		checker.advance(days);
		if (checker.intervalElapsed()) {
			checkDeciv();
		}
	}
	
	public MarketDecivData getDataFor(MarketAPI market) {
		MarketDecivData data = decivData.get(market);
		if (data == null) {
			data = new MarketDecivData();
			data.market = market;
			decivData.put(market, data);
		}
		return data;
	}
	
	public static int getMaxMonths() {
		return Global.getSettings().getInt("decivSamplingMonths");
	}
	public static int getMinStreak() {
		return Global.getSettings().getInt("decivMinStreak");
	}
	public static float getProbPerMonth() {
		return Global.getSettings().getFloat("decivProbPerMonthOverStreak");
	}
	public static float getMinFraction() {
		return Global.getSettings().getFloat("decivZeroStabilityMinFraction");
	}
	
	
	protected void updateSamples() {
		
		for (MarketAPI market : new ArrayList<MarketAPI>(decivData.keySet())) {
			if (!market.isInEconomy()) {
				decivData.remove(market);
			}
		}
		
		int maxSamples = getMaxMonths();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			MarketDecivData data = getDataFor(market);
			
			data.stabilityHistory.add(market.getStabilityValue());
			while (data.stabilityHistory.size() > maxSamples && !data.stabilityHistory.isEmpty()) {
				data.stabilityHistory.remove(0);
			}
		}
	}
	
	protected void checkDeciv() {
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (checkDeciv(market)) break;
		}
	}
	
	
	protected boolean checkDeciv(MarketAPI market) {
		MarketDecivData data = getDataFor(market);
		
		int max = getMaxMonths();
		int min = getMinStreak();
		float per = getProbPerMonth();
		float fraction = getMinFraction();
		
		if (data.stabilityHistory.size() < max) return false;
		if (data.stabilityHistory.get(0) > 0) return false;
		
		float streak = 0;
		float zeroCount = 0;
		boolean streakEnded = false;
		for (int i = data.stabilityHistory.size() - 1; i >= 0; i--) {
			Float curr = data.stabilityHistory.get(i);
			if (curr <= 0) {
				zeroCount++;
				if (!streakEnded) streak++;
			} else {
				streakEnded = true;
			}
		}
		
		if (streak < min) return false;
		if (zeroCount / max < fraction) return false;
		
		float prob = (streak - min) * per;
		
		
		String id = market.getId();
		if (!sentWarning.contains(id)) {
			sendWarning(market);
			sentWarning.add(id, 180f);
			return false;
		}
//		if (prob == 0f) {
//			sendWarning(market);
//			return false;
//		}
		
		if (random.nextFloat() >= prob) return false;
		
		decivilize(market, false);
		return true;
	}
	
	public static void decivilize(MarketAPI market, boolean fullDestroy) {
		decivilize(market, fullDestroy, true);
	}
	
	public static void decivilize(MarketAPI market, boolean fullDestroy, boolean withIntel) {
		if (market.getMemoryWithoutUpdate().getBoolean(NO_DECIV_KEY)) return;
//		System.out.println("Location: " + market.getLocationInHyperspace());
//		if (true) return;
		
		// issues with decivilizing stand-alone stations at the moment since they become treated as planets
		//if (!(market.getPrimaryEntity() instanceof PlanetAPI)) return;
		
		if (market.getPrimaryEntity().isDiscoverable()) return;
		
		if (withIntel) {
			DecivIntel intel = new DecivIntel(market, market.getPrimaryEntity(), fullDestroy, false);
			Global.getSector().getIntelManager().addIntel(intel);
		}
		
		market.setAdmin(null);
		
		for (SectorEntityToken entity : market.getConnectedEntities()) {
			entity.setFaction(Factions.NEUTRAL);
		}
		
		market.setPlanetConditionMarketOnly(true);
		market.setFactionId(Factions.NEUTRAL);
		
		market.getCommDirectory().clear();
		for (PersonAPI person : market.getPeopleCopy()) {
			market.removePerson(person);
		}
		market.clearCommodities();

		for (MarketConditionAPI mc : new ArrayList<MarketConditionAPI>(market.getConditions())) {
			if (mc.getSpec().isDecivRemove()) {
				market.removeSpecificCondition(mc.getIdForPluginModifications());
			}
		}
		
		for (Industry ind : new ArrayList<Industry>(market.getIndustries())) {
			market.removeIndustry(ind.getId(), null, false);
		}
		
		if (!fullDestroy && !market.hasCondition(Conditions.DECIVILIZED)) {
			market.addCondition(Conditions.DECIVILIZED);
		}
		
		int size = market.getSize();
		market.removeCondition(Conditions.RUINS_SCATTERED);
		market.removeCondition(Conditions.RUINS_WIDESPREAD);
		market.removeCondition(Conditions.RUINS_EXTENSIVE);
		market.removeCondition(Conditions.RUINS_VAST);
		String id = null;
		if (size <= 3) {
			id = market.addCondition(Conditions.RUINS_SCATTERED);
		} else if (size <= 4) {
			id = market.addCondition(Conditions.RUINS_WIDESPREAD);
		} else if (size <= 6) {
			id = market.addCondition(Conditions.RUINS_EXTENSIVE);
		} else {
			id = market.addCondition(Conditions.RUINS_VAST);
		}
		if (id != null) {
			MarketConditionAPI ruins = market.getSpecificCondition(id);
			if (ruins != null) {
				ruins.setSurveyed(true);
			}
		}
		
		market.getMemoryWithoutUpdate().set("$wasCivilized", true);
		
		market.setSize(1);
		market.getPopulation().setWeight(CoreImmigrationPluginImpl.getWeightForMarketSizeStatic(market.getSize()));
		market.getPopulation().normalize();
		
		for (SubmarketAPI sub : market.getSubmarketsCopy()) {
			market.removeSubmarket(sub.getSpecId());
		}
		
		for (SectorEntityToken entity : market.getConnectedEntities()) {
			if (!(entity instanceof PlanetAPI)) {
				Misc.setAbandonedStationMarket(market.getId() + "_deciv", entity);
			}
		}
		
		SectorEntityToken primary = market.getPrimaryEntity();
		market.getConnectedEntities().clear();
		market.setPrimaryEntity(primary);
		market.setPlayerOwned(false);
		
		Global.getSector().getEconomy().removeMarket(market);
		Misc.removeRadioChatter(market);
		market.advance(0f);
		
//		if (!(market.getPrimaryEntity() instanceof PlanetAPI)) {
//			Misc.setAbandonedStationMarket(market.getId() + "_deciv", primary);
//		}

	}
	
	
	public static void removeColony(MarketAPI market, boolean withRuins) {
		market.setAdmin(null);
		
		for (SectorEntityToken entity : market.getConnectedEntities()) {
			entity.setFaction(Factions.NEUTRAL);
		}
		
		market.setPlanetConditionMarketOnly(true);
		market.setFactionId(Factions.NEUTRAL);
		
		market.getCommDirectory().clear();
		for (PersonAPI person : market.getPeopleCopy()) {
			market.removePerson(person);
		}
		market.clearCommodities();

		for (MarketConditionAPI mc : new ArrayList<MarketConditionAPI>(market.getConditions())) {
			if (mc.getSpec().isDecivRemove()) {
				market.removeSpecificCondition(mc.getIdForPluginModifications());
			}
		}
		
		for (Industry ind : new ArrayList<Industry>(market.getIndustries())) {
			market.removeIndustry(ind.getId(), null, false);
		}
		
		if (withRuins) {
			int size = market.getSize();
			market.removeCondition(Conditions.RUINS_SCATTERED);
			market.removeCondition(Conditions.RUINS_WIDESPREAD);
			market.removeCondition(Conditions.RUINS_EXTENSIVE);
			market.removeCondition(Conditions.RUINS_VAST);
			if (size <= 3) {
				market.addCondition(Conditions.RUINS_SCATTERED);
			} else if (size <= 4) {
				market.addCondition(Conditions.RUINS_WIDESPREAD);
			} else if (size <= 6) {
				market.addCondition(Conditions.RUINS_EXTENSIVE);
			} else {
				market.addCondition(Conditions.RUINS_VAST);
			}
		}
		
		market.getMemoryWithoutUpdate().set("$wasCivilized", true);
		
		market.setSize(1);
		market.getPopulation().setWeight(CoreImmigrationPluginImpl.getWeightForMarketSizeStatic(market.getSize()));
		market.getPopulation().normalize();
		
		for (SubmarketAPI sub : market.getSubmarketsCopy()) {
			market.removeSubmarket(sub.getSpecId());
		}
		
		for (SectorEntityToken entity : market.getConnectedEntities()) {
			if (!(entity instanceof PlanetAPI)) {
				Misc.setAbandonedStationMarket(market.getId() + "_deciv", entity);
			}
		}
		
		SectorEntityToken primary = market.getPrimaryEntity();
		market.getConnectedEntities().clear();
		market.setPrimaryEntity(primary);
		market.setPlayerOwned(false);
		
		Global.getSector().getEconomy().removeMarket(market);
		Misc.removeRadioChatter(market);
		market.advance(0f);
	}
	
	public static void sendWarning(MarketAPI market) {
		DecivIntel intel = new DecivIntel(market, market.getPrimaryEntity(), false, true);
		Global.getSector().getIntelManager().addIntel(intel);
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
}















