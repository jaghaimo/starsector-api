package com.fs.starfarer.api.characters;

import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface FleetStatsSkillEffect extends LevelBasedEffect {
	void apply(MutableFleetStatsAPI stats, String id, float level);
	void unapply(MutableFleetStatsAPI stats, String id);
}
