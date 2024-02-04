package com.fs.starfarer.api.util;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ColorShifterUtil implements ColorShifterAPI {
	
	public static class ShiftData2 {
		public Color to;
		public FaderUtil fader;
		public float shift;
		public boolean nudged;
	}
	
	protected Color base;
	protected Color curr;
	protected boolean useSquareOfProgress = true;
	protected Map<Object, ShiftData2> data = new LinkedHashMap<Object, ShiftData2>();

	public ColorShifterUtil(Color base) {
		this.base = base;
		this.curr = base;
	}
	
	public boolean isUseSquareOfProgress() {
		return useSquareOfProgress;
	}

	public void setUseSquareOfProgress(boolean useSquareOfProgress) {
		this.useSquareOfProgress = useSquareOfProgress;
	}

	public Color getBase() {
		return base;
	}

	public void setBase(Color base) {
		this.base = base;
	}
	
	public Color getCurr() {
		return curr;
	}

	public void shift(Object source, Color to, float durIn, float durOut, float shift) {
		if (to == null) {
			data.remove(source);
			return;
		}
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
	
	public boolean isShifted() {
		//return curr != base;
		return !data.isEmpty();
	}
	
	protected void updateCurr() {
		if (data.isEmpty()) {
			curr = base;
			return;
		}
		curr = getCurrForBase(base);
	}
	
	public Color getCurrForBase(Color diffBase) {
		if (data == null || data.isEmpty()) {
			return diffBase;
		}
		
		float totalWeight = 0f;
		for (ShiftData2 sd : data.values()) {
			float progress = sd.fader.getBrightness();
			if (useSquareOfProgress) progress *= progress;
			totalWeight += progress;
		}
//		totalWeight = data.size();
		
		if (totalWeight <= 0) {
			return diffBase;
		}
		
		float red = (float)diffBase.getRed();
		float green = (float)diffBase.getGreen();
		float blue = (float)diffBase.getBlue();
		float alpha = (float)diffBase.getAlpha();
		
		for (ShiftData2 sd : data.values()) {
			float progress = sd.fader.getBrightness();
			if (useSquareOfProgress) progress *= progress;
			float currWeight = totalWeight - progress + 1f;
			red += ((float)sd.to.getRed() - (float)diffBase.getRed()) * sd.shift * progress / currWeight;
			green += ((float)sd.to.getGreen() - (float)diffBase.getGreen()) * sd.shift * progress / currWeight;
			blue += ((float)sd.to.getBlue() - (float)diffBase.getBlue()) * sd.shift * progress / currWeight;
			alpha += ((float)sd.to.getAlpha() - (float)diffBase.getAlpha()) * sd.shift * progress / currWeight;
		}
		
		if (red > 255) red = 255;
		if (green > 255) green = 255;
		if (blue > 255) blue = 255;
		if (alpha > 255) alpha = 255;
		if (red < 0) red = 0;
		if (green < 0) green = 0;
		if (blue < 0) blue = 0;
		if (alpha < 0) alpha = 0;
		
		return new Color((int)red, (int)green, (int)blue, (int)alpha);
	}
	
	public static void main(String[] args) {
		ColorShifterUtil c = new ColorShifterUtil(Color.red);
		
		for (int i = 0; i < 10; i++) {
			c.shift("c1", Color.green, 1f, 1f, 1f);
			c.shift("c2", Color.blue, 1f, 1f, 0.5f);
			c.advance(0.1f);
		}
		
		System.out.println(c.getCurr());

//		c.advance(0.1f);
//		System.out.println(c.getCurr());
	}
	
}

















