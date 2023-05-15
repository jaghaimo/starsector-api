package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer.AuroraRendererDelegate;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager.Flare;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager.FlareManagerDelegate;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class StarCoronaTerrainPlugin extends BaseRingTerrain implements AuroraRendererDelegate, FlareManagerDelegate {
	
	public static final float CR_LOSS_MULT_GLOBAL = 0.25f;
	
	public static class CoronaParams extends RingParams {
		public float windBurnLevel;
		public float flareProbability;
		public float crLossMult;
		
		public CoronaParams(float bandWidthInEngine, float middleRadius,
				SectorEntityToken relatedEntity,
				float windBurnLevel, float flareProbability, float crLossMult) {
			super(bandWidthInEngine, middleRadius, relatedEntity);
			this.windBurnLevel = windBurnLevel;
			this.flareProbability = flareProbability;
			this.crLossMult = crLossMult;
		}
	}
	
	transient protected SpriteAPI texture = null;
	transient protected Color color;
	
	protected AuroraRenderer renderer;
	protected FlareManager flareManager;
	protected CoronaParams params;
	
	protected transient RangeBlockerUtil blocker = null;
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		params = (CoronaParams) param;
		name = params.name;
		if (name == null) {
			name = "Corona";
		}
	}
	
	public String getNameForTooltip() {
		return "Corona";
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
		if (blocker == null) {
			blocker = new RangeBlockerUtil(360, super.params.bandWidthInEngine + 1000f);
		}
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	@Override
	protected boolean shouldPlayLoopOne() {
		return super.shouldPlayLoopOne() && !flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet());
	}

	@Override
	protected boolean shouldPlayLoopTwo() {
		return super.shouldPlayLoopTwo() && flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet());
	}



	transient private EnumSet<CampaignEngineLayers> layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7);
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return layers;
	}

	public CoronaParams getParams() {
		return params;
	}

	public void advance(float amount) {
		super.advance(amount);
		renderer.advance(amount);
		flareManager.advance(amount);
		
		if (amount > 0 && blocker != null) {
			blocker.updateLimits(entity, params.relatedEntity, 0.5f);
			blocker.advance(amount, 100f, 0.5f);
		}
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		if (blocker != null && !blocker.wasEverUpdated()) {
			blocker.updateAndSync(entity, params.relatedEntity, 0.5f);
		}
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
	public boolean containsPoint(Vector2f point, float radius) {
		if (blocker != null && blocker.isAnythingShortened()) {
			float angle = Misc.getAngleInDegrees(this.entity.getLocation(), point);
			float dist = Misc.getDistance(this.entity.getLocation(), point);
			float max = blocker.getCurrMaxAt(angle);
			if (dist > max) return false;
		}
		
		if (flareManager.isInActiveFlareArc(point)) {
			float outerRadiusWithFlare = computeRadiusWithFlare(flareManager.getActiveFlare());
			float dist = Misc.getDistance(this.entity.getLocation(), point);
			if (dist > outerRadiusWithFlare + radius) return false;
			if (dist + radius < params.middleRadius - params.bandWidthInEngine / 2f) return false;
			return true;
		}
		return super.containsPoint(point, radius);
	}
	
	protected float computeRadiusWithFlare(Flare flare) {
		float inner = getAuroraInnerRadius();
		float outer = params.middleRadius + params.bandWidthInEngine * 0.5f;
		float thickness = outer - inner;
		
		thickness *= flare.extraLengthMult;
		thickness += flare.extraLengthFlat;
		
		return inner + thickness;
	}
	
	@Override
	protected float getExtraSoundRadius() {
		float base = super.getExtraSoundRadius();
		
		float angle = Misc.getAngleInDegrees(params.relatedEntity.getLocation(), Global.getSector().getPlayerFleet().getLocation());
		float extra = 0f;
		if (flareManager.isInActiveFlareArc(angle)) {
			extra = computeRadiusWithFlare(flareManager.getActiveFlare()) - params.bandWidthInEngine;
		}
		//System.out.println("Extra: " + extra);
		return base + extra;
	}
	

	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			
			boolean inFlare = false;
			if (flareManager.isInActiveFlareArc(fleet)) {
				inFlare = true;
			}
			
			float intensity = getIntensityAtPoint(fleet.getLocation());
			if (intensity <= 0) return;

			String buffId = getModId();
			float buffDur = 0.1f;

			boolean protectedFromCorona = false;
			if (fleet.isInCurrentLocation() && 
					Misc.getDistance(fleet, Global.getSector().getPlayerFleet()) < 500) {
				for (SectorEntityToken curr : fleet.getContainingLocation().getCustomEntitiesWithTag(Tags.PROTECTS_FROM_CORONA_IN_BATTLE)) {
					float dist = Misc.getDistance(curr, fleet);
					if (dist < curr.getRadius() + fleet.getRadius() + 10f) {
						protectedFromCorona = true;
						break;
					}
				}
			}
			
			// CR loss and peak time reduction
			for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
				float recoveryRate = member.getStats().getBaseCRRecoveryRatePercentPerDay().getModifiedValue();
				float lossRate = member.getStats().getBaseCRRecoveryRatePercentPerDay().getBaseValue();
				
				float resistance = member.getStats().getDynamic().getValue(Stats.CORONA_EFFECT_MULT);
				if (protectedFromCorona) resistance = 0f;
				//if (inFlare) loss *= 2f;
				float lossMult = 1f;
				if (inFlare) lossMult = 2f;
				float adjustedLossMult = (0f + params.crLossMult * intensity * resistance * lossMult * CR_LOSS_MULT_GLOBAL);
				
				float loss = (-1f * recoveryRate + -1f * lossRate * adjustedLossMult) * days * 0.01f;
				float curr = member.getRepairTracker().getBaseCR();
				if (loss > curr) loss = curr;
				if (resistance > 0) { // not actually resistance, the opposite
					if (inFlare) {
						member.getRepairTracker().applyCREvent(loss, "flare", "Solar flare effect");
					} else {
						member.getRepairTracker().applyCREvent(loss, "corona", "Star corona effect");
					}
					
					float peakFraction = 1f / Math.max(1.3333f, 1f + params.crLossMult * intensity);
					float peakLost = 1f - peakFraction;
					peakLost *= resistance;
					
					float degradationMult = 1f + (params.crLossMult * intensity * resistance) / 2f;
					
					member.getBuffManager().addBuffOnlyUpdateStat(new PeakPerformanceBuff(buffId + "_1", 1f - peakLost, buffDur));
					member.getBuffManager().addBuffOnlyUpdateStat(new CRLossPerSecondBuff(buffId + "_2", degradationMult, buffDur));
				}
			}
			
			// "wind" effect - adjust velocity
			float maxFleetBurn = fleet.getFleetData().getBurnLevel();
			float currFleetBurn = fleet.getCurrBurnLevel();
			
			float maxWindBurn = params.windBurnLevel;
			if (inFlare) {
				maxWindBurn *= 2f;
			}
			
			
			float currWindBurn = intensity * maxWindBurn;
			float maxFleetBurnIntoWind = maxFleetBurn - Math.abs(currWindBurn);
			
			float angle = Misc.getAngleInDegreesStrict(this.entity.getLocation(), fleet.getLocation());
			Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(angle);
			if (currWindBurn < 0) {
				windDir.negate();
			}
			
			Vector2f velDir = Misc.normalise(new Vector2f(fleet.getVelocity()));
			velDir.scale(currFleetBurn);
			
			float fleetBurnAgainstWind = -1f * Vector2f.dot(windDir, velDir);
			
			float accelMult = 0.5f;
			if (fleetBurnAgainstWind > maxFleetBurnIntoWind) {
				accelMult += 0.75f + 0.25f * (fleetBurnAgainstWind - maxFleetBurnIntoWind);
			}
			float fleetAccelMult = fleet.getStats().getAccelerationMult().getModifiedValue();
			if (fleetAccelMult > 0) {// && fleetAccelMult < 1) {
				accelMult /= fleetAccelMult;
			}
			
			float seconds = days * Global.getSector().getClock().getSecondsPerDay();
			
			Vector2f vel = fleet.getVelocity();
			windDir.scale(seconds * fleet.getAcceleration() * accelMult);
			fleet.setVelocity(vel.x + windDir.x, vel.y + windDir.y);
			
			Color glowColor = getAuroraColorForAngle(angle);
			int alpha = glowColor.getAlpha();
			if (alpha < 75) {
				glowColor = Misc.setAlpha(glowColor, 75);
			}
			// visual effects - glow, tail
			
			
			float dist = Misc.getDistance(this.entity.getLocation(), fleet.getLocation());
			float check = 100f;
			if (params.relatedEntity != null) check = params.relatedEntity.getRadius() * 0.5f;
			if (dist > check) {
				float durIn = 1f;
				float durOut = 10f;
				Misc.normalise(windDir);
				float sizeNormal = 5f + 10f * intensity;
				float sizeFlare = 10f + 15f * intensity;
				for (FleetMemberViewAPI view : fleet.getViews()) {
					if (inFlare) {
						view.getWindEffectDirX().shift(getModId() + "_flare", windDir.x * sizeFlare, durIn, durOut, 1f);
						view.getWindEffectDirY().shift(getModId() + "_flare", windDir.y * sizeFlare, durIn, durOut, 1f);
						view.getWindEffectColor().shift(getModId() + "_flare", glowColor, durIn, durOut, intensity);
					} else {
						view.getWindEffectDirX().shift(getModId(), windDir.x * sizeNormal, durIn, durOut, 1f);
						view.getWindEffectDirY().shift(getModId(), windDir.y * sizeNormal, durIn, durOut, 1f);
						view.getWindEffectColor().shift(getModId(), glowColor, durIn, durOut, intensity);
					}
				}
			}
		}
	}
	
	public float getIntensityAtPoint(Vector2f point) {
		float angle = Misc.getAngleInDegrees(params.relatedEntity.getLocation(), point);
		float maxDist = params.bandWidthInEngine;
		if (flareManager.isInActiveFlareArc(angle)) {
			maxDist = computeRadiusWithFlare(flareManager.getActiveFlare());
		}
		float minDist = params.relatedEntity.getRadius();
		float dist = Misc.getDistance(point, params.relatedEntity.getLocation());
		
		if (dist > maxDist) return 0f;
		
		float intensity = 1f;
		if (minDist < maxDist) {
			intensity = 1f - (dist - minDist) / (maxDist - minDist);
			//intensity = 0.5f + intensity * 0.5f;
			if (intensity < 0) intensity = 0;
			if (intensity > 1) intensity = 1;
		}
		
		return intensity;
	}
	
	
	
	@Override
	public Color getNameColor() {
		Color bad = Misc.getNegativeHighlightColor();
		Color base = super.getNameColor();
		//bad = Color.red;
		return Misc.interpolateColor(base, bad, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 1f);
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
		
		tooltip.addTitle(name);
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
		tooltip.addPara("Reduces the combat readiness of " +
				"all ships in the corona at a steady pace.", nextPad);
		tooltip.addPara("The heavy solar wind also makes the star difficult to approach.", pad);
		tooltip.addPara("Occasional solar flare activity takes these effects to even more dangerous levels.", pad);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Reduces the peak performance time of ships and increases the rate of combat readiness degradation in protracted engagements.", small);
		}
		
		//tooltip.addPara("Does not stack with other similar terrain effects.", pad);
	}
	
	public boolean isTooltipExpandable() {
		return true;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}
	
	public String getTerrainName() {
		if (flareManager.isInActiveFlareArc(Global.getSector().getPlayerFleet())) {
			return "Solar Flare";
		}
		return super.getTerrainName();
	}
	
	public String getEffectCategory() {
		return null; // to ensure multiple coronas overlapping all take effect
		//return "corona_" + (float) Math.random();
	}

	public float getAuroraAlphaMultForAngle(float angle) {
		return 1f;
	}

	public float getAuroraBandWidthInTexture() {
		return 256f;
		//return 512f;
	}
	
	public float getAuroraTexPerSegmentMult() {
		return 1f;
		//return 2f;
	}

	public Vector2f getAuroraCenterLoc() {
		return params.relatedEntity.getLocation();
	}

	public Color getAuroraColorForAngle(float angle) {
		if (color == null) {
			if (params.relatedEntity instanceof PlanetAPI) {
				color = ((PlanetAPI)params.relatedEntity).getSpec().getCoronaColor();
				//color = Misc.interpolateColor(color, Color.white, 0.5f);
			} else {
				color = Color.white;
			}
			color = Misc.setAlpha(color, 25);
		}
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getColorForAngle(color, angle);
		}
		return color;
	}

	public float getAuroraInnerRadius() {
		return params.relatedEntity.getRadius() + 50f;
	}

	public float getAuroraOuterRadius() {
		return params.middleRadius + params.bandWidthInEngine * 0.5f;
	}

	public float getAuroraShortenMult(float angle) {
		return 0.85f + flareManager.getShortenMod(angle);
	}
	
	public float getAuroraInnerOffsetMult(float angle) {
		return flareManager.getInnerOffsetMult(angle);
	}

	public SpriteAPI getAuroraTexture() {
		return texture;
	}
	
	public RangeBlockerUtil getAuroraBlocker() {
		return blocker;
	}

	public float getAuroraThicknessFlat(float angle) {
//		float shorten = blocker.getShortenAmountAt(angle);
//		if (shorten > 0) return -shorten;
//		if (true) return -4000f;
		
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getExtraLengthFlat(angle);
		}
		return 0;
	}

	public float getAuroraThicknessMult(float angle) {
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getExtraLengthMult(angle);
		}
		return 1f;
	}
	
	
	
	

	public List<Color> getFlareColorRange() {
		List<Color> result = new ArrayList<Color>();
		
		if (params.relatedEntity instanceof PlanetAPI) {
			Color color = ((PlanetAPI)params.relatedEntity).getSpec().getCoronaColor();
			result.add(Misc.setAlpha(color, 255));
		} else {
			result.add(Color.white);
		}
		//result.add(Misc.setAlpha(getAuroraColorForAngle(0), 127));
		return result;
	}
	
	public float getFlareArcMax() {
		return 60;
	}
	
	public float getFlareArcMin() {
		return 30;
	}

	public float getFlareExtraLengthFlatMax() {
		return 500;
	}

	public float getFlareExtraLengthFlatMin() {
		return 200;
	}

	public float getFlareExtraLengthMultMax() {
		return 1.5f;
	}

	public float getFlareExtraLengthMultMin() {
		return 1;
	}

	public float getFlareFadeInMax() {
		return 10f;
	}

	public float getFlareFadeInMin() {
		return 3f;
	}

	public float getFlareFadeOutMax() {
		return 10f;
	}

	public float getFlareFadeOutMin() {
		return 3f;
	}

	public float getFlareOccurrenceAngle() {
		return 0;
	}

	public float getFlareOccurrenceArc() {
		return 360f;
	}

	public float getFlareProbability() {
		return params.flareProbability;
	}

	public float getFlareSmallArcMax() {
		return 20;
	}

	public float getFlareSmallArcMin() {
		return 10;
	}

	public float getFlareSmallExtraLengthFlatMax() {
		return 100;
	}

	public float getFlareSmallExtraLengthFlatMin() {
		return 50;
	}

	public float getFlareSmallExtraLengthMultMax() {
		return 1.05f;
	}

	public float getFlareSmallExtraLengthMultMin() {
		return 1;
	}

	public float getFlareSmallFadeInMax() {
		return 2f;
	}

	public float getFlareSmallFadeInMin() {
		return 1f;
	}

	public float getFlareSmallFadeOutMax() {
		return 2f;
	}

	public float getFlareSmallFadeOutMin() {
		return 1f;
	}

	public float getFlareShortenFlatModMax() {
		return 0.05f;
	}

	public float getFlareShortenFlatModMin() {
		return 0.05f;
	}

	public float getFlareSmallShortenFlatModMax() {
		return 0.05f;
	}

	public float getFlareSmallShortenFlatModMin() {
		return 0.05f;
	}

	public int getFlareMaxSmallCount() {
		return 3;
	}

	public int getFlareMinSmallCount() {
		return 5;
	}

	public float getFlareSkipLargeProbability() {
		return 0f;
	}

	public SectorEntityToken getFlareCenterEntity() {
		return this.entity;
	}
	
	public boolean hasAIFlag(Object flag) {
		return flag == TerrainAIFlags.CR_DRAIN ||
				flag == TerrainAIFlags.BREAK_OTHER_ORBITS ||
				flag == TerrainAIFlags.EFFECT_DIMINISHED_WITH_RANGE;
	}
	
	public float getMaxEffectRadius(Vector2f locFrom) {
		float angle = Misc.getAngleInDegrees(params.relatedEntity.getLocation(), locFrom);
		float maxDist = params.bandWidthInEngine;
		if (flareManager.isInActiveFlareArc(angle)) {
			maxDist = computeRadiusWithFlare(flareManager.getActiveFlare());
		}
		return maxDist;
	}
	public float getMinEffectRadius(Vector2f locFrom) {
		return 0f;
	}
	
	public float getOptimalEffectRadius(Vector2f locFrom) {
		return params.relatedEntity.getRadius();
	}
	
	public boolean canPlayerHoldStationIn() {
		return false;
	}

	public FlareManager getFlareManager() {
		return flareManager;
	}
	
}





