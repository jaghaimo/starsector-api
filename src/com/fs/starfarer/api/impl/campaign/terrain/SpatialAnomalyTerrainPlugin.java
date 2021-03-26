package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer.AuroraRendererDelegate;

public class SpatialAnomalyTerrainPlugin extends BaseRingTerrain implements AuroraRendererDelegate {
	
	public static class SpatialAnomalyParams extends RingParams {
		public float dur;
		public CampaignFleetAPI source;
		public SpatialAnomalyParams(CampaignFleetAPI source, float dur, float bandWidthInEngine, SectorEntityToken relatedEntity) {
			super(bandWidthInEngine, bandWidthInEngine / 2f, relatedEntity);
			this.source = source;
			this.dur = dur;
		}
	}
	
	transient protected SpriteAPI texture = null;
	protected SpatialAnomalyParams params;

	protected float elapsed;
	protected AuroraRenderer renderer;
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		this.params = (SpatialAnomalyParams) param;
		name = "Spatial Anomaly";
	}
	
	@Override
	protected Object readResolve() {
		super.readResolve();
		layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7);
		texture = Global.getSettings().getSprite("terrain", "aurora");
		if (renderer == null) {
			renderer = new AuroraRenderer(this);
		}
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.noneOf(CampaignEngineLayers.class);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}

	protected transient float phase = 0f;
	public void advance(float amount) {
		super.advance(amount);
		
		float period = (float)Math.PI * 2f;
		phase += period/10f * amount;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsed +=  days;
		if (elapsed >= 0.3f) params.source = null;
		
//		elapsed = 0.5f;
		if (elapsed >= params.dur) {
			getEntity().setExpired(true);
		}
		
		renderer.advance(amount);
	}
	
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		float left = params.dur - elapsed;
		if (left < 0) left = 0;
		if (left < 0.1f) {
			alphaMult *= left / 0.1f;
		}
		if (elapsed < 0.1f) {
			alphaMult *= elapsed / 0.1f;
		}
		
		renderer.render(alphaMult);
		
//		GL11.glTranslatef(entity.getLocation().x, entity.getLocation().y, 0);
//		
//		
//		float r = params.bandWidthInEngine;
//		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//		Misc.renderQuad(-r, -r, r * 2f, r * 2f, Color.gray, alphaMult);
//		
//		GL11.glPopMatrix();
	}
	
	
	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity == params.source) return;
		
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			
			float penalty = 0.5f;
			fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
								"Inside " + getTerrainName().toLowerCase(), 1f - penalty, 
								fleet.getStats().getFleetwideMaxBurnMod());
		}
	}
	
	
	@Override
	public boolean containsPoint(Vector2f point, float radius) {
		return super.containsPoint(point, radius);
	}

	public boolean hasTooltip() {
		return false;
	}
	
	public String getTerrainName() {
		return super.getTerrainName();
	}
	
	public String getNameForTooltip() {
		return getTerrainName();
	}
	
	public String getEffectCategory() {
		return "spatial_anomaly";
	}

	public boolean canPlayerHoldStationIn() {
		return false;
	}

	
	
	
	public float getAuroraAlphaMultForAngle(float angle) {
		return 1f;
	}

	public float getAuroraBandWidthInTexture() {
		return 256f;
	}

	public RangeBlockerUtil getAuroraBlocker() {
		return null;
	}

	public Vector2f getAuroraCenterLoc() {
		return getEntity().getLocation();
	}

	public Color getAuroraColorForAngle(float angle) {
		return new Color(255,165,100,255);
	}

	public float getAuroraInnerOffsetMult(float angle) {
		return 1f;
	}

	public float getAuroraInnerRadius() {
		//return params.bandWidthInEngine * 0.5f;
		return 50f;
	}

	public float getAuroraOuterRadius() {
		return params.bandWidthInEngine;
	}

	public float getAuroraShortenMult(float angle) {
		return 0.5f;
	}

	public float getAuroraTexPerSegmentMult() {
		return 1f;
	}

	public SpriteAPI getAuroraTexture() {
		return texture;
	}

	public float getAuroraThicknessFlat(float angle) {
		return 0;
	}

	public float getAuroraThicknessMult(float angle) {
		return 1f;
	}	
	
}
