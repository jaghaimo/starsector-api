package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer.AuroraRendererDelegate;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager.Flare;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager.FlareManagerDelegate;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class MagneticFieldTerrainPlugin extends BaseRingTerrain implements AuroraRendererDelegate, FlareManagerDelegate {
	
	public static class MagneticFieldParams extends RingParams {
		public Color baseColor;
		transient public List<Color> auroraColorRange = new ArrayList<Color>();
		public String c = null;
		
		public float auroraFrequency;
		
		public float innerRadius;
		public float outerRadius;
		
		public MagneticFieldParams(float bandWidthInEngine, float middleRadius, SectorEntityToken relatedEntity,
								   float innerRadius,
								   float outerRadius,
								   Color baseColor, 
								   float auroraFrequency) {
			this(bandWidthInEngine, middleRadius, relatedEntity, innerRadius, outerRadius,
					baseColor, auroraFrequency, 
					Color.red, Color.orange, Color.yellow, Color.green, Color.blue,
					new Color(75, 0, 130), new Color(127, 0, 255));
		}
	   public MagneticFieldParams(float bandWidthInEngine, float middleRadius, SectorEntityToken relatedEntity,
			   float innerRadius,
			   float outerRadius,
			   Color baseColor, 
			   float auroraFrequency,
								   Color ... auroraColors) {
			super(bandWidthInEngine, middleRadius, relatedEntity);
			this.auroraFrequency = auroraFrequency;
			this.baseColor = baseColor;
			this.innerRadius = innerRadius;
			this.outerRadius = outerRadius;
			if (auroraColors != null) {
				for (Color curr : auroraColors) {
					this.auroraColorRange.add(curr);
				}
			}
		}
	   
		Object readResolve() {
			if (c != null) {
				auroraColorRange = Misc.colorsFromString(c);
			} else {
				auroraColorRange = new ArrayList<Color>();
			}
			return this;
		}
		
		Object writeReplace() {
			c = Misc.colorsToString(auroraColorRange);
			return this;
		}
	}
	
	//public static final float VISIBLITY_MULT = 0.5f;
	
	//public static final float SENSOR_MULT = 0.5f;
	public static final float SENSOR_MULT_AURORA = 0.1f;
	public static final float DETECTED_MULT_AURORA = 0f;
	public static final float DETECTED_MULT = .25f;
	
//	public static float BURN_MULT = 0.5f;
//	public static float BURN_MULT_AURORA = 0.25f;
	

	transient protected SpriteAPI texture = null;
	transient protected Color color;
	protected AuroraRenderer renderer;
	
	protected MagneticFieldParams params;
	protected FlareManager flareManager;

	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		this.params = (MagneticFieldParams) param;
		name = params.name;
		if (name == null) {
			name = "Magnetic Field";
		}
	}
	
	public String getNameForTooltip() {
		return "Magnetic Field";
	}
	
	@Override
	protected Object readResolve() {
		super.readResolve();
		texture = Global.getSettings().getSprite("terrain", "aurora");
		layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7);
		if (renderer == null) {
			renderer = new AuroraRenderer(this);
		}
		if (flareManager == null) {
			flareManager = new FlareManager(this);
		}
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}


	public void advance(float amount) {
		super.advance(amount);
		renderer.advance(amount);
		flareManager.advance(amount);
	}
	
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		renderer.render(viewport.getAlphaMult());
	}
	
	@Override
	public float getRenderRange() {
		Flare curr = flareManager.getActiveFlare();
		if (curr != null) {
			float outerRadiusWithFlare = computeRadiusWithFlare(flareManager.getActiveFlare());
			return outerRadiusWithFlare + 200f;
		}
		return super.getRenderRange();
	}

	@Override
	protected boolean shouldPlayLoopOne() {
		return super.shouldPlayLoopOne() && !flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet());
	}

	@Override
	protected boolean shouldPlayLoopTwo() {
		return super.shouldPlayLoopTwo() && flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet());
	}
	

	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			
