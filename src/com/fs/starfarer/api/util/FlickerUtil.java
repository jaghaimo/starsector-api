package com.fs.starfarer.api.util;

public class FlickerUtil {
	private float angle;
	private float brightness;
	private float currTime;
	private float currMaxBurstTime;
	private float currMaxBrightness;
	private float maxBurstTime;
	private float peakTime;
	private float peakDur;
	private boolean stop = false;
	
	public FlickerUtil() {
		//maxBurstTime = 0.25f;
		//maxBurstTime = 1f;
		maxBurstTime = 0.5f;
		newBurst();
	}
	
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	private void newBurst() {
		if (stop) {
			brightness = 0f;
			return;
		}
		currMaxBurstTime = maxBurstTime * 0.25f + (float) Math.random() * maxBurstTime * 0.75f;
		//currMaxBrightness = (float) Math.random() * 0.75f + 0.5f;
		currMaxBrightness = (float) Math.random() * 0.5f + 0.75f;
		//currMaxBrightness = 1f;
		if (currMaxBrightness > 1) currMaxBrightness = 1;
		//peakTime = currMaxBurstTime / 3f;
		//peakDur = peakTime/2f;
		peakTime = 0f;
		//peakDur = currMaxBurstTime / 5f;
		peakDur = currMaxBurstTime / 20f;
		currTime = 0f;
		
		angle = (float) Math.random() * 360f;
		brightness = 1f;
	}
	
	public float getBrightness() {
		return brightness * currMaxBrightness;
	}
	
	public float getAngle() {
		return angle;
	}
	
	/**
	 * Returns true if the current burst ended and a new one just started.
	 * @param amount
	 * @return
	 */
	public boolean advance(float amount) {
		currTime += amount;
		
		if (currTime > currMaxBurstTime) {
			newBurst();
			return true;
		} else {
			if (currTime > peakTime + peakDur){
				//brightness -= amount / (currMaxBurstTime - peakTime - peakDur);
				brightness -= amount / Math.max(0.1f, 0.25f - peakTime - peakDur);
				if (brightness < 0) brightness = 0;
			} else {
				brightness = 1f;
			}
		}
		return false;
	}

	public float getCurrTime() {
		return currTime;
	}
	
	

}
