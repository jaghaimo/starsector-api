package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationGenDataSpec {

	//id	tags	freqYOUNG	freqAVERAGE	freqOLD
	
	private String id;
	private float freqYOUNG, freqAVERAGE, freqOLD;
	private Set<String> tags = new HashSet<String>();
	
	public LocationGenDataSpec(JSONObject row) throws JSONException {
		id = row.getString("id");
		
		freqYOUNG = (float) row.optDouble("freqYOUNG", 0);
		freqAVERAGE = (float) row.optDouble("freqAVERAGE", 0);
		freqOLD = (float) row.optDouble("freqOLD", 0);
		
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

	public float getFreqYOUNG() {
		return freqYOUNG;
	}

	public void setFreqYOUNG(float freqYOUNG) {
		this.freqYOUNG = freqYOUNG;
	}

	public float getFreqAVERAGE() {
		return freqAVERAGE;
	}

	public void setFreqAVERAGE(float freqAVERAGE) {
		this.freqAVERAGE = freqAVERAGE;
	}

	public float getFreqOLD() {
		return freqOLD;
	}

	public void setFreqOLD(float freqOLD) {
		this.freqOLD = freqOLD;
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
