package com.fs.starfarer.api.characters;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface ShipSkillEffect extends LevelBasedEffect {
	void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level);
	void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id);
}
