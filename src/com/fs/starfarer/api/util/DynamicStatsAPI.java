package com.fs.starfarer.api.util;

import java.util.Map;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;

/**
 * Mutable stats that are created on-demand when they're needed, unlike
 * the "standard" stats that have to be hardcoded.
 * 
 * Intended to be useful for stuff like the interactions between terrain,
 * hull mod effects, and character skills. It's desirable for them to be
 * able to affect each other, but since all can be modded in from scratch,
 * it's hard to rely on the hardcoded stat set.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public interface DynamicStatsAPI {

	/**
	 * Base value of the stat is 1.
	 * @param id
	 * @return
	 */
	MutableStat getStat(String id);
	
	/**
	 * Base value of the stat is 1.
	 * @param id
	 * @return
	 */
	float getValue(String id);
	
	
	StatBonus getMod(String id);
	float getValue(String id, float base);
	
	
	void removeUmodified();
	
	Map<String, MutableStat> getStats();
	Map<String, StatBonus> getMods();
	
	
	boolean isEmpty();

}
