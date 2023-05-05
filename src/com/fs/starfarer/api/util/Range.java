package com.fs.starfarer.api.util;

import java.util.Random;

import com.fs.starfarer.api.Global;

public class Range {

	public float min, max, range;
	
	public Range(String settingsKey) {
		min = Global.getSettings().getFloatFromArray(settingsKey, 0);
		max = Global.getSettings().getFloatFromArray(settingsKey, 1);
		range = max - min;
	}
	
	public float rollFloat(Random random) {
		if (random == null) random = Misc.random;
		return min + random.nextFloat() * range;
	}
	public int rollInt(Random random) {
		if (random == null) random = Misc.random;
		return (int) min + random.nextInt((int) range + 1);
	}
	
	public float interpFloat(float t) {
		return min + range * t;
	}
	
	public int interpInt(float t) {
		return (int) min + (int) Math.round(t * range);
	}
}
