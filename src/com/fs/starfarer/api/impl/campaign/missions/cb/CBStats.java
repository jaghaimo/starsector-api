package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.util.Pair;

public class CBStats {
	
	public static int BASE_REWARD = Global.getSettings().getInt("baseCustomBounty");
	public static int REWARD_PER_DIFFICULTY = Global.getSettings().getInt("personCustomBountyPerLevel");
	
	public static float DEFAULT_DAYS = 120;
	public static float REMNANT_STATION_DAYS = 365;
	public static float ENEMY_STATION_DAYS = 365;
	public static float REMNANT_PLUS_DAYS = 0; // no time limit
	
	// offer frequency
	public static float PATHER_FREQ = 0.5f;
	public static float PIRATE_FREQ = 1f;
	public static float DESERTER_FREQ = 1f;
	public static float DERELICT_FREQ = 0.5f;
	public static float REMNANT_FREQ = 0.5f;
	public static float REMNANT_STATION_FREQ = 0.5f;
	public static float MERC_FREQ = 0.5f;
	public static float REMNANT_PLUS_FREQ = 1f;
	public static float ENEMY_STATION_FREQ = 0.5f;
	
	// bounty mult
	public static float PATHER_MULT = 0.8f;
	public static float PIRATE_MULT = 0.8f;
	public static float DESERTER_MULT = 1f;
	public static float DERELICT_MULT = 1.2f;
	public static float REMNANT_MULT = 1.75f;
	public static float REMNANT_STATION_MULT = 2f;
	public static float MERC_MULT = 2f;
	public static float REMNANT_PLUS_MULT = 3f;
	public static float ENEMY_STATION_MULT = 2f;
	
	// difficulty thresholds, maps to a Pair
	// if difficulty is >= Pair.one: the bounty is unavailable as a "more challenging" option
	// if difficulty is >= Pair.two: the bounty is only available as an "easier" option
	public static Map<Class, Pair<Integer, Integer>> THRESHOLDS = 
							new LinkedHashMap<Class, Pair<Integer,Integer>>();
	public static void setThresholds(Class bounty, int challenging, int normal) {
		THRESHOLDS.put(bounty, new Pair<Integer, Integer>(challenging, normal));
	}
	static {
		setThresholds(CBPirate.class, 5, 8);
		setThresholds(CBPather.class, 5, 8);
		setThresholds(CBDeserter.class, 8, 12);
		setThresholds(CBMerc.class, 12, 12);
		setThresholds(CBDerelict.class, 12, 12);
		setThresholds(CBRemnant.class, 12, 12);
		setThresholds(CBRemnantPlus.class, 12, 12);
		setThresholds(CBRemnantStation.class, 12, 12);
		setThresholds(CBEnemyStation.class, 12, 12);
	}
	public static int getThresholdNotHigh(Class c) {
		int result = 12;
		if (!THRESHOLDS.containsKey(c)) return result;
		return THRESHOLDS.get(c).one;
	}
	public static int getThresholdNotNormal(Class c) {
		int result = 12;
		if (!THRESHOLDS.containsKey(c)) return result;
		return THRESHOLDS.get(c).two;
	}
	
	
	// offer frequency
	public static float TRADER_FREQ = 1f;
	public static float PATROL_FREQ = 1f;
	
	// bounty mult
	public static float TRADER_MULT = 0.5f;
	public static float PATROL_MULT = 0.67f;
	
	
	
	public static int getBaseBounty(int difficulty, float mult, BaseHubMission mission) {
		int baseReward = CBStats.BASE_REWARD + difficulty * CBStats.REWARD_PER_DIFFICULTY;
		baseReward *= mult;
		if (mission != null) {
			baseReward *= 0.9f + 0.2f * mission.getGenRandom().nextFloat();
			baseReward = BaseHubMission.getRoundNumber(baseReward);
		}
		return baseReward;
	}

}


