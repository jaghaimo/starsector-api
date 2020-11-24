package com.fs.starfarer.api.impl.campaign.intel.inspection;

import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HegemonyInspectionManager implements EveryFrameScript {

	public static final String KEY = "$core_hegemonyInspectionManager";
	
	public static final float MAX_THRESHOLD = 1000f;
	
	public static HegemonyInspectionManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (HegemonyInspectionManager) test; 
	}
	
	public HegemonyInspectionManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	protected Object readResolve() {
		if (inspectionChecker == null) {
			inspectionChecker = new IntervalUtil(20f, 40f);
		}
		return this;
	}
	
	protected IntervalUtil inspectionChecker = new IntervalUtil(20f, 40f);
	
	protected float suspicion = 0f;
	protected float threshold = 100f;
	protected float inspectionDelay = 0f;;
	protected int numAttempts = 0;
	
	public void advance(float amount) {
		
		float days = Misc.getDays(amount);
		if (intel != null) {
			if (intel.isEnded()) {
				intel = null;
				inspectionDelay = 100f + 100f * random.nextFloat();
			}
		} else {
			inspectionDelay -= days;
			if (inspectionDelay <= 0) inspectionDelay = 0;
		}
		
		if (DebugFlags.HEGEMONY_INSPECTION_DEBUG) {
			days *= 1000f;
			inspectionDelay = 0f;
			suspicion = 1000f;
		}
		
		inspectionChecker.advance(days);
		if (inspectionChecker.intervalElapsed() && intel == null && inspectionDelay <= 0) {
			checkInspection();
		}
	}

	protected void checkInspection() {
		float total = 0f;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isPlayerOwned()) {
				total += getAICoreUseValue(market);
			}
		}
		
		//suspicion += total;
		suspicion += total * (0.25f + random.nextFloat() * 0.75f);
		
		//suspicion += 100000;
		
		if (suspicion >= threshold) {
			createInspection();
		}
	}
	
	protected Random random = new Random();
	protected HegemonyInspectionIntel intel = null;
	public void createInspection() {
		createInspection(null);
	}
	public void createInspection(Integer fpOverride) {
		
		MarketAPI target = null;
		float max = 0f;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isPlayerOwned()) {
				float curr = getAICoreUseValue(market);
				if (curr > max) {
					target = market;
					max = curr;
				}
			}
		}
		
		if (target != null && max > 0) {
			WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
			for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
				if (market.getFactionId().equals(Factions.HEGEMONY)) {
					if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY)) {
						picker.add(market, market.getSize());
					}
				}
			}	
			MarketAPI from = picker.pick();
			if (from == null) return;
			
			float fp = 50 + threshold * 0.5f;
			//fp = 500;
			if (fpOverride != null) {
				fp = fpOverride;
			}
			intel = new HegemonyInspectionIntel(from, target, fp);
			if (intel.isDone()) {
				intel = null;
				return;
			}
		} else {
			return;
		}
		
		numAttempts++;
		suspicion = 0f;
		threshold *= 2f;
		if (threshold > MAX_THRESHOLD) {
			threshold = MAX_THRESHOLD;
		}
	}
	
	public int getNumAttempts() {
		return numAttempts;
	}

	public static float getAICoreUseValue(MarketAPI market) {
		float total = 0f;
		
		String aiCoreId = market.getAdmin().getAICoreId();
		if (aiCoreId != null) {
			total += 10f;
		}
		
		for (Industry ind : market.getIndustries()) {
			String id = ind.getAICoreId();
			float w = 0f;
			if (Commodities.ALPHA_CORE.equals(id)) {
				w = 4f;
			} else if (Commodities.BETA_CORE.equals(id)) {
				w = 2f;
			} else if (Commodities.GAMMA_CORE.equals(id)) {
				w = 1f;
			}
			total += w;
		}
		
		return total;
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public float getThreshold() {
		return threshold;
	}
	
	
}















