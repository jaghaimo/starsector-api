package com.fs.starfarer.api.campaign.ai;

/**
 * Variables set in memory that indicate certain decisions or behaviors
 * of the AI. Used to communicate between otherwise-disconnected modules, such
 * as ability AI and other fleet AI modules.
 * 
 * Mods replacing parts of the fleet AI should try to use these if possible/appropriate.
 * 
 * Mods replacing fleet AI entirely (i.e. all modules, and all ability AI) are free
 * to either use these or devise their own system.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class FleetAIFlags {
	
	/**
	 * SectorEntityToken, set if actively pursuing something.
	 */
	public static final String PURSUIT_TARGET = "$ai_pursuitTarget";
	
	
	/**
	 * SectorEntityToken, nearest enemy the AI is trying to avoid.
	 */
	public static final String NEAREST_FLEEING_FROM = "$ai_fleeingFrom";
	
	
	/**
	 * SectorEntityToken, nearest enemy. May or may not be trying to avoid.
	 */
	public static final String NEAREST_ENEMY = "$ai_nearestEnemy";
	
	
	/**
	 * Vector2f, Where it wants to go.
	 */
	public static final String TRAVEL_DEST = "$ai_travelDest";
	
	
	/**
	 * Vector2f, Where it's actually going (due to avoiding enemies/terrain/etc).
	 * Not set while orbiting.
	 */
	public static final String MOVE_DEST = "$ai_moveDest";
	
	
	
	/**
	 * Boolean, whether the AI wants to keep the transponder on.
	 */
	public static final String WANTS_TRANSPONDER_ON = "$ai_wantsTransponderOn";
	
	
	
	/**
	 * Float, days spent pursuing a target that's no longer visible.
	 */
	public static final String DAYS_TARGET_UNSEEN = "$ai_daysTargetUnseen";
	
	/**
	 * Vector2f, last location where the target was seen.
	 */
	public static final String LAST_SEEN_TARGET_LOC = "$ai_lastSeenTargetLoc";
	
	/**
	 * Float, direction the target was going when last seen.
	 */
	public static final String LAST_SEEN_TARGET_HEADING = "$ai_lastSeenTargetHeading";
	
	
	/**
	 * Boolean, whether the fleet has an ability-induced speed penalty.
	 */
	public static final String HAS_SPEED_PENALTY = "$ai_hasSpeedPenalty";
	
	public static final String USED_INTERDICTION_PULSE = "$ai_usedIP";
	
	public static final String HAS_VISION_PENALTY = "$ai_hasVisionPenalty";
	public static final String HAS_SPEED_BONUS = "$ai_hasSpeedBonus";
	public static final String HAS_VISION_BONUS = "$ai_hasVisionBonus";
	
	public static final String HAS_LOWER_DETECTABILITY = "$ai_hasLowerDetectability";
	public static final String HAS_HIGHER_DETECTABILITY = "$ai_hasHigherDetectability";
	
	
}











