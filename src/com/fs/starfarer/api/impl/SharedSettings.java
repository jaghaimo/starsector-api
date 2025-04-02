package com.fs.starfarer.api.impl;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

/**
 * Generally meant for UI settings that are shared across multiple campaigns.
 * 
 * "Was this checkbox checked last time this dialog was closed", etc. In some cases that should be tracked
 * per-campaign, but in some cases it makes sense for this to be shared.
 * 
 * The amount of data stored here should be small and bounded (i.e. not growing indefinitely).
 * 
 * Code using this should assume the data stored here may be lost (e.g. due to the common folder being deleted)
 * and handle this/recover gracefully.
 * 
 * @author Alex
 *
 */
public class SharedSettings {
	public static String SETTINGS_DATA_FILE = "core_shared_settings.json";
	
	protected static JSONObject json = new JSONObject();
	
	static {
		loadIfNeeded();
		
		// not needed if saveIfNeeded() is called after making changes
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			public void run() {
//				// remotely possible for this to fail (if it takes >15s, somehow?)
//				// in which case it'll just start fresh on next load
//				saveIfNeeded();
//			}
//		});
	}
	
	public static void loadIfNeeded() {
		try {
			if (Global.getSettings().fileExistsInCommon(SETTINGS_DATA_FILE)) {
				json = Global.getSettings().readJSONFromCommon(SETTINGS_DATA_FILE, true);
			}
		} catch (IOException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		} catch (JSONException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		}
	}
	
	public static void saveIfNeeded() {
		try {
			Global.getSettings().writeJSONToCommon(SETTINGS_DATA_FILE, json, true);
		} catch (JSONException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		} catch (IOException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		}
	}

	public static JSONObject get() {
		return json;
	}
	
	
	public static boolean optBoolean(String key, boolean defaultValue) {
		return json.optBoolean(key, defaultValue);
	}
	public static void setBoolean(String key, boolean value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		}
	}

	public static float optFloat(String key, float defaultValue) {
		return (float) json.optDouble(key, defaultValue);
	}
	public static void setFloat(String key, float value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		}
	}
	
	public static int optInt(String key, int defaultValue) {
		return json.optInt(key, defaultValue);
	}
	public static void setInt(String key, int value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		}
	}
	
	public static String optString(String key, String defaultValue) {
		return json.optString(key, defaultValue);
	}
	public static void setString(String key, String value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			Global.getLogger(SharedSettings.class).warn(e.getMessage(), e);
		}
	}
	
	public static void unset(String key) {
		json.remove(key);
	}
}







