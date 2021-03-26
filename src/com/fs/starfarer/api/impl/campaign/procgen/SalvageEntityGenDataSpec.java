package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SalvageEntityGenDataSpec {
	
	public static enum DiscoverabilityType {
		DISCOVERABLE,
		NOT_DISCOVERABLE,
		ALWAYS_VISIBLE,
	}
	
	public static class DropData implements Cloneable {
		transient public String group;
		transient public int chances = -1;
		transient public int maxChances = -1;
		transient public int value = -1;
		transient public float valueMult = 1f;
		private WeightedRandomPicker<DropGroupRow> custom = null;
		
		private String j = null;
//		public DropData(String group) {
//			this.group = group;
//		}
		Object readResolve() {
			if (j != null) {
				try {
					JSONObject json = new JSONObject(j);
					if (json.has("g")) group = json.getString("g");
					if (json.has("c")) {
						chances = json.getInt("c");
					} else {
						chances = -1;
					}
					if (json.has("mC")) {
						maxChances = json.getInt("mC");
					} else {
						maxChances = -1;
					}
					if (json.has("v")) {
						value = json.getInt("v");
					} else {
						value = -1;
					}
					if (json.has("vM")) {
						valueMult = (float) json.getDouble("vM");
					} else {
						valueMult = 1f;
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			return this;
		}
		
		Object writeReplace() {
			try {
				JSONObject json = new JSONObject();
				if (group != null) json.put("g", group);
				if (chances > 0) json.put("c", chances);
				if (maxChances > 0) json.put("mC", maxChances);
				if (value > 0) json.put("v", value);
				if (valueMult != 1) json.put("vM", valueMult);
				j = json.toString();
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			return this;
		}
		
		
		public void addWeapon(String id, float weight) {
			addCustom(DropGroupRow.WEAPON_PREFIX + id, weight);
		}
		public void addHullMod(String id, float weight) {
			//addCustom(DropGroupRow.MOD_PREFIX + id, weight);
			addCustom("item_modspec:" + id, weight);
		}
		public void addFighterChip(String id, float weight) {
			addCustom(DropGroupRow.FIGHTER_PREFIX + id, weight);
		}
		public void addNothing(float weight) {
			addCustom(DropGroupRow.NOTHING, weight);
		}
		public void addCommodity(String id, float weight) {
			addCustom(id, weight);
		}
		public void addSpecialItem(String data, float weight) {
			addCustom(DropGroupRow.ITEM_PREFIX + data, weight);
		}
		public void addCustom(String data, float weight) {
			initCustom();
			for (int i = 0; i < custom.getItems().size(); i++) {
				DropGroupRow row = custom.getItems().get(i);
				if (row.getCommodity() != null && row.getCommodity().equals(data)) {
					float w = custom.getWeight(i);
					w += weight;
					custom.setWeight(i, w);
					return;
				}
			}
			DropGroupRow row = new DropGroupRow(data, "custom", weight);
			custom.add(row, weight);
		}
		
		public void addRandomWeapons(int tier, float weight) {
			addCustom(DropGroupRow.WEAPON_PREFIX, tier, weight, (String []) null);
		}
		public void addRandomWeapons(int tier, float weight, String ... tags) {
			addCustom(DropGroupRow.WEAPON_PREFIX, tier, weight, tags);
		}
		
		public void addRandomHullmods(int tier, float weight) {
			//addCustom(DropGroupRow.MOD_PREFIX, tier, weight, (String []) null);
			addCustom("item_modspec:", tier, weight, (String []) null);
		}
		public void addRandomHullmods(int tier, float weight, String ... tags) {
			//addCustom(DropGroupRow.MOD_PREFIX, tier, weight, tags);
			addCustom("item_modspec:", tier, weight, tags);
		}
		
		public void addRandomFighters(int tier, float weight) {
			addCustom(DropGroupRow.FIGHTER_PREFIX, tier, weight, (String []) null);
		}
		public void addRandomFighters(int tier, float weight, String ... tags) {
			addCustom(DropGroupRow.FIGHTER_PREFIX, tier, weight, tags);
		}
		
		private void addCustom(String prefix, int tier, float weight, String ... tags) {
			initCustom();
			
			String data = prefix;
			data += "{";
			if (tier >= 0) {
				data += "tier:" + tier + ",";
			}
			if (tags != null && tags.length > 0) {
				data += "tags:[";
				for (String tag : tags) {
					data += tag + ",";
				}
				data += "]";
			}
			data += "}";
			DropGroupRow row = new DropGroupRow(data, "custom", weight);
			custom.add(row, weight);
		}
		
		public void initCustom() {
			if (custom == null) {
				custom = new WeightedRandomPicker<DropGroupRow>();
			}
		}
		
		public void clearCustom() {
			custom = null;
		}
		
		public WeightedRandomPicker<DropGroupRow> getCustom() {
			return custom;
		}

		@Override
		public DropData clone() {
			try {
				DropData copy = (DropData) super.clone();
				
				if (custom != null) {
					copy.custom = new WeightedRandomPicker<DropGroupRow>();
					for (int i = 0; i < custom.getItems().size(); i++) {
						copy.custom.add(custom.getItems().get(i), custom.getWeight(i));
					}
				}
				return copy;
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}

//	id,rating,detection_range,xpGain,drop_value,drop_random
//	derelict_probe,0,2000,500,"basic:2000, extended:500",ai_cores1:1
//	probDefenders	minStr	maxStr	maxSize
	// probStation	stationRole
	private String id;
	private String nameOverride;
	private float salvageRating, detectionRange, xpDiscover, xpSalvage, radiusOverride = -1f;
	private DiscoverabilityType type;
	private float probDefenders, defQuality, minStr, maxStr, maxDefenderSize, minDefenderSize, probStation;
	private String stationRole;
	private String defFaction;
	
//	private Map<String, Float> dropValue = new HashMap<String, Float>();
//	private Map<String, Float> dropRandom = new HashMap<String, Float>();
	private List<DropData> dropValue = new ArrayList<DropData>();
	private List<DropData> dropRandom = new ArrayList<DropData>();

	private Set<String> tags = new HashSet<String>();
	
	public SalvageEntityGenDataSpec(JSONObject row) throws JSONException {
		id = row.getString("id");
		
		stationRole = row.getString("stationRole");
		
		nameOverride = row.optString("name");
		if (nameOverride != null && nameOverride.isEmpty()) nameOverride = null;
		
		salvageRating = (float) row.optDouble("rating", 0);
		detectionRange = (float) row.optDouble("detection_range", 0);
		
		float baseXP = 200 + 1 * (salvageRating * 20f);
		//baseXP /= 3f;
		baseXP = Misc.getRounded(baseXP);
//		if (id.equals("ruins_vast")) {
//			System.out.println("wefwefe");
//		}
		xpDiscover = (float) row.optDouble("xpDiscover", baseXP);
		xpSalvage = (float) row.optDouble("xpSalvage", baseXP * 3f);
		
		radiusOverride = (float) row.optDouble("radius", -1);
		
		defQuality = (float) row.optDouble("defQuality", 1f);
		
		probDefenders = (float) row.optDouble("probDefenders", 0);
		minStr = (float) row.optDouble("minStr", 0);
		maxStr = (float) row.optDouble("maxStr", 0);
		maxDefenderSize = (float) row.optDouble("maxSize", 4);
		minDefenderSize = (float) row.optDouble("minSize", 0);
		
		defFaction = row.optString("defFaction", null);
		if (defFaction != null && defFaction.isEmpty()) defFaction = null;
		
		probStation = (float) row.optDouble("probStation", 0);
		
		type = Misc.mapToEnum(row, "type", DiscoverabilityType.class, DiscoverabilityType.DISCOVERABLE);
		
		if (row.has("drop_value") && !row.getString("drop_value").isEmpty()) {
			//readToMap(row.getString("drop_value"), dropValue);
			String [] parts = row.getString("drop_value").split(",");
			for (String part : parts) {
				part = part.trim();
				String [] pair = part.split(":");
				String key = pair[0].trim();
				String value = pair[1].trim();
				
				DropData data = new DropData();
				data.group = key;
				data.value = Integer.valueOf(value);
				data.chances = -1;
				
				dropValue.add(data);
			}
		}
		
		if (row.has("drop_random") && !row.getString("drop_random").isEmpty()) {
			//readToMap(row.getString("drop_random"), dropRandom);
			String [] parts = row.getString("drop_random").split(",");
			for (String part : parts) {
				part = part.trim();
				String [] pair = part.split(":");
				String key = pair[0].trim();
				String value = pair[1].trim();
				
				DropData data = new DropData();
				data.group = key;
				
				if (value.contains("x")) {
					String [] chancesXValue = value.split("x");
					if (chancesXValue[0].trim().contains("-")) {
						data.chances = Integer.valueOf(chancesXValue[0].trim().split("-")[0]);
						data.maxChances = Integer.valueOf(chancesXValue[0].trim().split("-")[1]);
					} else {
						data.chances = Integer.valueOf(chancesXValue[0].trim());
					}
					data.value = Integer.valueOf(chancesXValue[1].trim()); 
				} else {
					data.value = -1;
					
					if (value.contains("-")) {
						data.chances = Integer.valueOf(value.trim().split("-")[0]);
						data.maxChances = Integer.valueOf(value.trim().split("-")[1]);
					} else {
						float c = Float.valueOf(value); 
						data.chances = (int) Math.max(c, 1);
						if (c < 1) data.valueMult = c;
					}
					//data.chances = Integer.valueOf(value);
				}
				
				dropRandom.add(data);
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
	}
	
	public String getDefFaction() {
		return defFaction;
	}

	public void setDefFaction(String defFaction) {
		this.defFaction = defFaction;
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
	
	public float getRadiusOverride() {
		return radiusOverride;
	}

	public void setRadiusOverride(float radiusOverride) {
		this.radiusOverride = radiusOverride;
	}

	public String getNameOverride() {
		return nameOverride;
	}

	public void setNameOverride(String nameOverride) {
		this.nameOverride = nameOverride;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getSalvageRating() {
		return salvageRating;
	}
	public void setSalvageRating(float salvageRating) {
		this.salvageRating = salvageRating;
	}
	public float getDetectionRange() {
		return detectionRange;
	}
	public void setDetectionRange(float detectionRange) {
		this.detectionRange = detectionRange;
	}
	public float getXpDiscover() {
		return xpDiscover;
	}
	public void setXpDiscover(float xpDiscover) {
		this.xpDiscover = xpDiscover;
	}
	public float getXpSalvage() {
		return xpSalvage;
	}
	public void setXpSalvage(float xpSalvage) {
		this.xpSalvage = xpSalvage;
	}


	public List<DropData> getDropValue() {
		return dropValue;
	}
	public List<DropData> getDropRandom() {
		return dropRandom;
	}
	public DiscoverabilityType getType() {
		return type;
	}
	public void setType(DiscoverabilityType type) {
		this.type = type;
	}
	public float getProbDefenders() {
		return probDefenders;
	}
	public void setProbDefenders(float probDefenders) {
		this.probDefenders = probDefenders;
	}
	public float getMinStr() {
		return minStr;
	}
	public void setMinStr(float minStr) {
		this.minStr = minStr;
	}
	public float getMaxStr() {
		return maxStr;
	}
	public void setMaxStr(float maxStr) {
		this.maxStr = maxStr;
	}
	public float getMaxDefenderSize() {
		return maxDefenderSize;
	}
	public void setMaxDefenderSize(float maxSize) {
		this.maxDefenderSize = maxSize;
	}
	
	public float getMinDefenderSize() {
		return minDefenderSize;
	}

	public void setMinDefenderSize(float minDefenderSize) {
		this.minDefenderSize = minDefenderSize;
	}

	public float getDefQuality() {
		return defQuality;
	}

	public void setDefQuality(float defQuality) {
		this.defQuality = defQuality;
	}

	public float getProbStation() {
		return probStation;
	}

	public void setProbStation(float probStation) {
		this.probStation = probStation;
	}

	public String getStationRole() {
		return stationRole;
	}

	public void setStationRole(String stationRole) {
		this.stationRole = stationRole;
	}


//	public static void readToMap(String input, Map<String, Float> map) {
//		String [] parts = input.split(",");
//		for (String part : parts) {
//			part = part.trim();
//			String [] pair = part.split(":");
//			String key = pair[0].trim();
//			float value = Float.valueOf(pair[1].trim());
//			map.put(key, value);
//		}
//	}
	
//	public static void verifySpecsExist(Class clazz, List<String> specs, String desc) {
//		for (String id : specs) {
//			Object spec = Global.getSettings().getSpec(clazz, id, true);
//			if (spec == null) {
//				throw new RuntimeException("Spec of class " + clazz.getName() +
//										   " with id [" + id + "] not found while verifying [" + 
//										   desc + "]");
//			}
//		}
//	}
	
	
}
