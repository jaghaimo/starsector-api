package com.fs.starfarer.api.combat;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public class MutableStatWithTempMods extends MutableStat {

	public static class TemporaryStatMod {
		float timeRemaining; // days or seconds, whatever
		String source;
		public TemporaryStatMod(float timeRemaining, String source) {
			this.timeRemaining = timeRemaining;
			this.source = source;
		}
	}
	
	private LinkedHashMap<String, TemporaryStatMod> tempMods = new LinkedHashMap<String, TemporaryStatMod>();
	
	public MutableStatWithTempMods(float base) {
		super(base);
	}
	
	protected Object readResolve() {
		super.readResolve();
//		if (tempMods == null) {
//			tempMods = new LinkedHashMap<String, TemporaryStatMod>();
//		}
//		if (flatAfterMult == null) {
//			flatAfterMult = new HashMap<String, StatMod>();
//		}
		return this;
	}
	
	protected Object writeReplace() {
		if (tempMods != null && getMods().isEmpty()) {
			tempMods = null;
		}
		return this;
	}
	
	
	public void removeTemporaryMod(String source) {
		TemporaryStatMod mod = getMods().remove(source);
		if (mod == null) return;
		
		unmodify(mod.source);
	}
	
	private TemporaryStatMod getMod(String source, float durInDays) {
		TemporaryStatMod mod = getMods().get(source);
		if (mod == null) {
			mod = new TemporaryStatMod(durInDays, source);
			getMods().put(source, mod);
		}
		mod.timeRemaining = durInDays;
		return mod;
	}
	
	public void addTemporaryModFlat(float durInDays, String source, String desc, float value) {
		getMod(source, durInDays);
		modifyFlat(source, value, desc);
	}
	public void addTemporaryModMult(float durInDays, String source, String desc, float value) {
		getMod(source, durInDays);
		modifyMult(source, value, desc);
	}
	public void addTemporaryModFlat(float durInDays, String source, float value) {
		getMod(source, durInDays);
		modifyFlat(source, value);
	}
	public void addTemporaryModPercent(float durInDays, String source, String desc, float value) {
		getMod(source, durInDays);
		modifyPercent(source, value, desc);
	}
	public void addTemporaryModPercent(float durInDays, String source, float value) {
		getMod(source, durInDays);
		modifyPercent(source, value);
	}
	
	public Map<String, TemporaryStatMod> getMods() {
		if (tempMods == null) {
			tempMods = new LinkedHashMap<String, TemporaryStatMod>();
		}
		return tempMods;
	}
	public boolean hasMod(String source) {
		return getMods().containsKey(source);
	}
	
	public void advance(float days) {
		if (tempMods == null || getMods().isEmpty()) return;
		
		Iterator<TemporaryStatMod> iter = getMods().values().iterator();
		while (iter.hasNext()) {
			TemporaryStatMod mod = iter.next();
			mod.timeRemaining -= days;
			if (mod.timeRemaining <= 0) {
				iter.remove();
				unmodify(mod.source);
			}
		}
	}
}











