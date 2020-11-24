package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class PlanetGenDataSpec implements EntityGenDataSpec {

	
	//id	category	frequency	habOffsetMin	habOffsetMax	minRadius	maxRadius	minColor	maxColor
	private String id, category;
	private float frequency, habOffsetMin, habOffsetMax, minRadius, maxRadius;
	private float probOrbits, minOrbits, maxOrbits;
	private float habOffsetYOUNG, habOffsetAVERAGE, habOffsetOLD;
	private Color minColor, maxColor;
	
	private Map<String, Float> multipliers = new HashMap<String, Float>();
	private Set<String> tags = new HashSet<String>();
	
	public PlanetGenDataSpec(JSONObject row) throws JSONException {
		id = row.getString("id");
		category = row.getString("category");
		frequency = (float) row.getDouble("frequency");
		habOffsetMin = (float) row.optDouble("habOffsetMin", -1000);
		habOffsetMax = (float) row.optDouble("habOffsetMax", 1000);
		minRadius = (float) row.optDouble("minRadius", 0);
		maxRadius = (float) row.optDouble("maxRadius", 0);
		
		habOffsetYOUNG = (float) row.optDouble("habOffsetYOUNG", 0);
		habOffsetAVERAGE = (float) row.optDouble("habOffsetAVERAGE", 0);
		habOffsetOLD = (float) row.optDouble("habOffsetOLD", 0);
		
		probOrbits = (float) row.optDouble("probOrbits", 0);
		minOrbits = (float) row.optDouble("minOrbits", 0);
		maxOrbits = (float) row.optDouble("maxOrbits", 0);
		
		for (String key : JSONObject.getNames(row)) {
			float frequency = (float) row.optDouble(key, 1f);
			if (frequency != 1) {
				multipliers.put(key, frequency);
			}
		}
		
		String tags = row.optString("tags", null);
		if (tags != null) {
			String [] split = tags.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				addTag(tag);
			}
		}
		
		minColor = StarGenDataSpec.parseColor(row.optString("minColor", null) + " 255", " ");
		maxColor = StarGenDataSpec.parseColor(row.optString("maxColor", null) + " 255", " ");
	}
	
	public String toString() {
		return getId();
	}

	public String getCategory() {
		return category;
	}

	public float getFrequency() {
		return frequency;
	}
	
	public String getId() {
		return id;
	}

	public float getHabOffsetMin() {
		return habOffsetMin;
	}

	public float getHabOffsetMax() {
		return habOffsetMax;
	}

	public float getMinRadius() {
		return minRadius;
	}

	public float getMaxRadius() {
		return maxRadius;
	}

	public Color getMinColor() {
		return minColor;
	}

	public Color getMaxColor() {
		return maxColor;
	}

	public float getMultiplier(String key) {
		if (!multipliers.containsKey(key)) return 1f;
		return multipliers.get(key);
	}
	
	public Set<String> getTags() {
		return tags;
	}
	
	public void addTag(String tag) {
		tags.add(tag);
	}

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}

	public Map<String, Float> getMultipliers() {
		return multipliers;
	}

	public float getProbOrbits() {
		return probOrbits;
	}

	public float getMinOrbits() {
		return minOrbits;
	}

	public float getMaxOrbits() {
		return maxOrbits;
	}

	public float getHabOffsetYOUNG() {
		return habOffsetYOUNG;
	}

	public float getHabOffsetAVERAGE() {
		return habOffsetAVERAGE;
	}

	public float getHabOffsetOLD() {
		return habOffsetOLD;
	}
	
}
