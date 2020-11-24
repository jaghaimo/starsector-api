package com.fs.starfarer.api.impl.campaign.intel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class FactionHostilityManager extends BaseEventManager {

	public static String getConflictId(FactionAPI a, FactionAPI b) {
		String id1 = a.getId();
		String id2 = b.getId();
		if (id1.compareTo(id2) < 0) {
			return id1 + "_" + id2;
		}
		return id2 + "_" + id1;
	}
	
	public static final String KEY = "$core_factionHostilityManager";
	
	public static final float TIMEOUT_AFTER_ENDING = 30f;
	public static final float CHECK_DAYS = 10f;
	public static final float CHECK_PROB = 0.5f;
	
	public static FactionHostilityManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (FactionHostilityManager) test; 
	}
	
	public FactionHostilityManager() {
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	public boolean areHostilitiesOngoing(FactionAPI a, FactionAPI b) {
		FactionHostilityIntel intel = getHostilties(a, b);
		return intel != null && !intel.isEnding();
	}
	
	public FactionHostilityIntel getHostilties(FactionAPI a, FactionAPI b) {
		String id = getConflictId(a, b);
		for (EveryFrameScript s : getActive()) {
			FactionHostilityIntel intel = (FactionHostilityIntel) s;
			if (intel.getId().equals(id)) {
				return intel;
			}
		}
		return null;
	}
	
	public List<FactionHostilityIntel> getHostilitiesInvolving(FactionAPI faction) {
		List<FactionHostilityIntel> result = new ArrayList<FactionHostilityIntel>();
		for (EveryFrameScript s : getActive()) {
			FactionHostilityIntel intel = (FactionHostilityIntel) s;
			if (intel.getOne() == faction || intel.getTwo() == faction) {
				result.add(intel);
			}
		}
		return result;
	}
	
	
	@Override
	protected int getMinConcurrent() {
		List<FactionAPI> factions = getEligibleFactions(true);
		
		float mult = Global.getSettings().getFloat("minFactionHostilitiesMult");
		return (int) Math.ceil(factions.size() * mult);
	}
	
	@Override
	protected int getMaxConcurrent() {
		List<FactionAPI> factions = getEligibleFactions(true);
		
		float mult = Global.getSettings().getFloat("maxFactionHostilitiesMult");
		return (int) Math.ceil(factions.size() * mult);
	}
	
	protected List<FactionAPI> getEligibleFactions(boolean checkNumMarkets) {
		List<FactionAPI> factions = Global.getSector().getAllFactions();
		List<FactionAPI> result = new ArrayList<FactionAPI>();
		for (int i = 0; i < factions.size(); i++) {
			FactionAPI faction = factions.get(i);
			if (!faction.getCustom().optBoolean(Factions.CUSTOM_ENGAGES_IN_HOSTILITIES)) {
				continue;
			}
			
			if (faction.isPlayerFaction()) continue;
			
			if (checkNumMarkets) {
				int count = Misc.getFactionMarkets(faction).size();
				if (count <= 0) continue;
			}
			
			result.add(faction);
		}
		
		return result;
	}
	
	
	@Override
	protected float getBaseInterval() {
		return CHECK_DAYS;
	}
	
	public void startHostilities(String a, String b) {
		startHostilities(Global.getSector().getFaction(a), Global.getSector().getFaction(b));
		
	}
	public void startHostilities(FactionAPI a, FactionAPI b) {
		if (areHostilitiesOngoing(a, b)) return;
		
		FactionHostilityIntel intel = new FactionHostilityIntel(a, b);
		addActive(intel);
	}

	protected Random random = new Random();
	
	@Override
	protected EveryFrameScript createEvent() {
		if (random.nextFloat() < CHECK_PROB) return null;
		
		Pair<FactionAPI, FactionAPI> pick = pickFactions();
		if (pick == null) return null;
		
		FactionHostilityIntel intel = new FactionHostilityIntel(pick.one, pick.two);
		
		return intel;
	}
	
	public void notifyRecentlyEnded(String id) {
		recentlyEnded.add(id, TIMEOUT_AFTER_ENDING);
	}
	
	public TimeoutTracker<String> getRecentlyEnded() {
		return recentlyEnded;
	}

	protected TimeoutTracker<String> recentlyEnded = new TimeoutTracker<String>();
	@Override
	public void advance(float amount) {
		//amount *= 100f;
		super.advance(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		recentlyEnded.advance(days);
	}

	protected Pair<FactionAPI, FactionAPI> pickFactions() {
		List<FactionAPI> factions = new ArrayList<FactionAPI>();
		Map<FactionAPI, Integer> marketCounts = new HashMap<FactionAPI, Integer>();
		
		for (FactionAPI faction : getEligibleFactions(false)) {
			int count = Misc.getFactionMarkets(faction).size();
			if (count <= 0) continue;
			marketCounts.put(faction, count);
			
			factions.add(faction);
		}
		
		WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<FactionAPI>(random);
		
		boolean weighCommissionedMore = false;
		FactionAPI commission = Misc.getCommissionFaction();
		if (commission != null && getHostilitiesInvolving(commission).isEmpty()) {
			weighCommissionedMore = true;
		}
		
		for (FactionAPI faction : factions) {
			float w = 1f;
			if (commission == faction && weighCommissionedMore) {
				w = factions.size();
			}
			picker.add(faction, w);
		}
		
		FactionAPI one = picker.pick();
		if (one == null) return null;
		
		picker.clear();
		for (FactionAPI faction : factions) {
			if (faction == one) continue;
			if (faction.isHostileTo(one)) continue;
			if (faction.isAtWorst(one, RepLevel.COOPERATIVE)) continue;
			
			String id = getConflictId(one, faction);
			if (recentlyEnded.contains(id)) continue;
			
			float w = marketCounts.get(faction);
			picker.add(faction, w);
		}
		
		FactionAPI two = picker.pick();
		if (two == null) return null;
		
		return new Pair<FactionAPI, FactionAPI>(one, two);
		
	}
	
}















