package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class NameGenData {
	
	public static final String TAG_PLANET = "planet";
	public static final String TAG_MOON = "moon";
	public static final String TAG_STAR = "star";
	public static final String TAG_ASTEROID_BELT = "asteroid_belt";
	public static final String TAG_ASTEROID_FIELD = "asteroid_field";
	public static final String TAG_MAGNETIC_FIELD = "magnetic_field";
	public static final String TAG_RING = "ring";
	public static final String TAG_ACCRETION = "accretion";
	public static final String TAG_NEBULA = "nebula";
	public static final String TAG_CONSTELLATION = "constellation";
	
	//	name	secondary	tags	parents
	
	private String name, secondary;
	private float frequency;
	private boolean reusable;
	private Set<String> tags = new HashSet<String>();
	private Set<String> parents = new HashSet<String>();
	
	public NameGenData(String name, String secondary) {
		this.name = name;
		this.secondary = secondary;
		this.reusable = false;
		this.frequency = 1;
	}
	
	public NameGenData(JSONObject row) throws JSONException {
		name = row.getString("name");
		secondary = row.optString("secondary");
		
		if (name != null) name = name.trim();
		if (secondary != null) secondary = secondary.trim();
		
		frequency = (float) row.optDouble("frequency", 1);
		reusable = row.optBoolean("reusable", false);
		
		if (secondary != null && secondary.isEmpty()) secondary = null;
		
		String tags = row.optString("tags", null);
		if (tags != null) {
			String [] split = tags.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				addTag(tag);
			}
		}
		
		String parents = row.optString("parents", null);
		if (parents != null) {
			String [] split = parents.split(",");
			for (String parent : split) {
				parent = parent.trim();
				if (parent.isEmpty()) continue;
				addParent(parent);
			}
		}
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
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
	
	public void setParents(Set<String> parents) {
		this.parents = parents;
	}
	
	public Set<String> getParents() {
		return parents;
	}
	
	public void addParent(String parent) {
		parents.add(parent);
	}
	
	public boolean hasParent(String parent) {
		return parents.contains(parent);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSecondary() {
		return secondary;
	}

	public void setSecondary(String secondary) {
		this.secondary = secondary;
	}

	public float getFrequency() {
		return frequency;
	}

	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}

	public boolean isReusable() {
		return reusable;
	}

	public void setReusable(boolean reusable) {
		this.reusable = reusable;
	}
	
	

}
