package com.fs.starfarer.api.ui;

import java.awt.Color;

/**
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public interface LabelAPI {
	void setHighlight(int start, int end);
	void highlightFirst(String substring);
	void highlightLast(String substring);
	void setHighlight(String ... substrings);
	void unhighlightIndex(int index);
	void setHighlightColor(Color color);
	void setHighlightColors(Color ... colors);
	void setAlignment(Alignment mid);
	void setText(String text);
	String getText();
	void setOpacity(float opacity);
	float getOpacity();
	PositionAPI getPosition();
	
	void italicize();
	/**
	 * @param shear amount of shear to the right
	 */
	void italicize(float shear);
	void unitalicize();
	PositionAPI autoSizeToWidth(float width);
	
	float computeTextWidth(String in);
	float computeTextHeight(String in);
	void flash(float durIn, float durOut);
	void render(float alphaMult);
	void advance(float amount);
}
