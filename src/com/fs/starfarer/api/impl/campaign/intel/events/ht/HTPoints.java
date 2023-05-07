package com.fs.starfarer.api.impl.campaign.intel.events.ht;

/**
 * Hyperspace Topography event progress point values
 * @author Alex
 *
 *	Each point is worth roughly 200 credits after the event bar is maxed out and "Topographic Data" items are
 *  generated periodically.
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public class HTPoints {

	// neutrino burst from sensor array; one of the main ways
	public static int NEUTRINO_BURST_VOLATILES_COST = 3;
	
	public static int NEUTRINO_BURST_MAKESHIFT = 10;
	public static int NEUTRINO_BURST_DOMAIN = 20;
	
	public static int SCAVENGER_MIN = 5;
	public static int SCAVENGER_MAX = 10;
	
	// various ASB scans
	public static int SCAN_BLACK_HOLE_LONG_RANGE = 15;
	public static int SCAN_BLACK_HOLE_SHORT_RANGE = 40;
	public static int SCAN_ION_STORM = 50;
	public static int SCAN_NEBULA = 15;
	public static int SCAN_BINARY = 20;
	public static int SCAN_TRINARY = 30;
	public static int SCAN_GAS_GIANT = 5;
	public static int SCAN_NEUTRON_STAR = 10;
	public static int SCAN_PULSAR_BEAM = 25;
	
	// from data found as a salvage special, see TopographicDataSpecial
	public static int LOW_MIN = 10;
	public static int LOW_MAX = 20;
	public static int MEDIUM_MIN = 30;
	public static int MEDIUM_MAX = 50;
	public static int HIGH_MIN = 60;
	public static int HIGH_MAX = 100;

	// traveling at burn above 20 in hyperspace
	public static int PER_DAY_AT_BURN_20 = 0;
	public static int PER_DAY_AT_BURN_30 = 3;
	public static int PER_DAY_AT_BURN_40 = 6;
	public static int PER_DAY_AT_BURN_50 = 12;
	public static int BURN_POINT_CHUNK_SIZE = 20; // once this many points are accumulated, they get given to the player
	

	
}
