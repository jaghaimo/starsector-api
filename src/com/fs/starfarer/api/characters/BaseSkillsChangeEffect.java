package com.fs.starfarer.api.characters;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseSkillsChangeEffect implements SkillsChangeEffect {

	protected Color base;
	protected Color dark;
	protected Color bright;
	protected Color sBase;
	protected Color sDark;
	protected Color sBright;
	
	protected void prepare() {
		base = Global.getSettings().getBasePlayerColor();
		bright = Global.getSettings().getBrightPlayerColor();
		dark = Global.getSettings().getDarkPlayerColor();
		
		sBase = Misc.getStoryOptionColor();
		sDark = Misc.getStoryDarkColor();
		sBright = Misc.getStoryBrightColor();
	}
	
	public boolean hasEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to) {
		return false;
	}

	public void infoButtonPressed(ButtonAPI button, Object param, Map<String, Object> dataMap) {
		
	}

	public void printEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, TooltipMakerAPI info, Map<String, Object> dataMap) {
		
	}

	public void applyEffects(MutableCharacterStatsAPI from, MutableCharacterStatsAPI to, Map<String, Object> dataMap) {
		
	}

}
