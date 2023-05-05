package com.fs.starfarer.api.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.util.Misc;

public class MapParams {
	public static final float GRID_SIZE_MAP_UNITS = 2000;
	public static final float GRID_SIZE_PIXELS = 75f;
	
	
	public SectorEntityToken entityToShow;
	
	public MapFilterData filterData = null;
	
	public boolean smallConstellations = false;
	public List<ArrowData> arrows = new ArrayList<IntelInfoPlugin.ArrowData>();
	
	public boolean showFilter = true;
	public boolean smallFilter = false;
	public boolean showTabs = true;
	
	public float starSelectionRadiusMult = 1f;
	public float starAlphaMult = 1f;
	public boolean useFullAlphaForShownSystems = false;
	
	public Color borderColor = null;
	public boolean renderTopBorder = true;

	public float maxZoomMapSizePadding = 4000f;
	public float zoomLevel = 0f;
	public LocationAPI location = null;
	public Set<StarSystemAPI> showSystems = null;
	public Set<Constellation> showConsellations = null;
	public Vector2f centerOn = null;
	
	public List<MarkerData> markers = null;
	public boolean withLayInCourse = false;
	public boolean skipCurrLocMarkerRendering = false;
	
	
	public MapParams() {
		filterData = new MapFilterData(false);
		showFilter = false;
		smallFilter = true;
		showTabs = false;
		filterData.starscape = true;
		filterData.names = true;
		
		//showSystems = new HashSet<StarSystemAPI>();
		
		starSelectionRadiusMult = 0.8f;
		starAlphaMult = 0.5f;
		
		borderColor = Misc.getDarkPlayerColor();
		renderTopBorder = true;
		
		location = Global.getSector().getHyperspace();
		//skipCurrLocMarkerRendering = true;
		
		//markers = new ArrayList<MarkerData>();
//		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
//		markers.add(new MarkerData(player.getLocationInHyperspace(), null, null));
		
		zoomLevel = 1f;
		
		Vector2f loc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
		centerOn = new Vector2f(loc);
	}
	
	public void positionToShowAllMarkersAndSystems(boolean showPlayerFleet, float heightOnScreen) {
		Vector2f center = new Vector2f();
		float total = 0f;
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		MarkerData playerMarker = null;
		if (showPlayerFleet) {
			skipCurrLocMarkerRendering = true;
			if (markers == null) {
				markers = new ArrayList<MarkerData>();
			}
			playerMarker = new MarkerData(player.getLocationInHyperspace(), null, null);
			markers.add(playerMarker);
//			Vector2f.add(center, player.getLocationInHyperspace(), center);
//			total++;
		}
		
		Vector2f min = new Vector2f();
		Vector2f max = new Vector2f();
		if (showSystems != null) {
			for (StarSystemAPI curr : showSystems) {
				Vector2f p = curr.getLocation();
				min.x = Math.min(min.x, p.x);
				min.y = Math.min(min.y, p.y);
				max.x = Math.max(max.x, p.x);
				max.y = Math.max(max.y, p.y);
				
				Vector2f.add(center, p, center);
				total++;
			}
		}
		
		if (markers != null) {
			for (MarkerData curr : markers) {
				if (curr == playerMarker) continue;
				Vector2f p = curr.coordinates;
				min.x = Math.min(min.x, p.x);
				min.y = Math.min(min.y, p.y);
				max.x = Math.max(max.x, p.x);
				max.y = Math.max(max.y, p.y);
				
				Vector2f.add(center, p, center);
				total++;
			}
		}
		
		if (total > 0) {
			center.scale(1f / total);
		}
		
		float factor = GRID_SIZE_PIXELS / GRID_SIZE_MAP_UNITS;
		float distance = Misc.getDistance(player.getLocationInHyperspace(), center);
		Vector2f diff = Vector2f.sub(max, min, new Vector2f());
		distance += diff.length() * 0.5f;
		
		distance *= 1.2f;
		distance *= factor;
		
		float maxShown = heightOnScreen;
		
		float zoom = (float) (Math.ceil(distance / maxShown)) + 1;
		if (zoom < 3) zoom = 3;
		
		zoomLevel = zoom;
		
		Vector2f loc = Misc.interpolateVector(Global.getSector().getPlayerFleet().getLocationInHyperspace(),
											  center, 0.5f);
		
		centerOn = loc;
		
//		zoomLevel = 100;
//		centerOn = new Vector2f();
	}
	
	public void showSystem(StarSystemAPI system) {
		if (showSystems == null) {
			showSystems = new HashSet<StarSystemAPI>();
		}
		showSystems.add(system);
	}
	
	public void showMarket(MarketAPI market) {
		float scale = 1f;
		if (market != null && !market.isPlanetConditionMarketOnly()) {
			scale = MarkerData.getScaleForMarket(market);
		}
		showMarket(market, scale);
	}
	public void showMarket(MarketAPI market, float scale) {
		if (markers == null) {
			markers = new ArrayList<MarkerData>();
		}
		
		markers.add(new MarkerData(
						market.getLocationInHyperspace(), 
						null, 
						market.getFaction().getBaseUIColor(), 
						scale));
	}
}















