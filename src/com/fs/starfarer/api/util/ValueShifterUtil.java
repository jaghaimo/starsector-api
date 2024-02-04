package com.fs.starfarer.api.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValueShifterUtil implements ValueShifterAPI {
	
	public static class ShiftData2 {
		public float to;
		public FaderUtil fader;
		public float shift;
		public boolean nudged;
	}
	
	protected float base;
	protected float curr;
	protected boolean useSquareOfProgress = true;
	transient protected float averageShift;
	protected Map<Object, ShiftData2> data = new LinkedHashMap<Object, ShiftData2>();

	public ValueShifterUtil(float base) {
		this.base = base;
		this.curr = base;
	}

	public boolean isUseSquareOfProgress() {
		return useSquareOfProgress;
	}

	public void setUseSquareOfProgress(boolean useSquareOfProgress) {
		this.useSquareOfProgress = useSquareOfProgress;
	}


	public boolean isShifted() {
		//return base != curr;
		return !data.isEmpty();
	}
	
	public float getBase() {
		return base;
	}

	public void setBase(float base) {
		this.base = base;
	}
	
	public float getCurr() {
		return curr;
	}

	public void shift(Object source, float to, float durIn, float durOut, float shift) {
		ShiftData2 sd = data.get(source);
		if (sd == null) {
			sd = new ShiftData2();
			sd.fader = new FaderUtil(0, durIn, durOut);
			sd.fader.setBounceDown(true);
			data.put(source, sd);
		}
		sd.to = to;
		sd.shift = shift;
		sd.fader.setDuration(durIn, durOut);
		sd.fader.fadeIn();
		sd.nudged = true;
	}
	
	public void advance(float amount) {
		Iterator<ShiftData2> iter = data.values().iterator();
		while (iter.hasNext()) {
			ShiftData2 sd = iter.next();
			if (!sd.nudged) sd.fader.fadeOut();
			sd.nudged = false;
			sd.fader.advance(amount);
			if (sd.fader.isFadedOut()) {
				iter.remove();
			}
		}
		updateCurr();
	}
	
	protected void updateCurr() {
		if (data.isEmpty()) {
			curr = base;
			averageShift = 0f;
			return;
		}
		
		float totalWeight = 0f;
		for (ShiftData2 sd : data.values()) {
			float progress = sd.fader.getBrightness();
			if (useSquareOfProgress) progress *= progress;
			totalWeight += progress;
		}
		averageShift = totalWeight / (float) data.size();
		//totalWeight = data.size();
		
		if (totalWeight <= 0) {
			curr = base;
			return;
		}
		
		float result = base;
		
		for (ShiftData2 sd : data.values()) {
			float progress = sd.fader.getBrightness();
			if (useSquareOfProgress) progress *= progress;
			
			result += (sd.to - base) * sd.shift * progress / (totalWeight - progress + 1f);
		}
		
		curr = result;
	}
	
	public float getAverageShift() {
		return averageShift;
	}
	
	public float getShiftProgress(Object key) {
		if (data.containsKey(key)) {
			return data.get(key).fader.getBrightness();
		}
		return 0f;
	}

	public static void main(String[] args) {
		ValueShifterUtil c = new ValueShifterUtil(10f);
		
		for (int i = 0; i < 10; i++) {
			c.shift("c1", 25f, 1f, 1f, 1f);
			c.shift("c2", 20f, 1f, 1f, 1f);
			c.advance(0.1f);
		}
		
		System.out.println(c.getCurr());

//		c.advance(0.1f);
//		System.out.println(c.getCurr());
	}
	
}

















