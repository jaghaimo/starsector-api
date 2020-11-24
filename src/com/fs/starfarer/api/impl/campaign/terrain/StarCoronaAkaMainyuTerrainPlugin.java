package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class StarCoronaAkaMainyuTerrainPlugin extends StarCoronaTerrainPlugin {

	public static final float ARC = 100f;
	
	@Override
	public float getAuroraAlphaMultForAngle(float angle) {
		SectorEntityToken star = params.relatedEntity.getLightSource();
		if (star != null) {
			float toStar = Misc.getAngleInDegrees(params.relatedEntity.getLocation(), star.getLocation());
			float diff = Misc.getAngleDiff(toStar, angle);
			float max = ARC / 2f;
			if (diff < max) {
				return Math.max(0, 1f - diff / max);
			}
			return 0f;
		}
		
		return 1f;
	}

	@Override
	public Color getAuroraColorForAngle(float angle) {
		if (color == null) {
			if (params.relatedEntity instanceof PlanetAPI) {
				color = ((PlanetAPI)params.relatedEntity).getSpec().getAtmosphereColor();
				//color = Misc.interpolateColor(color, Color.white, 0.25f);
			} else {
				color = Color.white;
			}
			color = Misc.setAlpha(color, 155);
		}
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getColorForAngle(color, angle);
		}
		return super.getAuroraColorForAngle(angle);
	}

	@Override
	public boolean containsPoint(Vector2f point, float radius) {
		SectorEntityToken star = params.relatedEntity.getLightSource();
		if (star != null) {
			float toStar = Misc.getAngleInDegrees(params.relatedEntity.getLocation(), star.getLocation());
			if (!Misc.isInArc(toStar, ARC, params.relatedEntity.getLocation(), point)) {
				return false;
			}
		}
		return super.containsPoint(point, radius);
	}

	@Override
	public String getTerrainName() {
		return "Ion Storm";
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		tooltip.addTitle("Ion Storm");
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, small);
			nextPad = small;
		}
		tooltip.addPara("The intense heat and radiation reduce the combat readiness of " +
						"all ships in the magnetotail at a steady pace.", nextPad);
		tooltip.addPara("The ionized gas being ejected from the atmosphere makes the planet difficult to approach.", pad);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Reduces the peak performance time of ships and increases the rate of combat readiness degradation in protracted engagements.", small);
		}
	}

	
	
	
	
	
	
}
