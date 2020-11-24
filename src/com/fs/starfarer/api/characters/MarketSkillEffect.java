package com.fs.starfarer.api.characters;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2017 Fractal Softworks, LLC
 */
public interface MarketSkillEffect extends LevelBasedEffect {
	void apply(MarketAPI market, String id, float level);
	void unapply(MarketAPI market, String id);
}