//			fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
//					"Inside strong magnetic field", VISIBLITY_MULT, 
//					fleet.getStats().getDetectedRangeMod());
			
			if (flareManager.isInActiveFlareArc(fleet)) {
//				fleet.getStats().removeTemporaryMod(getModId() + "_3");
//				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
//									"Inside magnetic storm", getAdjustedMult(fleet, BURN_MULT_AURORA), 
//									fleet.getStats().getFleetwideMaxBurnMod());
//				fleet.getStats().addTemporaryModPercent(0.1f, getModId() + "_1",
//									"Inside magnetic storm", -100f * (1f - getAdjustedMult(fleet, BURN_MULT_AURORA)), 
//									fleet.getStats().getFleetwideMaxBurnMod());
				
				//fleet.getStats().removeTemporaryMod(getModId() + "_4");
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_2",
						"Inside magnetic storm", SENSOR_MULT_AURORA, 
						fleet.getStats().getSensorRangeMod());
				
				fleet.getStats().removeTemporaryMod(getModId() + "_6");
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_5",
						"Inside magnetic storm", DETECTED_MULT_AURORA, 
						fleet.getStats().getDetectedRangeMod());
			} else {
//				fleet.getStats().removeTemporaryMod(getModId() + "_1");
//				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_3",
//						"Inside strong magnetic field", getAdjustedMult(fleet, BURN_MULT), 
//						fleet.getStats().getFleetwideMaxBurnMod());
//				fleet.getStats().addTemporaryModPercent(0.1f, getModId() + "_3",
//						"Inside strong magnetic field", -100f * (1f - getAdjustedMult(fleet, BURN_MULT)), 
//						fleet.getStats().getFleetwideMaxBurnMod());
				
				fleet.getStats().removeTemporaryMod(getModId() + "_5");
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_6",
						"Inside strong magnetic field", DETECTED_MULT, 
						fleet.getStats().getDetectedRangeMod());
//				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_4",
//								"Inside strong magnetic field", SENSOR_MULT, 
//								fleet.getStats().getSensorRangeMod());
				
			}
		}
	}
	
	protected float getAdjustedMult(CampaignFleetAPI fleet, float baseMult) {
		float skillMod = fleet.getCommanderStats().getDynamic().getValue(Stats.NAVIGATION_PENALTY_MULT);
		float penalty = 1f - baseMult;
		penalty *= skillMod;
		return 1f - penalty;
	}
	
	
	@Override
	public boolean containsPoint(Vector2f point, float radius) {
		if (flareManager.isInActiveFlareArc(point)) {
			float outerRadiusWithFlare = computeRadiusWithFlare(flareManager.getActiveFlare());
			float dist = Misc.getDistance(this.entity.getLocation(), point);
			if (dist > outerRadiusWithFlare + radius) return false;
			if (dist + radius < params.middleRadius - params.bandWidthInEngine / 2f) return false;
			return true;
		}
		return super.containsPoint(point, radius);
	}

	private float computeRadiusWithFlare(Flare flare) {
		//params.relatedEntity.getRadius() + 50f;
		//params.middleRadius + params.bandWidthInEngine * 0.75f;
		float inner = getAuroraInnerRadius();
		float outer = params.middleRadius + params.bandWidthInEngine * 0.5f;
		float thickness = outer - inner;
		
		thickness *= flare.extraLengthMult;
		thickness += flare.extraLengthFlat;
		
		return inner + thickness;
	}
	
	@Override
	public Color getNameColor() {
		Color bad = Misc.getNegativeHighlightColor();
		Color base = super.getNameColor();
		//bad = Color.red;
		//return Misc.interpolateColor(base, bad, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 1f);
		return super.getNameColor();
	}

	public boolean hasTooltip() {
		return true;
	}
	
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addTitle("Magnetic Field");
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		
//		float sensorMult = SENSOR_MULT;
//		float burnMult = getAdjustedMult(player, BURN_MULT);
//		String extraText = "";
//		if (flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet())) {
//			sensorMult = SENSOR_MULT_AURORA;
//			burnMult = getAdjustedMult(player, BURN_MULT_AURORA);
//			//extraText = " The sensor penalty is currently increased due to being inside a magnetic storm.";
//			extraText = " The sensor and travel speed penalties are currently increased due to being inside a magnetic storm.";
//		}
		
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
		
		float detectedMult = DETECTED_MULT;
		if (flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet())) {
			detectedMult = DETECTED_MULT_AURORA;
		}
		tooltip.addPara("Reduces the range at which fleets inside can be detected by %s.", nextPad,
				highlight, 
				"" + (int) ((1f - detectedMult) * 100) + "%"
		);
		
		if (flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet())) {
			tooltip.addPara("The magnetic storm also blinds the sensors of a fleet within," +
					" reducing their range by %s.", pad,
					highlight, 
					"" + (int) ((1f - SENSOR_MULT_AURORA) * 100) + "%"
			);
		}
		
//		String sensorMultStr = Misc.getRoundedValue(1f - sensorMult);
//		String burnMultStr = Misc.getRoundedValue(1f - burnMult);
//		tooltip.addPara("Your fleet's sensor range is reduced by %s. Your fleet's speed is reduced by %s." + extraText, pad,
//				highlight,
//				"" + (int) ((1f - sensorMult) * 100) + "%",
//				"" + (int) ((1f - burnMult) * 100) + "%"
//				);
		
