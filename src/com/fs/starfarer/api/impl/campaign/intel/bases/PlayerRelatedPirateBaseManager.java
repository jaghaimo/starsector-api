package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.impl.campaign.intel.events.PiracyRespiteScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PlayerRelatedPirateBaseManager implements EveryFrameScript {

	public static final String KEY = "$core_PR_pirateBaseManager";
	
	
	//public static int MIN_MONTHS_BEFORE_RAID = Global.getSettings().getInt("minMonthsBeforeFirstPirateRaidOnPlayerColony");
	
	public static int MIN_TIMEOUT = Global.getSettings().getIntFromArray("playerRelatedPirateBaseCreationTimeoutMonths", 0); 
	public static int MAX_TIMEOUT = Global.getSettings().getIntFromArray("playerRelatedPirateBaseCreationTimeoutMonths", 1);
	
	public static int MIN_TIMEOUT_DESTROYED = Global.getSettings().getIntFromArray("playerRelatedPirateBaseCreationTimeoutExtraAfterBaseDestroyed", 0); 
	public static int MAX_TIMEOUT_DESTROYED  = Global.getSettings().getIntFromArray("playerRelatedPirateBaseCreationTimeoutExtraAfterBaseDestroyed", 1); 
	
	
	public static PlayerRelatedPirateBaseManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (PlayerRelatedPirateBaseManager) test; 
	}
	
	
	protected long start = 0;
	//protected boolean sentFirstRaid = false;
	protected IntervalUtil monthlyInterval = new IntervalUtil(20f, 40f);
	protected int monthsPlayerColoniesExist = 0;
	protected int baseCreationTimeout = 0;
	protected Random random = new Random();
	
	protected List<PirateBaseIntel> bases = new ArrayList<PirateBaseIntel>();
	
	public PlayerRelatedPirateBaseManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		start = Global.getSector().getClock().getTimestamp();
	}
	
	
	public void advance(float amount) {
		
		for (PirateBaseIntel intel : bases) {
			intel.advance(amount);
		}
		
		float days = Misc.getDays(amount);
		
		if (DebugFlags.RAID_DEBUG) {
			days *= 100f;
		}
		
		monthlyInterval.advance(days);
		
		if (monthlyInterval.intervalElapsed()) {
			removeDestroyedBases();
			
			FactionAPI player = Global.getSector().getPlayerFaction();
			List<MarketAPI> markets = Misc.getFactionMarkets(player);
			
			Iterator<MarketAPI> iter = markets.iterator();
			while (iter.hasNext()) {
				if (iter.next().isHidden()) iter.remove();
			}
			
			if (markets.isEmpty()) {
				return;
			}
			
			monthsPlayerColoniesExist++;
			
//			if (!sentFirstRaid) {
//				if (monthsPlayerColoniesExist >= MIN_MONTHS_BEFORE_RAID && !markets.isEmpty()) {
//					sendFirstRaid(markets);
//					baseCreationTimeout = MIN_TIMEOUT + random.nextInt(MAX_TIMEOUT - MIN_TIMEOUT + 1);
//				}
//				return;
//			}
			
			if (baseCreationTimeout > 0) {
				baseCreationTimeout--;
			} else {
				if (random.nextFloat() > 0.5f && PiracyRespiteScript.get() == null) {
					addBasesAsNeeded();
				}
			}
		}
	}
	
	protected void removeDestroyedBases() {
		Iterator<PirateBaseIntel> iter = bases.iterator();
		while (iter.hasNext()) {
			PirateBaseIntel intel = iter.next();
			if (intel.isEnded() && !intel.getMarket().isInEconomy()) {
				iter.remove();
				
//				int baseTimeout = 3;
//				switch (intel.getTier()) {
//				case TIER_1_1MODULE: baseTimeout = 3; break;
//				case TIER_2_1MODULE: baseTimeout = 3; break;
//				case TIER_3_2MODULE: baseTimeout = 4; break;
//				case TIER_4_3MODULE: baseTimeout = 5; break;
//				case TIER_5_3MODULE: baseTimeout = 6; break;
//				}
//				baseCreationTimeout += baseTimeout + random.nextInt(baseTimeout + 1);
				baseCreationTimeout += MIN_TIMEOUT_DESTROYED + random.nextInt(MAX_TIMEOUT_DESTROYED - MIN_TIMEOUT_DESTROYED + 1);
			}
		}
	}
	
	protected void addBasesAsNeeded() {
		FactionAPI player = Global.getSector().getPlayerFaction();
		List<MarketAPI> markets = Misc.getFactionMarkets(player);
		
		Set<StarSystemAPI> systems = new LinkedHashSet<StarSystemAPI>();
		for (MarketAPI curr : markets) {
			StarSystemAPI system = curr.getStarSystem();
			if (system != null) {
				systems.add(system);
			}
		}
		if (systems.isEmpty()) return;
		
		float marketTotal = markets.size();
		int numBases = (int) (marketTotal / 2);
		if (numBases < 1) numBases = 1;
		if (numBases > 2) numBases = 2;
		
		
		if (bases.size() >= numBases) {
			return;
		}
		
		
		StarSystemAPI initialTarget = null;
		float bestWeight = 0f;
		OUTER: for (StarSystemAPI curr : systems) {
			float w = 0f;
			for (MarketAPI m : Global.getSector().getEconomy().getMarkets(curr)) {
				if (m.hasCondition(Conditions.PIRATE_ACTIVITY)) continue OUTER;
				if (m.getFaction().isPlayerFaction()) {
					w += m.getSize() * m.getSize();
				}
			}
			if (w > bestWeight) {
				bestWeight = w;
				initialTarget = curr;
			}
		}
		
		if (initialTarget == null) return;
		
		StarSystemAPI target = pickSystemForPirateBase(initialTarget);
		if (target == null) return;
		
		PirateBaseTier tier = pickTier(target);
		
		String factionId = pickPirateFaction();
		if (factionId == null) return;
		
		//factionId = Factions.HEGEMONY;
		
		PirateBaseIntel intel = new PirateBaseIntel(target, factionId, tier);
		if (intel.isDone()) {
			intel = null;
			return;
		}
		
		//intel.setTargetPlayerColoniesOnly(true);
		// this is for raids: don't do it since raids are handled by HostileActivityEventIntel now
		//intel.setForceTarget(initialTarget);
		intel.updateTarget();
		bases.add(intel);
		
		baseCreationTimeout = MIN_TIMEOUT + random.nextInt(MAX_TIMEOUT - MIN_TIMEOUT + 1);
	}

	public String pickPirateFaction() {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		for (FactionAPI faction : Global.getSector().getAllFactions()) {
			if (!faction.isHostileTo(Factions.PLAYER)) continue;
			
			if (faction.getCustomBoolean(Factions.CUSTOM_MAKES_PIRATE_BASES)) {
				picker.add(faction.getId(), 1f);
			}
		}
		return picker.pick();
	}
	
