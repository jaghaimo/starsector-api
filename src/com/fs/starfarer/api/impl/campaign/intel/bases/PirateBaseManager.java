package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.Tuning;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PirateBaseManager extends BaseEventManager {

	public static final String KEY = "$core_pirateBaseManager";
	
	public static final float CHECK_DAYS = 10f;
	public static final float CHECK_PROB = 0.5f;
	
	
	public static PirateBaseManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (PirateBaseManager) test; 
	}
	
	protected long start = 0;
	protected float extraDays = 0;
	
	protected int numDestroyed = 0;
	protected int numSpawnChecksToSkip = 0;
	
	public PirateBaseManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		start = Global.getSector().getClock().getTimestamp();
	}
	
	@Override
	protected int getMinConcurrent() {
		return Global.getSettings().getInt("minPirateBases");
	}
	@Override
	protected int getMaxConcurrent() {
		return Global.getSettings().getInt("maxPirateBases");
	}
	
	@Override
	protected float getBaseInterval() {
		return CHECK_DAYS;
	}
	
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
	}





	protected Random random = new Random();
	@Override
	protected EveryFrameScript createEvent() {
		if (numSpawnChecksToSkip > 0) {
			numSpawnChecksToSkip--;
			return null;
		}
		
		if (random.nextFloat() < CHECK_PROB) return null;
		
		StarSystemAPI system = pickSystemForPirateBase();
		if (system == null) return null;
		
		//PirateBaseIntel intel = new PirateBaseIntel(system, Factions.PIRATES, PirateBaseTier.TIER_5_3MODULE);
		//PirateBaseIntel intel = new PirateBaseIntel(system, Factions.PIRATES, PirateBaseTier.TIER_3_2MODULE);
		PirateBaseTier tier = pickTier();
		
		//tier = PirateBaseTier.TIER_5_3MODULE;
		
		String factionId = pickPirateFaction();
		if (factionId == null) return null;
		
		PirateBaseIntel intel = new PirateBaseIntel(system, factionId, tier);
		if (intel.isDone()) intel = null;

		return intel;
	}
	
	public String pickPirateFaction() {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		for (FactionAPI faction : Global.getSector().getAllFactions()) {
			if (faction.getCustomBoolean(Factions.CUSTOM_MAKES_PIRATE_BASES)) {
				picker.add(faction.getId(), 1f);
			}
		}
		return picker.pick();
	}
	
	public float getUnadjustedDaysSinceStart() {
		float days = Global.getSector().getClock().getElapsedDaysSince(start);
		return days;
	}
	
	public float getDaysSinceStart() {
		float days = Global.getSector().getClock().getElapsedDaysSince(start) + extraDays;
		if (Misc.isFastStartExplorer()) {
			days += Tuning.FAST_START_EXTRA_DAYS - 30f;
		} else if (Misc.isFastStart()) {
			days += Tuning.FAST_START_EXTRA_DAYS + 60f;
		}
		return days;
	}
	
	/**
	 * 0 at six months (depending on start option chosen), goes up to 1 two years later.
	 * @return
	 */
	public float getStandardTimeFactor() {
		float timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - Tuning.FAST_START_EXTRA_DAYS) / (Tuning.DAYS_UNTIL_FULL_TIME_FACTOR);
		if (timeFactor < 0) timeFactor = 0;
		if (timeFactor > 1) timeFactor = 1;
		return timeFactor;
	}
	
	public float getExtraDays() {
		return extraDays;
	}

	public void setExtraDays(float extraDays) {
		this.extraDays = extraDays;
	}

	protected PirateBaseTier pickTier() {
		float days = getDaysSinceStart();
		
		days += numDestroyed * 200;
		
		WeightedRandomPicker<PirateBaseTier> picker = new WeightedRandomPicker<PirateBaseTier>();
		
		if (days < 360) {
			picker.add(PirateBaseTier.TIER_1_1MODULE, 10f);
			picker.add(PirateBaseTier.TIER_2_1MODULE, 10f);
		} else if (days < 720f) {
			picker.add(PirateBaseTier.TIER_2_1MODULE, 10f);
			picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
		} else if (days < 1080f) {
			picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
			picker.add(PirateBaseTier.TIER_4_3MODULE, 10f);
		} else {
			picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
			picker.add(PirateBaseTier.TIER_4_3MODULE, 10f);
			picker.add(PirateBaseTier.TIER_5_3MODULE, 10f);
		}
		
		
//		if (true) {
//			picker.clear();
//			picker.add(PirateBaseTier.TIER_1_1MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_2_1MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_4_3MODULE, 10f);
//			picker.add(PirateBaseTier.TIER_5_3MODULE, 10f);
//		}
		
		
		return picker.pick();
	}
	
	public static String RECENTLY_USED_FOR_BASE = "$core_recentlyUsedForBase";
	public static float genBaseUseTimeout() {
		return 120f + 60f * (float) Math.random();
	}
	public static void markRecentlyUsedForBase(StarSystemAPI system) {
		if (system != null && system.getCenter() != null) {
			system.getCenter().getMemoryWithoutUpdate().set(RECENTLY_USED_FOR_BASE, true, genBaseUseTimeout());
		}
	}
	
	protected StarSystemAPI pickSystemForPirateBase() {
		WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<StarSystemAPI>(random);
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasPulsar()) continue;
			if (system.hasTag(Tags.THEME_SPECIAL)) continue;
			if (system.hasTag(Tags.THEME_HIDDEN)) continue;
			
			float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
			if (days < 45f) continue;
			if (system.getCenter().getMemoryWithoutUpdate().contains(RECENTLY_USED_FOR_BASE)) continue;
			
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

	public int getNumDestroyed() {
		return numDestroyed;
	}

	public void setNumDestroyed(int numDestroyed) {
		this.numDestroyed = numDestroyed;
	}
	
	public void incrDestroyed() {
		numDestroyed++;
		numSpawnChecksToSkip = Math.max(numSpawnChecksToSkip, (Tuning.PIRATE_BASE_MIN_TIMEOUT_MONTHS + 
					Misc.random.nextInt(Tuning.PIRATE_BASE_MAX_TIMEOUT_MONTHS - Tuning.PIRATE_BASE_MIN_TIMEOUT_MONTHS + 1))
				* 3); // checks happen every 10 days on average, *3 to get months
	}
	
}















