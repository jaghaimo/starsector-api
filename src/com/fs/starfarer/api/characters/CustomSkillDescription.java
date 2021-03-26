package com.fs.starfarer.api.characters;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CustomSkillDescription extends LevelBasedEffect {
	boolean hasCustomDescription();
	void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width);
}
