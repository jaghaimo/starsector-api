package com.fs.starfarer.api.impl.campaign.enc;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.LocationAPI;

public class EncounterPoint {
	public String id;
	public LocationAPI where; 
	public Vector2f loc;
	public String type;
	public Object custom;
	
	public EncounterPoint(String id, LocationAPI where, Vector2f loc, String type) {
		this.id = id;
		this.where = where;
		this.loc = loc;
		this.type = type;
	}
	
	public Vector2f getLocInHyper() {
		Vector2f loc = this.loc;
		if (!where.isHyperspace()) {
			loc = where.getLocation();
		}
		return loc;
	}

	@Override
	public String toString() {
		return "id:" + id + ", where:" + where + ", loc: " + loc + ", type: " + type; 
	}
	
	
	
}
