package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class ConditionGenDataSpec {
	
	public static String NO_PICK_SUFFIX = "_no_pick";
	
	//id	group	rank	order	hazard reqSurvey xpMult requiresAll	requiresAny	requiresNotAny	
	//cat_barren	rocky_unstable	rocky_ice	barren_desert	lava	lava_minor	cat_frozen
	//cryovolcanic	irradiated	cat_toxic	toxic_cold	terran	terran-eccentric	jungle	water	arid	tundra	desert
	//multipliers >>>	tectonic_activity	extreme_tectonic_activity	low_gravity	high_gravity	very_hot
	private String id, group;
	private float rank, order, hazard, xpMult;
	private boolean requiresSurvey = false;
	
	private Map<String, Float> multipliers = new HashMap<String, Float>();
	private Set<String> requiresAll = new HashSet<String>();
	private Set<String> requiresAny = new HashSet<String>();
	private Set<String> requiresNotAny = new HashSet<String>();
	
	public ConditionGenDataSpec(JSONObject row) throws JSONException {
		id = row.getString("id");
		group = row.getString("group");
		rank = (float) row.optDouble("rank", 0);
		order = (float) row.optDouble("order", 0);
		hazard = (float) row.optDouble("hazard", 0);
		xpMult = (float) row.optDouble("xpMult", 0);
		
		requiresSurvey = row.optBoolean("reqSurvey", false);
		
		
//		for (Object o : Global.getSettings().getAllSpecs(CategoryGenDataSpec.class)) {
//			ConditionGenDataSpec spec = (ConditionGenDataSpec) o;
//			if (spec.getId().equals("ore_no_pick")) {
//				spec.getMultipliers().put("<your planet id>", 0f);
//			}
//			if (spec.getId().equals("ore_sparse")) {
//				spec.getMultipliers().put("<your planet id>", 4f);
//			}
//			if (spec.getId().equals("ore_moderate")) {
//				spec.getMultipliers().put("<your planet id>", 10f);
//			}
//			if (spec.getId().equals("ore_abundant")) {
//				spec.getMultipliers().put("<your planet id>", 5f);
//			}
//			if (spec.getId().equals("ore_rich")) {
//				spec.getMultipliers().put("<your planet id>", 2f);
//			}
//			if (spec.getId().equals("ore_ultrarich")) {
//				spec.getMultipliers().put("<your planet id>", 1f);
//			}
//		}
		
		for (String key : JSONObject.getNames(row)) {
			float mult = (float) row.optDouble(key, 1f);
			//if (mult != 1) {
			if (row.has(key) && !row.getString(key).isEmpty()) {
				multipliers.put(key, mult);
			}
		}
		
		String requiresAllStr = row.optString("requiresAll", null);
		if (requiresAllStr != null) {
			String [] split = requiresAllStr.split(",");
			for (String condition : split) {
				condition = condition.trim();
				if (condition.isEmpty()) continue;
				addRequiresAll(condition);
			}
		}
		
		String requiresAnyStr = row.optString("requiresAny", null);
		if (requiresAnyStr != null) {
			String [] split = requiresAnyStr.split(",");
			for (String condition : split) {
				condition = condition.trim();
				if (condition.isEmpty()) continue;
				addRequiresAny(condition);
			}
		}
		
		String requiresNotAny = row.optString("requiresNotAny", null);
		if (requiresNotAny != null) {
			String [] split = requiresNotAny.split(",");
			for (String condition : split) {
				condition = condition.trim();
				if (condition.isEmpty()) continue;
				addRequiresNotAny(condition);
			}
		}
	}
	

	public float getXpMult() {
		return xpMult;
	}

	public void setXpMult(float xpMult) {
		this.xpMult = xpMult;
	}


	public float getMultiplier(String key) {
		if (!multipliers.containsKey(key)) return 1f;
		return multipliers.get(key);
	}
	
	public boolean hasMultiplier(String key) {
		return multipliers.containsKey(key);
	}
	
	public Set<String> getRequiresAll() {
		return requiresAll;
	}
	
	public void addRequiresAll(String condition) {
		requiresAll.add(condition);
	}

	public boolean requiresAllContains(String condition) {
		return requiresAll.contains(condition);
	}
	
	public Set<String> getRequiresAny() {
		return requiresAny;
	}
	
	public void addRequiresAny(String condition) {
		requiresAny.add(condition);
	}
	
	public boolean requiresAnyContains(String condition) {
		return requiresAny.contains(condition);
	}
	public Set<String> getRequiresNotAny() {
		return requiresNotAny;
	}
	
	public void addRequiresNotAny(String condition) {
		requiresNotAny.add(condition);
	}
	
	public boolean requiresNotAnyContains(String condition) {
		return requiresNotAny.contains(condition);
	}

	public Map<String, Float> getMultipliers() {
		return multipliers;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getGroup() {
		return group;
	}


	public void setGroup(String group) {
		this.group = group;
	}


	public float getRank() {
		return rank;
	}

	public void setRank(float rank) {
		this.rank = rank;
	}


	public float getOrder() {
		return order;
	}

	public void setOrder(float order) {
		this.order = order;
	}


	public boolean isRequiresSurvey() {
		return requiresSurvey;
	}

	public void setRequiresSurvey(boolean requiresSurvey) {
		this.requiresSurvey = requiresSurvey;
	}

	public float getHazard() {
		return hazard;
	}

	public void setHazard(float hazard) {
		this.hazard = hazard;
	}
	
}
