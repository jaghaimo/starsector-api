package com.fs.starfarer.api.util;

public class RollingAverageTracker {

	private IntervalUtil timer;
	private final float f;
	private float elaspedFractionOverride = -1;
	private float curr = 0, avg = 0;
	
	
	public RollingAverageTracker(float minInterval, float maxInterval, float factor) {
		this.f = factor;
		timer = new IntervalUtil(minInterval, maxInterval);
	}
	
	public void advance(float amount) {
		timer.advance(amount);
		if (timer.intervalElapsed()) {
			updateAverage();
		}
	}
	
	public void updateAverage() {
		if (avg <= 0) {
			avg = curr;
		} else {
			avg = avg * (1f - f) + curr * f;
		}
		curr = 0;
	}
	
	public float getCurr() {
		return curr;
	}

	public float getAverage() {
		float e = timer.getElapsed() / timer.getIntervalDuration();
		if (elaspedFractionOverride >= 0) {
			e = elaspedFractionOverride;
		}
		return avg * (1f - f * e) + curr * f * e;
//		return curr;
	}
	
	public void add(float val) {
		curr += val;
	}
	public void sub(float val) {
		curr -= val;
		if (curr < 0) curr = 0;
	}

	public void setElaspedFractionOverride(float elaspedFractionOverride) {
		this.elaspedFractionOverride = elaspedFractionOverride;
	}
	
	
}
