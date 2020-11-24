package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class LuddicPathBaseManager extends BaseEventManager {

	public static float AI_CORE_ADMIN_INTEREST = 10f;

	public static final String KEY = "$core_luddicPathBaseManager";
	
	public static final float INERTIA_DAYS_MAX = 30f;
	public static final float INERTIA_DAYS_MIN = 10f;
	
	public static final float CHECK_DAYS = 10f;
	public static final float CHECK_PROB = 0.5f;
	
	
	public static LuddicPathBaseManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (LuddicPathBaseManager) test; 
	}
	
	protected long start = 0;
	
	public LuddicPathBaseManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		start = Global.getSector().getClock().getTimestamp();
	}
	
	@Override
	protected int getMinConcurrent() {
		return Global.getSettings().getInt("minLPBases");
	}
	@Override
	protected int getMaxConcurrent() {
		return Global.getSettings().getInt("maxLPBases");
	}
	
	@Override
	protected float getBaseInterval() {
		return CHECK_DAYS;
	}
	
	@Override
	protected Object readResolve() {
		super.readResolve();
		if (cellChecker == null) {
			cellChecker = new IntervalUtil(1f, 3f);
		}
		if (cells == null) {
			cells = new LinkedHashMap<MarketAPI, LuddicPathCellsIntel>();
		}
		return this;
	}
	
	protected IntervalUtil cellChecker = new IntervalUtil(1f, 3f);
	protected int timesSinceLastChange = 10000;
	protected int activeMod = 0;
	protected int sleeperMod = 0;
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		cellChecker.advance(days);
		if (cellChecker.intervalElapsed()) {
			timesSinceLastChange++;
			if (timesSinceLastChange > 50) {
				activeMod = Misc.random.nextInt(3);
				sleeperMod = Misc.random.nextInt(3);
			}
			
			updateCellStatus();
		}
	}

	protected LinkedHashMap<MarketAPI, LuddicPathCellsIntel> cells = new LinkedHashMap<MarketAPI, LuddicPathCellsIntel>();
	
	protected void updateCellStatus() {
		
		float fraction = Global.getSettings().getFloat("basePatherCellFraction");
		float minInterest = Global.getSettings().getFloat("minInterestForPatherCells");
		
		List<Pair<MarketAPI, Float>> marketAndScore = new ArrayList<Pair<MarketAPI,Float>>();
		int total = 0;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getEconGroup() != null) continue;
			if (market.getSize() < 4) continue;
			float score = getLuddicPathMarketInterest(market);
			total++;
			if (score >= minInterest) {
				marketAndScore.add(new Pair<MarketAPI, Float>(market, score));
			}
		}
		
		int numActive = Math.round(total * fraction);
		int numSleeper = numActive;
		
		numActive += activeMod;
		numSleeper += sleeperMod;
		
		LinkedHashSet<MarketAPI> active = new LinkedHashSet<MarketAPI>();
		LinkedHashSet<MarketAPI> sleeper = new LinkedHashSet<MarketAPI>();
		
		
		Collections.sort(marketAndScore, new Comparator<Pair<MarketAPI, Float>>() {
			public int compare(Pair<MarketAPI, Float> o1, Pair<MarketAPI, Float> o2) {
				return (int) Math.signum(o2.two - o1.two);
			}
		});
		
		int count = 0;
		for (Pair<MarketAPI, Float> p : marketAndScore) {
			if (count < numActive) {
				active.add(p.one);
			} else if (count < numActive + numSleeper) {
				sleeper.add(p.one);
			} else {
				break;
			}
			count++;
		}
		
		
		for (MarketAPI market : new ArrayList<MarketAPI>(cells.keySet())) {
			LuddicPathCellsIntel intel = cells.get(market);
			
			if (!active.contains(market) && !sleeper.contains(market)) {
				
				float score = getLuddicPathMarketInterest(market);
				
				if (intel.getInertiaTime() >= INERTIA_DAYS_MAX ||
						(intel.getInertiaTime() >= INERTIA_DAYS_MIN && score < minInterest)) {
					if (!intel.isEnding()) {
						intel.endAfterDelay();
						if (market.isPlayerOwned() || DebugFlags.PATHER_BASE_DEBUG) {
							intel.sendUpdateIfPlayerHasIntel(LuddicPathCellsIntel.UPDATE_DISSOLVED, false);
						}
					}
					cells.remove(market);
				} else { // keep already-established cells for up to INERTIA_DAYS_MAX, at the expense of other potential cells
					if (intel.isSleeper()) {
						List<MarketAPI> sleeperList = new ArrayList<MarketAPI>(sleeper);
						for (int i = sleeperList.size() - 1; i >= 0; i--) {
							MarketAPI other = sleeperList.get(i);
							LuddicPathCellsIntel otherIntel = cells.get(other);
							if (otherIntel != null) continue;
							
							sleeper.remove(other);
							break;
						}
						sleeper.add(market);
					} else {
						List<MarketAPI> activeList = new ArrayList<MarketAPI>(active);
						for (int i = activeList.size() - 1; i >= 0; i--) {
							MarketAPI other = activeList.get(i);
							LuddicPathCellsIntel otherIntel = cells.get(other);
							if (otherIntel != null) continue;
							
							active.remove(other);
							break;
						}
						active.add(market);
					}
				}
			} else {
				intel.setInertiaTime(0f);
			}
		}
		
		for (MarketAPI market : active) {
			LuddicPathCellsIntel intel = cells.get(market);
			if (intel == null) {
				intel = new LuddicPathCellsIntel(market, false);
				cells.put(market, intel);
			}
			LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(intel.getMarket());
			if (base != null) {
				intel.makeActiveIfPossible();
			} else {
				intel.makeSleeper();
			}
		}
		
		for (MarketAPI market : sleeper) {
			LuddicPathCellsIntel intel = cells.get(market);
			if (intel == null) {
				intel = new LuddicPathCellsIntel(market, true);
				cells.put(market, intel);
			}
			intel.makeSleeper();
		}
	}

	public static float getLuddicPathMarketInterest(MarketAPI market) {
		if (market.getFactionId().equals(Factions.LUDDIC_PATH)) return 0f;
		float total = 0f;
		
		String aiCoreId = market.getAdmin().getAICoreId();
		if (aiCoreId != null) {
			total += AI_CORE_ADMIN_INTEREST;
		}
		
		for (Industry ind : market.getIndustries()) {
			total += ind.getPatherInterest();
		}
		
		if (total > 0) {
			total += new Random(market.getName().hashCode()).nextFloat() * 0.1f;
		}
		
		if (market.getFactionId().equals(Factions.LUDDIC_CHURCH)) {
			total *= 0.1f;
		}
		
		return total;
	}
	
	
	
	protected Random random = new Random();
	@Override
	protected EveryFrameScript createEvent() {
		if (random.nextFloat() < CHECK_PROB) return null;
		
		StarSystemAPI system = pickSystemForLPBase();
		if (system == null) return null;
		
		String factionId = Factions.LUDDIC_PATH;
		
		LuddicPathBaseIntel intel = new LuddicPathBaseIntel(system, factionId);
		if (intel.isDone()) intel = null;

		return intel;
	}
	
	
	protected StarSystemAPI pickSystemForLPBase() {
		WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<StarSystemAPI>(random);
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
			if (days < 45f) continue;
			
			if (system.getCenter().getMemoryWithoutUpdate().contains(PirateBaseManager.RECENTLY_USED_FOR_BASE)) continue;
			
			float weight = 0f;
			if (system.hasTag(Tags.THEME_MISC_SKIP)) {
				weight = 1f;
			} else if (system.hasTag(Tags.THEME_MISC)) {
				weight = 3f;
			} else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
				weight = 3f;
			} else if (system.hasTag(Tags.THEME_RUINS)) {
				weight = 5f;
			} else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
				weight = 1f;
			}
			if (weight <= 0f) continue;
			
			float usefulStuff = system.getCustomEntitiesWithTag(Tags.OBJECTIVE).size() +
								system.getCustomEntitiesWithTag(Tags.STABLE_LOCATION).size();
			if (usefulStuff <= 0) continue;
			
			if (Misc.hasPulsar(system)) continue;
			if (Misc.getMarketsInLocation(system).size() > 0) continue;
			
			float dist = system.getLocation().length();
			
			
//			float distMult = 1f - dist / 20000f;
//			if (distMult > 1f) distMult = 1f;
//			if (distMult < 0.1f) distMult = 0.1f;
			
			float distMult = 1f;
			
			if (dist > 36000f) {
				far.add(system, weight * usefulStuff * distMult);
			} else {
				picker.add(system, weight * usefulStuff * distMult);
			}
		}
		
		if (picker.isEmpty()) {
			picker.addAll(far);
		}
		
		return picker.pick();
	}
	
}















