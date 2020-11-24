package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectiveGenDataSpec {

	private String id;
	private String category;
	private float frequency;
	private Set<String> tags = new HashSet<String>();
	
	public ObjectiveGenDataSpec(JSONObject row) throws JSONException {
		id = row.getString("id");
		category = row.getString("category");
		frequency = (float) row.optDouble("frequency", 0);
		
		String tags = row.optString("tags", null);
		if (tags != null) {
			String [] split = tags.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				addTag(tag);
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public float getFrequency() {
		return frequency;
	}

	public void setFrequency(float frequency) {
		this.frequency = frequency;
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
}
