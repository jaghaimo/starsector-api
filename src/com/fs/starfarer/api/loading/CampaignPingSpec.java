package com.fs.starfarer.api.loading;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CampaignPingSpec {
//		"danger":{
//			"sounds":["ui_new_radar_icon", "", ""],
//			"color":"textEnemyColor",
//			"range":100,
//			"duration":1,
//			"invert":false,
//			
//			"num":3,
//			"delay":0.5,	
//		},
	
	
	private String id;
	private List<String> sounds = new ArrayList<String>();
	private Color color;
	private float minRange, range, duration, delay = 1f, width, alphaMult = 1f, inFraction = 0.1f;
	private boolean invert;
	private boolean useFactionColor;
	private int num = 1;
	
	
	
	public CampaignPingSpec() {
		super();
	}

	public CampaignPingSpec(String id, Color color, JSONObject json) throws JSONException {
		this.id = id;
		
		this.color = color;
		
		sounds = new ArrayList<String>();
		JSONArray arr = json.optJSONArray("sounds");
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				sounds.add(arr.getString(i));
			}
		}
	
		inFraction = (float) json.optDouble("inFraction", 0.1f);
		minRange = (float) json.optDouble("minRange", 0f);
		range = (float) json.getDouble("range");
		width = (float) json.getDouble("width");
		duration = (float) json.getDouble("duration");
		delay = (float) json.optDouble("delay", 1f);
		alphaMult = (float) json.optDouble("alphaMult", 1f);
		num = json.optInt("num", 1);
		
		invert = json.optBoolean("invert", false);
		useFactionColor = json.optBoolean("useFactionColor", false);
	}
	
	public float getAlphaMult() {
		return alphaMult;
	}

	public float getInFraction() {
		return inFraction;
	}

	public boolean isUseFactionColor() {
		return useFactionColor;
	}

	public String getId() {
		return id;
	}

	public List<String> getSounds() {
		return sounds;
	}

	public Color getColor() {
		return color;
	}

	public float getRange() {
		return range;
	}

	public float getDuration() {
		return duration;
	}

	public float getDelay() {
		return delay;
	}

	public boolean isInvert() {
		return invert;
	}

	public int getNum() {
		return num;
	}

	public float getWidth() {
		return width;
	}

	public float getMinRange() {
		return minRange;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSounds(List<String> sounds) {
		this.sounds = sounds;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setMinRange(float minRange) {
		this.minRange = minRange;
	}

	public void setRange(float range) {
		this.range = range;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public void setDelay(float delay) {
		this.delay = delay;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public void setAlphaMult(float alphaMult) {
		this.alphaMult = alphaMult;
	}

	public void setInFraction(float inFraction) {
		this.inFraction = inFraction;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}

	public void setUseFactionColor(boolean useFactionColor) {
		this.useFactionColor = useFactionColor;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
	
}







