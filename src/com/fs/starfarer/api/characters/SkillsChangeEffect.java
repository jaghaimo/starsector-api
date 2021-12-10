package com.fs.starfarer.api.characters;

import java.util.Map;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface SkillsChangeEffect {

	void infoButtonPressed(ButtonAPI button, Object param, Map<String, Object> dataMap);
	
	boolean hasEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to);
	void printEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, TooltipMakerAPI info, Map<String, Object> dataMap);
	void applyEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, Map<String, Object> dataMap);
}