//		tooltip.addPara("Reduces the range at which fleets inside it can be detected by %s. Also reduces fleet sensor range by %s." + extraText, nextPad,
//				highlight, 
//				"" + (int) ((1f - VISIBLITY_MULT) * 100) + "%",
//				"" + (int) ((1f - sensorMult) * 100) + "%"
//		);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("No combat effects.", nextPad);
		}
	}
	
	public boolean isTooltipExpandable() {
		return true;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}
	
	public String getTerrainName() {
		if (flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet())) {
			return "Magnetic Storm";
		}
		return super.getTerrainName();
	}
	
	public String getEffectCategory() {
		return "magnetic_field-like";
	}

	public float getAuroraAlphaMultForAngle(float angle) {
		return 1f;
	}

	public float getAuroraBandWidthInTexture() {
		return 256f;
		//return 512f;
	}

	public Vector2f getAuroraCenterLoc() {
		return params.relatedEntity.getLocation();
	}

	public Color getAuroraColorForAngle(float angle) {
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getColorForAngle(params.baseColor, angle);
		}
		return params.baseColor;
	}

	public float getAuroraInnerRadius() {
		return params.innerRadius;
	}

	public float getAuroraOuterRadius() {
		return params.outerRadius;
	}

	public float getAuroraShortenMult(float angle) {
		return 0f + flareManager.getShortenMod(angle);
		//return 0.3f + flareManager.getShortenMod(angle);
	}
	
	public float getAuroraInnerOffsetMult(float angle) {
		return flareManager.getInnerOffsetMult(angle);
	}

	public float getAuroraTexPerSegmentMult() {
		return 1f;
	}

	public SpriteAPI getAuroraTexture() {
		return texture;
	}

	public float getAuroraThicknessFlat(float angle) {
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getExtraLengthFlat(angle);
		}
		return 0f;
	}

	public float getAuroraThicknessMult(float angle) {
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getExtraLengthMult(angle);
		}
		return 1f;
	}
	

	public float getFlareArcMax() {
		return 80;
	}

	public float getFlareArcMin() {
		return 30;
	}

	public List<Color> getFlareColorRange() {
		return params.auroraColorRange;
	}

	public float getFlareExtraLengthFlatMax() {
		return 0;
	}

	public float getFlareExtraLengthFlatMin() {
		return 0;
	}

	public float getFlareExtraLengthMultMax() {
		return 1;
	}

	public float getFlareExtraLengthMultMin() {
		return 1;
	}

	public float getFlareFadeInMax() {
		return 2f;
	}

	public float getFlareFadeInMin() {
		return 1f;
	}

	public float getFlareFadeOutMax() {
		return 5f;
	}

	public float getFlareFadeOutMin() {
		return 2f;
	}

	public float getFlareOccurrenceAngle() {
		return 0;
	}

	public float getFlareOccurrenceArc() {
		return 360f;
	}

	public float getFlareProbability() {
		return params.auroraFrequency;
	}

	public float getFlareSmallArcMax() {
		return 20;
	}

	public float getFlareSmallArcMin() {
		return 10;
	}

	public float getFlareSmallExtraLengthFlatMax() {
		return 0;
	}

	public float getFlareSmallExtraLengthFlatMin() {
		return 0;
	}

	public float getFlareSmallExtraLengthMultMax() {
		return 1;
	}

	public float getFlareSmallExtraLengthMultMin() {
		return 1;
	}

	public float getFlareSmallFadeInMax() {
		return 1f;
	}

	public float getFlareSmallFadeInMin() {
		return 0.5f;
	}

	public float getFlareSmallFadeOutMax() {
		return 1f;
	}

	public float getFlareSmallFadeOutMin() {
		return 0.5f;
	}

	public float getFlareShortenFlatModMax() {
		return 0.8f;
	}

	public float getFlareShortenFlatModMin() {
		return 0.8f;
	}

	public float getFlareSmallShortenFlatModMax() {
		return 0.8f;
	}

	public float getFlareSmallShortenFlatModMin() {
		return 0.8f;
	}

	public int getFlareMaxSmallCount() {
		return 2;
	}

	public int getFlareMinSmallCount() {
		return 7;
	}

	public float getFlareSkipLargeProbability() {
		return 0f;
	}

	public SectorEntityToken getFlareCenterEntity() {
		return this.entity;
	}

	public boolean hasAIFlag(Object flag) {
		return flag == TerrainAIFlags.REDUCES_DETECTABILITY || flag == TerrainAIFlags.REDUCES_SENSOR_RANGE;
	}
	
	public boolean canPlayerHoldStationIn() {
		return true;
	}

	public RangeBlockerUtil getAuroraBlocker() {
		return null;
	}
}



