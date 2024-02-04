package com.fs.starfarer.api.fleet;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.ColorShifterAPI;
import com.fs.starfarer.api.util.ValueShifterAPI;

public interface FleetMemberViewAPI {

	ColorShifterAPI getEngineColor();
	ValueShifterAPI getEngineWidthMult();
	ValueShifterAPI getEngineHeightMult();
	ColorShifterAPI getEngineGlowColor();
	ValueShifterAPI getEngineGlowSizeMult();
	ColorShifterAPI getContrailColor();
	ValueShifterAPI getContrailWidthMult();
	ValueShifterAPI getContrailDurMult();
	ColorShifterAPI getGlowColor();
	FleetMemberAPI getMember();
	ColorShifterAPI getWindEffectColor();
	//ValueShifterAPI getWindEffectLengthMult();
	ValueShifterAPI getWindEffectDirX();
	ValueShifterAPI getWindEffectDirY();
	void setJitter(float durIn, float durOut, Color color, int copies, float maxJitterRange);
	boolean isJittering();
	void overrideOffset(float x, float y);
	void setJitterBrightness(float b);
	void endJitter();
	void setUseCircularJitter(boolean circular);
	void setJitterDirection(Vector2f jitterDirection);
	void setJitterLength(float jitterLength);

}
