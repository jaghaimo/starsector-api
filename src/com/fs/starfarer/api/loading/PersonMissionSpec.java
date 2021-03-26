package com.fs.starfarer.api.loading;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission;
import com.fs.starfarer.api.util.Misc;

public class PersonMissionSpec {
	
	//mission id	person id	tagsAny	tagsAll	tagsNotAny
	//freq	min timeout	max timeout	minRep	maxRep	importance	plugin
	
	protected String missionId;
	protected String personId;
	
	protected Set<String> tags = new HashSet<String>();
	protected Set<String> tagsAny = new HashSet<String>();
	protected Set<String> tagsAll = new HashSet<String>();
	protected Set<String> tagsNotAny = new HashSet<String>();
	
	protected Set<String> reqMissionAny = new HashSet<String>();
	protected Set<String> reqMissionAll = new HashSet<String>();
	protected Set<String> reqMissionNone = new HashSet<String>();
	
	protected RepLevel min;
	protected RepLevel max;
	
	protected float freq;
	protected float minTimeout;
	protected float maxTimeout;
	
	//protected float importance;
	protected PersonImportance importance;
	
	protected String pluginClass;
	protected String icon;
	
	
	public PersonMissionSpec(JSONObject row) throws JSONException {
		
		missionId = row.getString("mission id");
		personId = row.optString("person id", null);
		if (personId != null && personId.isEmpty()) personId = null;
		
//		min = Misc.mapToEnum(row, "minRep", RepLevel.class, null, false);
//		max = Misc.mapToEnum(row, "maxRep", RepLevel.class, null, false);
		min = Misc.mapToEnum(row, "min rep", RepLevel.class, RepLevel.VENGEFUL);
		max = Misc.mapToEnum(row, "max rep", RepLevel.class, RepLevel.COOPERATIVE);
		
		freq = (float)row.optDouble("freq", 10f);
		minTimeout = (float)row.optDouble("min timeout", 10f);
		maxTimeout = (float)row.optDouble("max timeout", 10f);
		//importance = (float)row.optDouble("importance", 0f);
		importance = Misc.mapToEnum(row, "importance", PersonImportance.class, PersonImportance.VERY_LOW);
		
		pluginClass = row.getString("plugin");
		
		icon = row.optString("icon");
		if (icon == null || icon.isEmpty()) {
			icon = null;
		}
		
		String requiresAllStr = row.optString("tagsAll", null);
		if (requiresAllStr != null) {
			String [] split = requiresAllStr.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				tagsAll.add(tag);
			}
		}
		
		String requiresAnyStr = row.optString("tagsAny", null);
		if (requiresAnyStr != null) {
			String [] split = requiresAnyStr.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				tagsAny.add(tag);
			}
		}
		
		String requiresNotAny = row.optString("tagsNotAny", null);
		if (requiresNotAny != null) {
			String [] split = requiresNotAny.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				tagsNotAny.add(tag);
			}
		}
		
		String tagsStr = row.optString("tags", null);
		if (tagsStr != null) {
			String [] split = tagsStr.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				tags.add(tag);
			}
		}
		
		//reqMissionAny	reqMissionAll	reqMissionNone
		String reqs = row.optString("reqMissionAny", null);
		if (reqs != null) {
			String [] split = reqs.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				reqMissionAny.add(tag);
			}
		}
		
		reqs = row.optString("reqMissionAll", null);
		if (reqs != null) {
			String [] split = reqs.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				reqMissionAll.add(tag);
			}
		}
		
		reqs = row.optString("reqMissionNone", null);
		if (reqs != null) {
			String [] split = reqs.split(",");
			for (String tag : split) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				reqMissionNone.add(tag);
			}
		}
	}
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Set<String> getReqMissionAny() {
		return reqMissionAny;
	}

	public Set<String> getReqMissionAll() {
		return reqMissionAll;
	}

	public Set<String> getReqMissionNone() {
		return reqMissionNone;
	}



	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public RepLevel getMinRep() {
		return min;
	}

	public void setMinRep(RepLevel min) {
		this.min = min;
	}

	public RepLevel getMaxRep() {
		return max;
	}

	public void setMaxRep(RepLevel max) {
		this.max = max;
	}

	public float getFreq() {
		return freq;
	}

	public void setFreq(float freq) {
		this.freq = freq;
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

	public PersonImportance getImportance() {
		return importance;
	}

	public void setImportance(PersonImportance importance) {
		this.importance = importance;
	}

	public String getPluginClass() {
		return pluginClass;
	}

	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}
	
	public Set<String> getTagsAny() {
		return tagsAny;
	}

	public Set<String> getTagsAll() {
		return tagsAll;
	}

	public Set<String> getTagsNotAny() {
		return tagsNotAny;
	}

	public HubMission createMission() {
		HubMission mission = (HubMission) Global.getSettings().getInstanceOfScript(pluginClass);
		mission.setMissionId(missionId);
		if (icon != null && mission instanceof BaseHubMission) {
			BaseHubMission bhm = (BaseHubMission) mission;
			bhm.setIconName(icon);
		}
		return mission;
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
	
	
	public boolean tagsMatch(Set<String> tags) {
		
		boolean foundAll = true;
		for (String tag : getTagsAll()) {
			if (!tags.contains(tag)) {
				foundAll = false;
				break;
			}
		}
		if (!foundAll && !getTagsAll().isEmpty()) return false;
		

		boolean foundOne = false;
		for (String tag : getTagsAny()) {
			if (tags.contains(tag)) {
				foundOne = true;
				break;
			}
		}
		if (!foundOne && !getTagsAny().isEmpty()) return false;
		
		
		foundOne = false;
		for (String tag : getTagsNotAny()) {
			if (tags.contains(tag)) {
				foundOne = true;
				break;
			}
		}
		if (foundOne) return false;
		
		return true;
	}
	
	public boolean completedMissionsMatch(Set<String> completed) {
		
		boolean foundAll = true;
		for (String id : getReqMissionAll()) {
			if (!completed.contains(id)) {
				foundAll = false;
				break;
			}
		}
		if (!foundAll && !getReqMissionAll().isEmpty()) return false;
		

		boolean foundOne = false;
		for (String id : getReqMissionAny()) {
			if (completed.contains(id)) {
				foundOne = true;
				break;
			}
		}
		if (!foundOne && !getReqMissionAny().isEmpty()) return false;
		
		
		foundOne = false;
		for (String tag : getReqMissionNone()) {
			if (completed.contains(tag)) {
				foundOne = true;
				break;
			}
		}
		if (foundOne) return false;
		
		return true;
	}
}






