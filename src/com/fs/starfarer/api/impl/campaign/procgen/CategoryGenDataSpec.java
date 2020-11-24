package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class CategoryGenDataSpec {
	private String category;
	private float frequency;
	
	private Map<String, Float> multipliers = new HashMap<String, Float>();
	
	public CategoryGenDataSpec(JSONObject row) throws JSONException {
		category = row.getString("category");
		frequency = (float) row.getDouble("frequency");
		
		for (String key : JSONObject.getNames(row)) {
			float frequency = (float) row.optDouble(key, 1f);
			if (frequency != 1) {
				multipliers.put(key, frequency);
			}
			
		}
	}

	public String toString() {
		return getCategory();
	}
	
	public String getCategory() {
		return category;
	}

	public float getFrequency() {
		return frequency;
	}
	
	public float getMultiplier(String key) {
		if (!multipliers.containsKey(key)) return 1f;
		return multipliers.get(key);
	}

	public Map<String, Float> getMultipliers() {
		return multipliers;
	}
	
	
}
