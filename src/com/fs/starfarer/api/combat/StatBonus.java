package com.fs.starfarer.api.combat;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.MutableStat.StatModType;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public class StatBonus {

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		StatBonus other = (StatBonus) obj;
		if (Float.floatToIntBits(flatBonus) != Float.floatToIntBits(other.flatBonus)) return false;
		if (needsRecompute != other.needsRecompute) return false;
		if (Float.floatToIntBits(percentMod) != Float.floatToIntBits(other.percentMod)) return false;
		if (Float.floatToIntBits(mult) != Float.floatToIntBits(other.mult)) return false;
		
		if (flatBonuses == null) {
			if (other.flatBonuses != null) return false;
		} else {
			if (flatBonuses.size() != other.flatBonuses.size()) return false;
			for (String key : flatBonuses.keySet()) {
				if (!other.flatBonuses.containsKey(key)) return false;
				StatMod mod = flatBonuses.get(key);
				StatMod otherMod = other.flatBonuses.get(key);
				if (!mod.source.equals(otherMod.source)) return false;
				if (Float.floatToIntBits(mod.value) != Float.floatToIntBits(otherMod.value)) return false;
			}
		}
		
		if (multBonuses == null) {
			if (other.multBonuses != null) return false;
		} else if (!multBonuses.equals(other.multBonuses)) {
			if (multBonuses.size() != other.multBonuses.size()) return false;
			for (String key : multBonuses.keySet()) {
				if (!other.multBonuses.containsKey(key)) return false;
				StatMod mod = multBonuses.get(key);
				StatMod otherMod = other.multBonuses.get(key);
				if (!mod.source.equals(otherMod.source)) return false;
				if (Float.floatToIntBits(mod.value) != Float.floatToIntBits(otherMod.value)) return false;
			}
		}
		
		if (percentBonuses == null) {
			if (other.percentBonuses != null) return false;
		} else if (!percentBonuses.equals(other.percentBonuses)) {
			if (percentBonuses.size() != other.percentBonuses.size()) return false;
			for (String key : percentBonuses.keySet()) {
				if (!other.percentBonuses.containsKey(key)) return false;
				StatMod mod = percentBonuses.get(key);
				StatMod otherMod = other.percentBonuses.get(key);
				if (!mod.source.equals(otherMod.source)) return false;
				if (Float.floatToIntBits(mod.value) != Float.floatToIntBits(otherMod.value)) return false;
			}
		}
		return true;
	}

	public float flatBonus = 0f;
	public float mult = 1f;
	public float percentMod = 0f;
	