//	protected void sendFirstRaid(List<MarketAPI> markets) {
//		if (markets.isEmpty()) return;
//		
//		
//		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
//		picker.addAll(markets);
//		MarketAPI target = picker.pick();
//		
//		PirateBaseIntel closest = null;
//		float minDist = Float.MAX_VALUE;
//		for (IntelInfoPlugin p : Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class)) {
//			PirateBaseIntel intel = (PirateBaseIntel) p;
//			if (intel.isEnding()) continue;
//			
//			float dist = Misc.getDistanceLY(intel.getMarket().getPrimaryEntity(), target.getPrimaryEntity());
//			if (dist < minDist && dist <= 15) {
//				minDist = dist;
//				closest = intel;
//			}
//		}
//		
//		if (closest != null && target != null) {
//			float raidFP = 120 + 30f * random.nextFloat();
////			raidFP = 1000;
////			raidFP = 500;
//			closest.startRaid(target.getStarSystem(), raidFP);
//			sentFirstRaid = true;
//		}
//	}

	
	
	protected PirateBaseTier pickTier(StarSystemAPI system) {
		float max = 0f;
		for (MarketAPI m : Global.getSector().getEconomy().getMarkets(system)) {
			if (m.getFaction().isPlayerFaction()) {
				max = Math.max(m.getSize(), max);
			}
		}
		if (max >= 7) {
			return PirateBaseTier.TIER_5_3MODULE;
		} else if (max >= 6) {
			return PirateBaseTier.TIER_4_3MODULE;
		} else if (max >= 5) {
			return PirateBaseTier.TIER_3_2MODULE;
		} else if (max >= 4) {
			return PirateBaseTier.TIER_2_1MODULE;
		} else {
			return PirateBaseTier.TIER_1_1MODULE;
		}
		
	}
	
	protected StarSystemAPI pickSystemForPirateBase(StarSystemAPI initialTarget) {
		WeightedRandomPicker<StarSystemAPI> veryFar = new WeightedRandomPicker<StarSystemAPI>(random);
		WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<StarSystemAPI>(random);
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasPulsar()) continue;
			
			float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
			if (days < 180f) continue;
			
			if (system.getCenter().getMemoryWithoutUpdate().contains(PirateBaseManager.RECENTLY_USED_FOR_BASE)) continue;
			
			float weight = 0f;
			if (system.hasTag(Tags.THEME_MISC_SKIP)) {
				weight = 1f;
			} else if (system.hasTag(Tags.THEME_MISC)) {
				weight = 3f;
			} else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
				weight = 3f;
			} else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
				weight = 3f;
			} else if (system.hasTag(Tags.THEME_RUINS)) {
				weight = 5f;
			} else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
				//weight = 1f;
				weight = 0f;
			}
			if (weight <= 0f) continue;
			
			float usefulStuff = system.getCustomEntitiesWithTag(Tags.OBJECTIVE).size() +
								system.getCustomEntitiesWithTag(Tags.STABLE_LOCATION).size();
			if (usefulStuff <= 0) continue;
			
			if (Misc.getMarketsInLocation(system).size() > 0) continue;
			
			float dist = Misc.getDistance(initialTarget.getLocation(), system.getLocation());
			
			float distMult = 100000f / dist;
			distMult *= distMult;
			
			if (dist > 30000f) {
				veryFar.add(system, weight * usefulStuff * distMult);
			} else if (dist > 10000f) {
				far.add(system, weight * usefulStuff * distMult);
			} else {
				picker.add(system, weight * usefulStuff * distMult);
			}
		}
		
		if (picker.isEmpty()) {
			picker.addAll(far);
		}
		if (picker.isEmpty()) {
			picker.addAll(veryFar);
		}
		
		return picker.pick();
	}


	public boolean isDone() {
		return false;
	}


	public boolean runWhilePaused() {
		return false;
	}
	
}















