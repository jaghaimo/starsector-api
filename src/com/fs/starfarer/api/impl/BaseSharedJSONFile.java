package com.fs.starfarer.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

/**
 * Base class. Extending classes are for relatively small amounts of data meant to be shared across
 * different saves.
 * 
 * The amount of data stored here should be small and bounded (i.e. not growing indefinitely).
 * 
 * Code using this should assume the data stored here may be lost (e.g. due to the common folder being deleted)
 * and handle this/recover gracefully.
 * 
 * @author Alex
 *
 */
public abstract class BaseSharedJSONFile {
	protected abstract String getFilename();
	
	protected JSONObject json = new JSONObject();
	
	public void loadIfNeeded() {
		try {
			if (Global.getSettings().fileExistsInCommon(getFilename())) {
				json = Global.getSettings().readJSONFromCommon(getFilename(), true);
			}
		} catch (IOException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		} catch (JSONException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		}
	}
	
	public void saveIfNeeded() {
//		if (true) {
//			return;
//		}
		try {
			Global.getSettings().writeJSONToCommon(getFilename(), json, true);
		} catch (JSONException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		} catch (IOException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		}
	}
	
	protected Map<String, Set<String>> setCache = new HashMap<>();
	
	public Set<String> getSet(String key) {
		if (setCache.containsKey(key)) return setCache.get(key);
		
		JSONArray arr = json.optJSONArray(key);
		Set<String> set = new LinkedHashSet<>();
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				String curr = arr.optString(i);
				if (curr != null) {
					set.add(curr);
				}
			}
		}
		setCache.put(key, set);
		return set;
	}

	public boolean doesSetContain(String key, String value) {
		Set<String> set = getSet(key);
		return set.contains(value);
	}
	
	public boolean addToSet(String key, String value) {
		Set<String> set = getSet(key);
		if (set.contains(value)) {
			return false;
		}
		
		set.add(value);
		
		setCache.put(key, set);
		
		JSONArray arr = new JSONArray();
		List<String> list = new ArrayList<>();
		list.addAll(set);
		Collections.sort(list);
		
		for (String curr : list) {
			arr.put(curr);
		}
		
		try {
			json.put(key, arr);
		} catch (JSONException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		}
		
		return true;
	}
	
	public boolean optBoolean(String key, boolean defaultValue) {
		return json.optBoolean(key, defaultValue);
	}
	public void setBoolean(String key, boolean value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		}
	}

	public float optFloat(String key, float defaultValue) {
		return (float) json.optDouble(key, defaultValue);
	}
	public void setFloat(String key, float value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		}
	}
	
	public int optInt(String key, int defaultValue) {
		return json.optInt(key, defaultValue);
	}
	public void setInt(String key, int value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		}
	}
	
	public String optString(String key, String defaultValue) {
		return json.optString(key, defaultValue);
	}
	public void setString(String key, String value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(getClass()).warn(e.getMessage(), e);
		}
	}
	
	public void unset(String key) {
		json.remove(key);
	}
}







