package com.fs.starfarer.api.ui;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

public class MarkerData {
	public Vector2f coordinates;
	public LocationAPI location;
	public Color color;
	public float scale = 1f;
	public MarkerData(Vector2f coordinates, LocationAPI location) {
		this(coordinates, location, Global.getSector().getPlayerFaction().getBaseUIColor());
	}
	public MarkerData(Vector2f coordinates, LocationAPI location, Color color) {
		if (color == null) color = Global.getSector().getPlayerFaction().getBaseUIColor();
		this.coordinates = coordinates;
		this.location = location;
		if (this.location == null) {
			this.location = Global.getSector().getHyperspace();
		}
		this.color = Misc.scaleColorOnly(color, 0.67f);
	}
	public MarkerData(Vector2f coordinates, LocationAPI location, Color color, float scale) {
		this.coordinates = coordinates;
		this.location = location;
		this.color = Misc.scaleColorOnly(color, 0.67f);
		this.scale = scale;
		if (this.location == null) {
			this.location = Global.getSector().getHyperspace();
		}
	}
	public MarkerData(Vector2f coordinates, LocationAPI location, float scale) {
		this.coordinates = coordinates;
		this.location = location;
		this.scale = scale;
		color = Global.getSector().getPlayerFaction().getBaseUIColor();
		this.color = Misc.scaleColorOnly(color, 0.67f);
		if (this.location == null) {
			this.location = Global.getSector().getHyperspace();
		}
	}
	public static float getScaleForMarket(MarketAPI market) {
		float size = market.getSize();
		float scale = size / 8f;
		if (scale < 0.33f) scale = 0.33f;
		if (scale > 1f) scale = 1f;
		return scale;
	}
	
}