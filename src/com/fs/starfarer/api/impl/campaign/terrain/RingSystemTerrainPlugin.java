package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RingSystemTerrainPlugin extends BaseRingTerrain {
	public static final float VISIBLITY_MULT = 0.25f;
	
	public static float MAX_SNEAK_BURN_LEVEL = Global.getSettings().getFloat("maxSneakBurnLevel");
	
	
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		name = params.name;
		if (name == null) {
			name = "Ring System";
		}
	}
	
	private transient RingRenderer rr;
	public void renderOnMap(float factor, float alphaMult) {
		if (params == null) return;
		if (rr == null) {
			rr = new RingRenderer("systemMap", "map_ring");
		}
		//Color color = new Color(175, 175, 105, 255);
		Color color = Global.getSettings().getColor("ringSystemMapColor");
		if (entity.getOrbitFocus() instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) entity.getOrbitFocus();
			//color = Misc.interpolateColor(color, planet.getSpec().getIconColor(), 0.5f);
		}
		boolean spiral = params.bandWidthInEngine / 2f + 10f >= params.middleRadius;
		rr.render(entity.getLocation(),
				  params.middleRadius - params.bandWidthInEngine * 0.5f,
				  params.middleRadius + params.bandWidthInEngine * 0.5f,
				  color,
				  spiral, factor, alphaMult);
	}
	
	
	
	public void advance(float amount) {
		super.advance(amount);
	}
		

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		super.render(layer, viewport);
	}

	@Override
	public void applyEffect(SectorEntityToken entity, float days) {
		if (entity instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			if (fleet.getCurrBurnLevel() <= MAX_SNEAK_BURN_LEVEL) {
				fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1",
									"Hiding inside ring system", VISIBLITY_MULT, 
									fleet.getStats().getDetectedRangeMod());
			}
		}
	}

	public boolean hasTooltip() {
		return true;
	}

	private String nameForTooltip = null;
	public String getNameForTooltip() {
		if (nameForTooltip == null) return "Ring System";
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
				"" + (int) ((1f - VISIBLITY_MULT) * 100) + "%"
		);
		tooltip.addPara("*Press and hold %s to stop; combine with holding the left mouse button down to move slowly.", nextPad,
				Misc.getGrayColor(), highlight, 
				stop
		);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Numerous small bodies that make up the ring system present on the battlefield. Not large enough to be an in-combat navigational hazard.", small);
		}
		//tooltip.addPara("Does not stack with other similar terrain effects.", pad);
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
}
