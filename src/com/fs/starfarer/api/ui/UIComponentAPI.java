package com.fs.starfarer.api.ui;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

public interface UIComponentAPI {
	PositionAPI getPosition();

	void render(float alphaMult);
	void processInput(List<InputEventAPI> events);
	void advance(float amount);

	void setOpacity(float opacity);
	float getOpacity();
}
