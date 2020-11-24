package com.fs.starfarer.api.util;

public interface ValueShifterAPI {
	float getBase();
	void setBase(float base);
	float getCurr();
	
	/**
	 * Durations are in seconds, NOT days.
	 * @param source
	 * @param to
	 * @param durIn
	 * @param durOut
	 * @param shift
	 */
	void shift(Object source, float to, float durIn, float durOut, float shift);
	
	
	void advance(float amount);
}
