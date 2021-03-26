package com.fs.starfarer.api.loading;

import java.awt.Color;

import org.json.JSONObject;

public class ContactTagSpec {
	
//	"name":"Important",
//	"color":"buttonShortcut",
//	"width":120,
//	"putFirst":true,
//	"sort":10,

	private String tag;
	private String name;
	private Color color;
	private float width;
	private boolean putFirst;
	private float sort;
	public ContactTagSpec(String tag, Color color, JSONObject json) {
		this.tag = tag;
		this.color = color;
		
		name = json.optString("name", null);
		width = (float) json.optDouble("width", 0);
		putFirst = json.optBoolean("putFirst", false);
		sort = (float) json.optDouble("sort", 0);
	}
	
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public float getWidth() {
		return width;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public boolean isPutFirst() {
		return putFirst;
	}
	public void setPutFirst(boolean putFirst) {
		this.putFirst = putFirst;
	}
	public float getSort() {
		return sort;
	}
	public void setSort(float sort) {
		this.sort = sort;
	}
	

	
	
}
