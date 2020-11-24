package com.fs.starfarer.api.util;

/**
 * Meant to simulate brightness pattern of a lightning strike.
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class FlickerUtilV2 {

	public static final float UP_RATE = 25f;
	public static final float DOWN_RATE = 5f;
	public static final float END_PROB_PER_BURST = 0.1f;
	
	private float brightness;
	private float dir = 1f;
	private float wait;
	private float maxWait;
	private boolean stopBursts = false;
	private boolean stopAll = false;
	
	private float angle;
	private float currMax;
	private float currDur;
	private int numBursts = 0;
	
	private boolean peakFrame = false;
	
	public FlickerUtilV2() {
		this(4f);
	}
	public FlickerUtilV2(float maxWait) {
		this.maxWait = maxWait;
		angle = (float) Math.random() * 360f;
	}
	
	public float getAngle() {
		return angle;
	}
	
	public void newBurst() {
		currMax = 0.75f + (float) Math.random() * 0.5f;
		if (currMax > 1) currMax = 1;
		if (currMax < brightness) currMax = brightness;
		dir = 1f;
		//currDur = 0f + (float) Math.random() * 0.5f;
		currDur = 0f + (float) Math.random() * 0.5f;
		currDur *= currDur;
		currDur += 0.05f;
		numBursts++;
		peakFrame = true;
		//angle = (float) Math.random() * 360f;
	}
	
	public void newWait() {
		wait = (float) Math.random() * maxWait;
		numBursts = 0;
		stopBursts = false;
		angle = (float) Math.random() * 360f;
	}
	
	public void setWait(float wait) {
		this.wait = wait;
	}
	public void setNumBursts(int numBursts) {
		this.numBursts = numBursts;
	}
	public boolean isPeakFrame() {
		return peakFrame;
	}
	
	public int getNumBursts() {
		return numBursts;
	}
	
	public float getWait() {
		return wait;
	}
	
	public void advance(float amount) {
		peakFrame = false;
		if (wait > 0) {
			wait -= amount;
			if (wait > 0) {
				return;
			} else {
				newBurst();
			}
		}
		
		//float timeUp = Math.min(0.1f, currDur / 5f);
		//float timeDown = currDur - timeUp;
		if (dir > 0) {
			//brightness += amount / timeUp;
			brightness += amount * UP_RATE;
		} else {
			//brightness -= amount / timeDown;
			brightness -= amount * DOWN_RATE;
		}
		
		if (brightness < 0) brightness = 0;
		
		if (brightness >= currMax) {
			brightness = currMax;
			dir = -1;
		}
		
		
		currDur -= amount;
		if (currDur <= 0) {
			if (!stopBursts && !stopAll) {
				if ((float) Math.random() < END_PROB_PER_BURST * (float) numBursts) {
					stopBursts = true;
				} else {
					newBurst();
				}
			} else if (!stopAll && brightness <= 0) {
				newWait();
			}
		}
		
	}
	
	public void stop() {
		stopAll = true;
	}

	public float getBrightness() {
		return brightness;
	}
	
	public static void main(String[] args) {
		FlickerUtilV2 test = new FlickerUtilV2();
		
		for (int i = 0; i < 1000; i++) {
			test.advance(0.016f);
			System.out.println(test.getBrightness());
			//System.out.println(test.getAngle());
		}
	}
	
}








