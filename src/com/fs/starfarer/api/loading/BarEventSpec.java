package com.fs.starfarer.api.loading;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;

public class BarEventSpec {
	
	//bar event id,tags,freq,prob,
	//min dur,max dur,min timeout,max timeout,min accepted timeout,max accepted timeout,plugin
	
	protected String id;
	protected Set<String> tags = new HashSet<String>();
	
	protected float freq;
	protected float prob;
	protected float minDur;
	protected float maxDur;
	protected float minTimeout;
	protected float maxTimeout;
	protected float minAcceptedTimeout;
	protected float maxAcceptedTimeout;
	
	protected String pluginClass;
	
	
	public BarEventSpec(JSONObject row) throws JSONException {
		
		id = row.getString("bar event id");
		
		freq = (float)row.optDouble("freq", 10f);
		prob = (float)row.optDouble("prob", 1f);
		
		minDur = (float)row.optDouble("min dur", 30f);
		maxDur = (float)row.optDouble("max dur", 40f);
		minTimeout = (float)row.optDouble("min timeout", 30f);
		maxTimeout = (float)row.optDouble("max timeout", 40f);
		minAcceptedTimeout = (float)row.optDouble("min accepted timeout", 60f);
		maxAcceptedTimeout = (float)row.optDouble("max accepted timeout", 90f);
		
		pluginClass = row.getString("plugin");
		
		String tagsStr = row.optString("tags", null);
		if (tagsStr != null) {
			String [] split = tagsStr.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				tags.add(tag);
			}
		}
	}

	
	public boolean isMission() {
		return Global.getSettings().getInstanceOfScript(pluginClass) instanceof HubMissionWithBarEvent;
	}
	
	public HubMissionWithBarEvent createMission() {
		HubMissionWithBarEvent mission = (HubMissionWithBarEvent) Global.getSettings().getInstanceOfScript(pluginClass);
		PersonMissionSpec spec = Global.getSettings().getMissionSpec(id);
		if (spec != null && spec.getIcon() != null) {
			mission.setIconName(spec.getIcon());
		}
		mission.setMissionId(id);
		return mission;
	}
	
	public PortsideBarEvent createEvent() {
		PortsideBarEvent event = (PortsideBarEvent) Global.getSettings().getInstanceOfScript(pluginClass);
		return event;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public float getFreq() {
		return freq;
	}

	public void setFreq(float freq) {
		this.freq = freq;
	}

	public float getProb() {
		return prob;
	}

	public void setProb(float prob) {
		this.prob = prob;
	}

	public float getMinDur() {
		return minDur;
	}

	public void setMinDur(float minDur) {
		this.minDur = minDur;
	}

	public float getMaxDur() {
		return maxDur;
	}

	public void setMaxDur(float maxDur) {
		this.maxDur = maxDur;
	}

	public float getMinTimeout() {
		return minTimeout;
	}

	public void setMinTimeout(float minTimeout) {
		this.minTimeout = minTimeout;
	}

	public float getMaxTimeout() {
		return maxTimeout;
	}

	public void setMaxTimeout(float maxTimeout) {
		this.maxTimeout = maxTimeout;
	}

	public float getMinAcceptedTimeout() {
		return minAcceptedTimeout;
	}

	public void setMinAcceptedTimeout(float minAcceptedTimeout) {
		this.minAcceptedTimeout = minAcceptedTimeout;
	}

	public float getMaxAcceptedTimeout() {
		return maxAcceptedTimeout;
	}

	public void setMaxAcceptedTimeout(float maxAcceptedTimeout) {
		this.maxAcceptedTimeout = maxAcceptedTimeout;
	}

	public String getPluginClass() {
		return pluginClass;
	}

	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}
	
	
}






