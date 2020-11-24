package com.fs.starfarer.api.impl.campaign.population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.util.Misc;

public class PopulationComposition {
	
	private LinkedHashMap<String, Float> comp = new LinkedHashMap<String, Float>();
	//private float weight;
	private MutableStat weight = new MutableStat(0f);
//	private float leaving;
	
	Object readResolve() {
		if (weight == null ){
			weight = new MutableStat(0f);
		}
		return this;
	}
	
	public LinkedHashMap<String, Float> getComp() {
		return comp;
	}

	public float get(String id) {
		Float val = comp.get(id);
		if (val == null) return 0f;
		return val;
	}
	
	public void set(String id, float value) {
		comp.put(id, value);
	}
	
	public void add(String id, float value) {
		set(id, get(id) + value);
	}

	
	@Override
	public String toString() {
		String str = "";
		if (!weight.getFlatMods().containsKey(SET_WEIGHT_ID)) {
			for (StatMod mod : weight.getFlatMods().values()) {
				if (mod.value > 0) {
					str += "<b>+" + (int)mod.getValue() + "</b> " + mod.getDesc() + "\n";
				} else {
					str += "<b>" + (int) mod.getValue() + "</b> " + mod.getDesc() + "\n";
				}
			}
			str += "\n";
		}
		List<String> keys = new ArrayList<String>(comp.keySet());
		Collections.sort(keys, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return (int) Math.signum(get(o2) - get(o1));
			}
		});
		
		
		for (String key : keys) {
			//float val = Math.round(get(key));
			float val = Misc.getRoundedValueFloat(get(key));
			str += key + ": " + val + "\n";
		}
		
		return str;
	}

	public float getWeightValue() {
		return weight.getModifiedValue();
	}
	
	public float getPositiveWeight() {
		float total = 0;
		for (StatMod mod : weight.getFlatMods().values()) {
			if (mod.value > 0) {
				total += mod.value;
			}
		}
		return total;
	}
	
	public float getNegativeWeight() {
		float total = 0;
		for (StatMod mod : weight.getFlatMods().values()) {
			if (mod.value < 0) {
				total -= mod.value;
			}
		}
		return total;
	}
	
//	public void addWeight(float weight) {
//		this.weight += weight;
//	}
	
	public void setWeight(float weight) {
		this.weight.modifyFlat(SET_WEIGHT_ID, weight);
	}
	
	public static final String SET_WEIGHT_ID = "core_set";
	public void updateWeight() {
		//Global.getSettings().profilerBegin("updateWeight()");
		float total = 0;
		for (Float f : comp.values()) {
			total += f;
		}
		weight.modifyFlat(SET_WEIGHT_ID, total);
		//Global.getSettings().profilerEnd();
	}

	public MutableStat getWeight() {
		return weight;
	}

	public void normalize() {
		float w = weight.getModifiedValue();
		normalizeToWeight(w);
	}
	
	public void normalizeToPositive() {
		normalizeToWeight(getPositiveWeight());
	}
	
	public void normalizeToWeight(float w) {
		float total = 0f;
		for (Float f : comp.values()) {
			total += f;
		}
		
		if (w <= 0 || total <= 0) {
			for (String id : comp.keySet()) {
				set(id, 0);
			}
			return;
		}
		
		for (String id : comp.keySet()) {
			float f = get(id);
			set(id, f * w / total);
		}
	}
	
//	public float getLeaving() {
//		return leaving;
//	}
//
//	public void setLeaving(float negative) {
//		this.leaving = negative;
//	}
	
	
}









