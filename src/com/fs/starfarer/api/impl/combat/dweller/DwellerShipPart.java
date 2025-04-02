package com.fs.starfarer.api.impl.combat.dweller;

import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.ValueShifterUtil;

public interface DwellerShipPart {
	String getId();
	ValueShifterUtil getBrightness();
	void fadeOut();
	void setAlphaMult(float alphaMult);
	float getAlphaMult();
	void fadeIn();
	FaderUtil getFader();
	void advance(float amount);
	void render(float x, float y, float alphaMult, float angle, CombatEngineLayers layer);
	Set<String> getTags();
	void addTag(String tag);
	void removeTag(String tag);
	boolean hasTag(String tag);
	Color getColor();
	void setColor(Color color);
}