package com.fs.starfarer.api.impl.campaign.terrain;

import java.util.EnumSet;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * No visuals - meant to be used in conjunction with the various LocationAPI.addRingBand() methods.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public abstract class BaseRingTerrain extends BaseTerrain {

	public static class RingParams {
		public float bandWidthInEngine;
		public float middleRadius;
		public SectorEntityToken relatedEntity;
		public String name;
		public RingParams(float bandWidthInEngine, float middleRadius,
				SectorEntityToken relatedEntity, String name) {
			this.bandWidthInEngine = bandWidthInEngine;
			this.middleRadius = middleRadius;
			this.relatedEntity = relatedEntity;
			this.name = name;
		}
		public RingParams(float bandWidthInEngine, float middleRadius,
				SectorEntityToken relatedEntity) {
			this(bandWidthInEngine, middleRadius, relatedEntity, null);
		}
		
	}
	
	public RingParams params;
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		params = (RingParams) param;
		readResolve();
	}
	
	
	@Override
	public SectorEntityToken getRelatedEntity() {
		return params.relatedEntity;
	}

	protected Object readResolve() {
		layers = EnumSet.of(CampaignEngineLayers.TERRAIN_2);
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	transient protected EnumSet<CampaignEngineLayers> layers = EnumSet.of(CampaignEngineLayers.TERRAIN_2);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}
	
	@Override
	public boolean containsEntity(SectorEntityToken other) {
		if (other.getContainingLocation() != this.entity.getContainingLocation()) return false;
		return containsPoint(other.getLocation(), other.getRadius()) && !isPreventedFromAffecting(other);
	}
		
	@Override
	public boolean containsPoint(Vector2f point, float radius) {		
		float dist = Misc.getDistance(this.entity.getLocation(), point);
		if (dist > getMaxRadiusForContains() + radius) return false;
		if (dist < getMinRadiusForContains() - radius) return false;
		return true;
	}
	
	protected float getMinRadiusForContains() {
		return params.middleRadius - params.bandWidthInEngine / 2f;
	}
	
	protected float getMaxRadiusForContains() {
		return params.middleRadius + params.bandWidthInEngine / 2f;
	}

	public float getRenderRange() {
		return params.middleRadius + params.bandWidthInEngine / 2f + 50f;
	}
	
	public RingParams getRingParams() {
		return params;
	}

	public float getProximitySoundFactor() {
		float width = params.bandWidthInEngine / 2f + getExtraSoundRadius() + Global.getSector().getPlayerFleet().getRadius();
		if (width <= 1f) return 1f;
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float dist = Misc.getDistance(this.entity.getLocation(), player.getLocation());
		float mid = params.middleRadius;
		
		float distFromMid = Math.abs(dist - mid);
		if (dist < mid && params.middleRadius - params.bandWidthInEngine / 2f < getExtraSoundRadius()) {
			distFromMid = 0f;
		}
		if (distFromMid < width * 0.5f) return 1f;
		return (width - distFromMid) / (width * 0.5f); 
	}
	
	public void render(CampaignEngineLayers layer, ViewportAPI v) {
	}
	
	public void renderOnMap(float factor, float alphaMult) {
	}

	public void renderOnMapAbove(float factor, float alphaMult) {
	}
	
	public float getMaxEffectRadius(Vector2f locFrom) {
		return params.middleRadius + params.bandWidthInEngine / 2f;
	}
	public float getMinEffectRadius(Vector2f locFrom) {
		return params.middleRadius - params.bandWidthInEngine / 2f;
	}
	
	public float getOptimalEffectRadius(Vector2f locFrom) {
		return params.middleRadius;
	}
	
	public String getNameAOrAn() {
		if (getTerrainName() != null) {
			return Misc.getAOrAnFor(getTerrainName());
		}
		return "a";
	}
}