//	private HashMap<String, StatMod> flatBonuses = new HashMap<String, StatMod>();
//	private HashMap<String, StatMod> percentBonuses = new HashMap<String, StatMod>();
//	private HashMap<String, StatMod> multBonuses = new HashMap<String, StatMod>();
	private LinkedHashMap<String, StatMod> flatBonuses;
	private LinkedHashMap<String, StatMod> percentBonuses;
	private LinkedHashMap<String, StatMod> multBonuses;
	
	private boolean needsRecompute = false;
	public StatBonus() {
	}
	
	Object readResolve() {
		if (flatBonuses == null) {
			flatBonuses = new LinkedHashMap<String, StatMod>();
		}
		if (percentBonuses == null) {
			percentBonuses = new LinkedHashMap<String, StatMod>();
		}
		if (multBonuses == null) {
			multBonuses = new LinkedHashMap<String, StatMod>();
		}
		return this;
	}
	
	public boolean isUnmodified() {
		return (flatBonuses == null || getFlatBonuses().isEmpty()) &&
				(multBonuses == null || getMultBonuses().isEmpty()) && 
				(percentBonuses == null || getPercentBonuses().isEmpty());
	}
	
	public StatMod getFlatBonus(String source) {
		return getFlatBonuses().get(source);
	}
	
	public StatMod getPercentBonus(String source) {
		return getPercentBonuses().get(source);
	}
	
	public StatMod getMultBonus(String source) {
		return getMultBonuses().get(source);
	}
	
	public void modifyFlat(String source, float value) {
		modifyFlat(source, value, null);
	}
	public void modifyFlat(String source, float value, String desc) {
		StatMod mod = getFlatBonuses().get(source);
		if (mod == null && value == 0) return;
		if (mod != null && mod.value == value) return;
		
		mod = new StatMod(source, StatModType.FLAT, value, desc);
		getFlatBonuses().put(source, mod);
		needsRecompute = true;
	}
	
	public void modifyPercent(String source, float value) {
		modifyPercent(source, value, null);
	}
	
	public void modifyPercent(String source, float value, String desc) {
		StatMod mod = getPercentBonuses().get(source);
		if (mod == null && value == 0) return;
		if (mod != null && mod.value == value) return;
		
		mod = new StatMod(source, StatModType.PERCENT, value, desc);
		getPercentBonuses().put(source, mod);
		needsRecompute = true;
	}
	
	public void modifyPercentAlways(String source, float value, String desc) {
		StatMod mod = new StatMod(source, StatModType.PERCENT, value, desc);
		getPercentBonuses().put(source, mod);
		needsRecompute = true;
	}
	
	public void modifyMult(String source, float value) {
		modifyMult(source, value, null);
	}
	public void modifyMult(String source, float value, String desc) {
		StatMod mod = getMultBonuses().get(source);
		if (mod == null && value == 1) return;
		if (mod != null && mod.value == value) return;
		
		mod = new StatMod(source, StatModType.MULT, value, desc);
		getMultBonuses().put(source, mod);
		needsRecompute = true;
	}
	
	public void modifyMultAlways(String source, float value, String desc) {
		StatMod mod = new StatMod(source, StatModType.MULT, value, desc);
		getMultBonuses().put(source, mod);
		needsRecompute = true;
	}
	
	public void modifyFlatAlways(String source, float value, String desc) {
		StatMod mod = new StatMod(source, StatModType.FLAT, value, desc);
		getFlatBonuses().put(source, mod);
		needsRecompute = true;
	}
	
	public void unmodify() {
		if (flatBonuses != null) flatBonuses.clear();
		if (percentBonuses != null) percentBonuses.clear();
		if (multBonuses != null) multBonuses.clear();
		needsRecompute = true;
	}
	
	public void unmodify(String source) {
		if (flatBonuses != null) {
			StatMod mod = flatBonuses.remove(source);
			if (mod != null && mod.value != 0) needsRecompute = true; 
		}
		if (percentBonuses != null) {
			StatMod mod = percentBonuses.remove(source);
			if (mod != null && mod.value != 0) needsRecompute = true;
		}
		
		if (multBonuses != null) {
			StatMod mod = multBonuses.remove(source);
			if (mod != null && mod.value != 1) needsRecompute = true;
		}
	}
	
	public void unmodifyFlat(String source) {
		if (flatBonuses == null) return;
		StatMod mod = flatBonuses.remove(source);
		if (mod != null && mod.value != 0) needsRecompute = true;
	}
	
	public void unmodifyPercent(String source) {
		if (percentBonuses == null) return;
		StatMod mod = percentBonuses.remove(source);
		if (mod != null && mod.value != 0) needsRecompute = true;
	}
	
	public void unmodifyMult(String source) {
		if (multBonuses == null) return;
		StatMod mod = multBonuses.remove(source);
		if (mod != null && mod.value != 1) needsRecompute = true;
	}	
	
	private void recompute() {
		float flatMod = 0;
		float multBonus = 1f;
		percentMod = 0;
		if (percentBonuses != null) {
			for (StatMod mod : percentBonuses.values()) {
				percentMod += mod.value;
			}
		}
		
		if (flatBonuses != null) {
			for (StatMod mod : flatBonuses.values()) {
				flatMod += mod.value;
			}
		}
		
		if (multBonuses != null) {
			for (StatMod mod : multBonuses.values()) {
				multBonus *= mod.value;
			}
		}
		
		//mult = 1f + percentMod / 100f;
		//if (mult < 0) mult = 0;
		mult = multBonus;
		flatBonus = flatMod;
		
		needsRecompute = false;
	}
	
	public float computeEffective(float baseValue) {
		if (needsRecompute) recompute();
		//return baseValue * mult + flatBonus;
		return (baseValue + baseValue * percentMod / 100f + flatBonus) * mult;
	}

	public float getFlatBonus() {
		if (needsRecompute) recompute();
		return flatBonus;
	}
	
	/**
	 * Returns combined percentage and multiplier modifiers.
	 * @return
	 */
	public float getBonusMult() {
		if (needsRecompute) recompute();
		return mult * (1f + percentMod / 100f);
	}
	
	public float getMult() {
		if (needsRecompute) recompute();
		return mult;
	}

	public float getPercentMod() {
		if (needsRecompute) recompute();
		return percentMod;
	}

	public boolean isPositive(float baseValue) {
		return computeEffective(baseValue) > baseValue;
	}
	
	public boolean isNegative(float baseValue) {
		return computeEffective(baseValue) < baseValue;
	}

	public HashMap<String, StatMod> getFlatBonuses() {
		if (flatBonuses == null) {
			flatBonuses = new LinkedHashMap<String, StatMod>();
		}
		return flatBonuses;
	}

	public HashMap<String, StatMod> getPercentBonuses() {
		if (percentBonuses == null) {
			percentBonuses = new LinkedHashMap<String, StatMod>();
		}
		return percentBonuses;
	}

	public HashMap<String, StatMod> getMultBonuses() {
		if (multBonuses == null) {
			multBonuses = new LinkedHashMap<String, StatMod>();
		}
		return multBonuses;
	}
	
	public void applyMods(MutableStat other) {
		getFlatBonuses().putAll(other.getFlatMods());
		getPercentBonuses().putAll(other.getPercentMods());
		getMultBonuses().putAll(other.getMultMods());
		needsRecompute = true;
	}
	
	public void applyMods(StatBonus other) {
		getFlatBonuses().putAll(other.getFlatBonuses());
		getPercentBonuses().putAll(other.getPercentBonuses());
		getMultBonuses().putAll(other.getMultBonuses());
		needsRecompute = true;
	}
}











