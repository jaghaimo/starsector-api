package com.fs.starfarer.api;

public class SpriteId {

	private String category;
	private String key;
	
	public SpriteId(String category, String key) {
		this.category = category;
		this.key = key;
	}
	public String getCategory() {
		return category;
	}
	public String getKey() {
		return key;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	
}
