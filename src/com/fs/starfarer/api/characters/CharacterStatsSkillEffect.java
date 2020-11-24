package com.fs.starfarer.api.characters;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CharacterStatsSkillEffect extends LevelBasedEffect {
	void apply(MutableCharacterStatsAPI stats, String id, float level);
	void unapply(MutableCharacterStatsAPI stats, String id);
}
