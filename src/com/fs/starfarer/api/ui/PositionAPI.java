package com.fs.starfarer.api.ui;

import com.fs.starfarer.api.input.InputEventAPI;

public interface PositionAPI {
	float getX();
	float getY();
	float getWidth();
	float getHeight();
	float getCenterX();
	float getCenterY();
	
	PositionAPI setLocation(float x, float y);
	PositionAPI setSize(float width, float height);
	
	boolean containsEvent(InputEventAPI event);
	PositionAPI setXAlignOffset(float xAlignOffset);
	PositionAPI setYAlignOffset(float yAlignOffset);
	
	PositionAPI inTL(float xPad, float yPad);
	PositionAPI inTMid(float yPad);
	PositionAPI inTR(float xPad, float yPad);
	PositionAPI inRMid(float xPad);
	PositionAPI inMid();
	PositionAPI inBR(float xPad, float yPad);
	PositionAPI inBMid(float yPad);
	PositionAPI inBL(float xPad, float yPad);
	PositionAPI inLMid(float xPad);
	
	
	PositionAPI leftOfTop(UIComponentAPI sibling, float xPad);
	PositionAPI leftOfMid(UIComponentAPI sibling, float xPad);
	PositionAPI leftOfBottom(UIComponentAPI sibling, float xPad);
	PositionAPI rightOfTop(UIComponentAPI sibling, float xPad);
	PositionAPI rightOfMid(UIComponentAPI sibling, float xPad);
	PositionAPI rightOfBottom(UIComponentAPI sibling, float xPad);
	PositionAPI aboveLeft(UIComponentAPI sibling, float yPad);
	PositionAPI aboveMid(UIComponentAPI sibling, float yPad);
	PositionAPI aboveRight(UIComponentAPI sibling, float yPad);
	PositionAPI belowLeft(UIComponentAPI sibling, float yPad);
	PositionAPI belowMid(UIComponentAPI sibling, float yPad);
	PositionAPI belowRight(UIComponentAPI sibling, float yPad);
	
	void setSuspendRecompute(boolean suspendRecompute);
	boolean isSuspendRecompute();
	
	
}
