package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.Global;


public class DebugFlags {

	public static boolean ALLOW_KNOWN_HULLMOD_DROPS = false;
	
	public static boolean WITH_HYPER_STATION = false;
	
	public static boolean PRINT_RULES_DEBUG_INFO = false;
	
	public static boolean OBJECTIVES_DEBUG = false; // Can build for free at stable locations.
	
	public static boolean COLONY_DEBUG = false; // Surveying and establishing colonies is free.
	public static boolean ALLOW_VIEW_UNEXPLORED_SYSTEM_MAP = false; // can click on unexplored stars and see the system map
	public static boolean MARKET_HOSTILITIES_DEBUG = false; // can bombard and raid regardless of defenses
	public static boolean HIDE_COLONY_CONTROLS = false; // generally want this set to false
	
	
	public static boolean HEGEMONY_INSPECTION_DEBUG = false; // inspections quickly start and arrive
	public static boolean PUNITIVE_EXPEDITION_DEBUG = false; // punitive expeditions quickly start and arrive
	
	public static boolean FAST_RAIDS = false; // expeditions, raids, etc arrive quickly
	
	public static boolean RAID_DEBUG = false; // don't set this to true
	
	public static boolean PATHER_BASE_DEBUG = false;
	public static boolean DECIV_DEBUG = false;
	
	public static boolean FAST_PATROL_SPAWN = false;
	
	public static boolean PERSON_BOUNTY_DEBUG_INFO = false;
	
	public static boolean BAR_DEBUG = false; // all bar events generated w/o limit and more quickly
	
	
	// not really a debug flag...
	public static boolean SEND_UPDATES_WHEN_NO_COMM = false;
	
	
	
	static {
		setStandardConfig();
		
//		HEGEMONY_INSPECTION_DEBUG = true;
//		PUNITIVE_EXPEDITION_DEBUG = true;
//		FAST_PATROL_SPAWN = true;
//		PERSON_BOUNTY_DEBUG_INFO = true;
		
		if (Global.getSettings().getBoolean("playtestingMode")) {
			setPlaytestingConfig();
		}
		
//		PATHER_BASE_DEBUG = true;
//		RAID_DEBUG = true;
//		FAST_PATROL_SPAWN = true;
//		ALLOW_VIEW_UNEXPLORED_SYSTEM_MAP = true;
//		COLONY_DEBUG = true;
//		COLONY_DEBUG = false;
//		PUNITIVE_EXPEDITION_DEBUG = true;
//		HEGEMONY_INSPECTION_DEBUG = true;
//		MARKET_HOSTILITIES_DEBUG = true;
	}
	
	
	public static void setStandardConfig() {
		boolean dev = Global.getSettings().isDevMode();
		PRINT_RULES_DEBUG_INFO = dev;
		OBJECTIVES_DEBUG = dev;
		
		COLONY_DEBUG = dev;
		ALLOW_VIEW_UNEXPLORED_SYSTEM_MAP = dev;
		MARKET_HOSTILITIES_DEBUG = dev;
		
		HEGEMONY_INSPECTION_DEBUG = false;
		PUNITIVE_EXPEDITION_DEBUG = false;
		RAID_DEBUG = false;
		DECIV_DEBUG = false;
		FAST_RAIDS = false;
		
		BAR_DEBUG = dev;
		
		FAST_PATROL_SPAWN = false;
		
		PERSON_BOUNTY_DEBUG_INFO = dev;
	}
	
	public static void setPlaytestingConfig() {
		PRINT_RULES_DEBUG_INFO = false;
		OBJECTIVES_DEBUG = false;
		
		COLONY_DEBUG = false;
		ALLOW_VIEW_UNEXPLORED_SYSTEM_MAP = false;
		MARKET_HOSTILITIES_DEBUG = false;
		
		HEGEMONY_INSPECTION_DEBUG = false;
		PUNITIVE_EXPEDITION_DEBUG = false;
		RAID_DEBUG = false;
		DECIV_DEBUG = false;
		FAST_RAIDS = false;
		
		BAR_DEBUG = false;
		
		FAST_PATROL_SPAWN = false;
		
		PERSON_BOUNTY_DEBUG_INFO = false;
	}
	
	
	
	
}
