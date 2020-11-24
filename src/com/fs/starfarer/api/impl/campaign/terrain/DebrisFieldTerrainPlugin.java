package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DebrisFieldTerrainPlugin extends BaseRingTerrain {
	
	// when this many days are left, density will gradually go to 0
	public static final float DISSIPATE_DAYS = 3f;
	public static final float VISIBLITY_MULT = 0.25f;
	
	public static float computeDetectionRange(float radius) {
		float range = 100f + radius * 5f;
		if (range > 2000) range = 2000;
		return range;
	}
	
	public static enum DebrisFieldSource {
		GEN,
		PLAYER_SALVAGE,
		SALVAGE,
		BATTLE,
		MIXED,
	}
	
	public static class DebrisFieldParams extends RingParams {
		public float density;
		public float baseDensity;
		public float glowsDays;
		public float lastsDays;
		
		public float minSize = 4;
		public float maxSize = 16;
		public Color glowColor = new Color(255,165,100,255);
		
		
		public String defFaction = null;
		public float defenderProb = 0;
		public int minStr = 0;
		public int maxStr = 0;
		public int maxDefenderSize = 4;
		public long baseSalvageXP = 0;
		public DebrisFieldSource source = DebrisFieldSource.MIXED;
		
		public DebrisFieldParams(float bandWidthInEngine, float density,
								 float lastsDays, float glowsDays) {
			super(bandWidthInEngine, bandWidthInEngine / 2f, null);
			this.density = density;
			this.baseDensity = density;
			this.glowsDays = glowsDays;
			this.lastsDays = lastsDays;
		}
	}
	
	
	protected transient List<DebrisPiece> pieces;
	protected transient boolean initedDebris = false;

	public DebrisFieldParams params;
	protected boolean fadingOut = false;
	protected FaderUtil expander; // days;
	//protected float glowDaysLeft, daysLeft;
	protected float elapsed;
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		params = (DebrisFieldParams) param;
		name = params.name;
		if (name == null) {
			name = "Debris Field";
		}
		
//		glowDaysLeft = params.glowsDays;
//		daysLeft = params.lastsDays;
		
		float dur = params.bandWidthInEngine / 500f;
		expander = new FaderUtil(0, dur, dur);
		expander.fadeIn();
		
		((CampaignTerrainAPI)entity).setRadius(params.bandWidthInEngine);
		
		entity.setDetectionRangeDetailsOverrideMult(0.4f);
		entity.addTag(Tags.DEBRIS_FIELD);
	}
	
	public DebrisFieldParams getParams() {
		return params;
	}

	@Override
	protected Object readResolve() {
		super.readResolve();
		layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7A);
		//initDebrisIfNeeded();
		return this;
	}
	
	protected transient boolean wasInNonCurrentLocation = false;
	public void advance(float amount) {
		super.advance(amount);
		
		if (amount <= 0) {
			//((CampaignTerrainAPI)entity).setRadius(params.bandWidthInEngine);
			return; // happens during game load
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsed += days;
		
		if (!entity.isInCurrentLocation()) {
			pieces = null;
			initedDebris = false;
			wasInNonCurrentLocation = true;
			
			if (params.lastsDays - elapsed <= 0) {
				getEntity().setExpired(true);
			}
			return;
		}
		
		// not necessary?
		// necessary because it affects the number of sensor contact indicators
		((CampaignTerrainAPI)entity).setRadius(params.bandWidthInEngine);
		
		
//		daysLeft -= days;
//		glowDaysLeft -= days;
		
		float left = params.lastsDays - elapsed;
		if (left < DISSIPATE_DAYS) {
			float decr = days / DISSIPATE_DAYS * params.baseDensity;
			params.density -= decr;
			if (params.density < 0) params.density = 0;
		}
		
		if (wasInNonCurrentLocation) {
			expander.forceIn();
			wasInNonCurrentLocation = false;
		}
		expander.advance(days);
		
		initDebrisIfNeeded();
		
		List<DebrisPiece> remove = new ArrayList<DebrisPiece>();
		int withIndicator = 0;
		for (DebrisPiece piece : pieces) {
			piece.advance(days);
			if (piece.isDone()) {
				remove.add(piece);
			} else {
				if (piece.hasIndicator()) {
					withIndicator++;
				}
//				if (glowDaysLeft > 0) {
//					piece.getGlowFader().fadeIn();
//				} else {
//					piece.getGlowFader().fadeOut();
//				}
			}
		}
		pieces.removeAll(remove);

		int withIndicatorGoal = (int) (pieces.size() * 0.1f);
		if (withIndicatorGoal < 3) withIndicatorGoal = 3;
		
		int addIndicators = withIndicatorGoal - withIndicator;
		if (addIndicators > 0) {
			WeightedRandomPicker<DebrisPiece> picker = new WeightedRandomPicker<DebrisPiece>();
			for (DebrisPiece piece : pieces) {
				if (!piece.hasIndicator()) {
					picker.add(piece);
				}
			}
			for (int i = 0; i < addIndicators && !picker.isEmpty(); i++) {
				DebrisPiece piece = picker.pickAndRemove();
				if (piece != null) {
					piece.showIndicator();
				}
			}
		}
		
		
		if (left > 0) {
			addPiecesToMax();
		} else {
			if (pieces.isEmpty()) {
				getEntity().setExpired(true);
			}
		}

	}
		
	@Override
	protected float getMaxRadiusForContains() {
		return super.getMaxRadiusForContains() * expander.getBrightness();
	}

	@Override
	protected float getMinRadiusForContains() {
		return super.getMinRadiusForContains() * expander.getBrightness();
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		//System.out.println("RENDER");
		super.render(layer, viewport);
		
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= entity.getSensorFaderBrightness();
		alphaMult *= entity.getSensorContactFaderBrightness();
		if (alphaMult <= 0) return;

		
		GL11.glPushMatrix();
		GL11.glTranslatef(entity.getLocation().x, entity.getLocation().y, 0);
		
		initDebrisIfNeeded();
		for (DebrisPiece piece : pieces) {
			piece.render(alphaMult);
		}
		
		for (DebrisPiece piece : pieces) {
			piece.renderIndicator(alphaMult);
		}
		
		GL11.glPopMatrix();
		
	}
	
	protected void addPiecesToMax() {
		float mult = params.bandWidthInEngine / 500f;
		mult *= mult;
		int baseMax = (int) (mult * 100f * (0.5f + 0.5f * params.density));
		
		if (baseMax < 7) baseMax = 7;
		//baseMax = 100;
		//System.out.println(baseMax);
		//System.out.println(baseMax);
		int max = (int) (baseMax * expander.getBrightness() * expander.getBrightness());
		
		max *= 2;
		
		while (pieces.size() < max) {
			DebrisPiece piece = new DebrisPiece(this);
			pieces.add(piece);
		}
	}
	
	protected void initDebrisIfNeeded() {
		if (initedDebris) return;
		initedDebris = true;
		
		pieces = new ArrayList<DebrisPiece>();
		
		if (params.lastsDays - elapsed > 0) {
			addPiecesToMax();
		}
		
		for (DebrisPiece piece : pieces) {
			piece.advance(1f * expander.getBrightness());
		}
	}
	
	
	
	

	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			if (fleet.getCurrBurnLevel() <= RingSystemTerrainPlugin.MAX_SNEAK_BURN_LEVEL) {
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
									"Hiding inside debris field", VISIBLITY_MULT, 
									fleet.getStats().getDetectedRangeMod());
			}
		}
	}

	public boolean hasTooltip() {
		return true;
	}

	private String nameForTooltip = null;
	public String getNameForTooltip() {
		if (nameForTooltip == null) return "Debris Field";
		return nameForTooltip;
	}

	public void setNameForTooltip(String nameForTooltip) {
		this.nameForTooltip = nameForTooltip;
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color fuel = Global.getSettings().getColor("progressBarFuelColor");
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addTitle(getNameForTooltip());
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		
		float left = params.lastsDays - elapsed;
		if (left >= 1000) {
			tooltip.addPara("This particular field appears stable and is unlikely to drift apart any time soon.", pad);
		} else {
			String atLeastTime = Misc.getAtLeastStringForDays((int) left);
			tooltip.addPara("This particular field is unstable, but should not drift apart for " + atLeastTime + ".", pad);
		}
		
		
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
//		tooltip.addPara("Reduces the range at which stationary fleets inside it can be detected by %s.", nextPad,
//				highlight, 
//				"" + (int) ((1f - VISIBLITY_MULT) * 100) + "%"
//		);
		
		String stop = Global.getSettings().getControlStringForEnumName("GO_SLOW");
		tooltip.addPara("Reduces the range at which stationary or slow-moving* fleets inside it can be detected by %s.", nextPad,
				highlight, 
				"" + (int) ((1f - RingSystemTerrainPlugin.VISIBLITY_MULT) * 100) + "%"
		);
		tooltip.addPara("*Press and hold %s to stop; combine with holding the left mouse button down to move slowly.", nextPad,
				Misc.getGrayColor(), highlight, 
				stop
		);
		
		tooltip.addPara("Scavenging through the debris for anything useful is possible, but can be dangerous for the crew and equipment involved.", pad);
		
//		if (expanded) {
//			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
//			tooltip.addPara("Numerous small bodies that make up the ring system present on the battlefield. Not large enough to be an in-combat navigational hazard.", small);
//		}
	}
	
	public boolean isTooltipExpandable() {
		return true;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}
	
	public String getEffectCategory() {
		return "ringsystem-like";
	}
	
	public boolean hasAIFlag(Object flag) {
		return flag == TerrainAIFlags.HIDING_STATIONARY;
	}
	
	@Override
	public String getIconSpriteName() {
		return Global.getSettings().getSpriteName("terrain", "debrisFieldMapIcon");
	}

	public boolean isFadingOut() {
		return fadingOut;
	}

	public FaderUtil getExpander() {
		return expander;
	}

	public float getGlowDaysLeft() {
		return params.glowsDays - elapsed;
	}
	
	public float getPieceGlowProbability() {
		float glowLeft = getGlowDaysLeft();
		if (glowLeft <= 0) return 0;
		if (params.glowsDays <= 0) return 0;
		return glowLeft / params.glowsDays;
	}

	public float getDaysLeft() {
		return params.lastsDays - elapsed;
	}
	
	
}
