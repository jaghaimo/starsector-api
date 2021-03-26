package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FlickerUtilV2;
import com.fs.starfarer.api.util.Misc;

public class FusionLampEntityPlugin extends BaseCustomEntityPlugin {

	public static Color GLOW_COLOR = new Color(255,165,100,255);
	public static Color LIGHT_COLOR = new Color(255,165,100,255);
	
	public static String VOLATILES_SHORTAGE_KEY = "$core_volatilesShortage";
	
	public static String GLOW_COLOR_KEY = "$core_lampGlowColor";
	public static String LIGHT_COLOR_KEY = "$core_lampLightColor";
	public static float GLOW_FREQUENCY = 0.2f; // on/off cycles per second
	
	
	transient private SpriteAPI sprite;
	transient private SpriteAPI glow;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		//this.entity = entity;
		entity.setDetectionRangeDetailsOverrideMult(0.75f);
		readResolve();
	}
	
	Object readResolve() {
		//sprite = Global.getSettings().getSprite("campaignEntities", "fusion_lamp");
		glow = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
		return this;
	}
	
	protected float phase = 0f;
	protected FlickerUtilV2 flicker = new FlickerUtilV2();

	public void advance(float amount) {
		phase += amount * GLOW_FREQUENCY;
		while (phase > 1) phase --;
		
		flicker.advance(amount * 1f);
		
		SectorEntityToken focus = entity.getOrbitFocus();
		if (focus instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) focus;
			float lightAlpha = getLightAlpha();
			lightAlpha *= entity.getSensorFaderBrightness();
			lightAlpha *= entity.getSensorContactFaderBrightness();
			planet.setSecondLight(
					new Vector3f(entity.getLocation().x, entity.getLocation().y, entity.getCircularOrbitRadius() * 0.75f), 
					Misc.scaleColor(getLightColor(), lightAlpha));
		}
	}
	
	public float getFlickerBasedMult() {
		float shortage = entity.getMemoryWithoutUpdate().getFloat(VOLATILES_SHORTAGE_KEY);
		shortage *= 0.33f;
		if (shortage <= 0f) return 1f;
		
		//float f = (1f - shortage) + (shortage * flicker.getBrightness());
		float f = 1f - shortage * flicker.getBrightness();
		return f;
	}
	
	public float getGlowAlpha() {
		float glowAlpha = 0f;
		if (phase < 0.5f) glowAlpha = phase * 2f;
		if (phase >= 0.5f) glowAlpha = (1f - (phase - 0.5f) * 2f);
		glowAlpha = 0.75f + glowAlpha * 0.25f;
		glowAlpha *= getFlickerBasedMult();
		if (glowAlpha < 0) glowAlpha = 0;
		if (glowAlpha > 1) glowAlpha = 1;
		return glowAlpha;
	}
	public float getLightAlpha() {
		//if (true) return 0f;
		float lightAlpha = 0f;
		if (phase < 0.5f) lightAlpha = phase * 2f;
		if (phase >= 0.5f) lightAlpha = (1f - (phase - 0.5f) * 2f);
		lightAlpha = 0.5f + lightAlpha * 0.5f;
		lightAlpha *= getFlickerBasedMult();
		if (lightAlpha < 0) lightAlpha = 0;
		if (lightAlpha > 1) lightAlpha = 1;
		return lightAlpha;
	}
	
	public Color getGlowColor() {
		Color glowColor = GLOW_COLOR;
		if (entity.getMemoryWithoutUpdate().contains(GLOW_COLOR_KEY)) {
			glowColor = (Color) entity.getMemoryWithoutUpdate().get(GLOW_COLOR_KEY);
		}
		return glowColor;
	}
	public Color getLightColor() {
		Color lightColor = LIGHT_COLOR;
		if (entity.getMemoryWithoutUpdate().contains(LIGHT_COLOR_KEY)) {
			lightColor = (Color) entity.getMemoryWithoutUpdate().get(LIGHT_COLOR_KEY);
		}
		return lightColor;
	}

	public float getRenderRange() {
		return entity.getRadius() + 1200f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= entity.getSensorFaderBrightness();
		alphaMult *= entity.getSensorContactFaderBrightness();
		if (alphaMult <= 0) return;
		
		CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
		if (spec == null) return;
		
		float w = spec.getSpriteWidth();
		float h = spec.getSpriteHeight();
		
		Vector2f loc = entity.getLocation();
		
		if (sprite != null) {
			sprite.setAngle(entity.getFacing() - 90f);
			sprite.setSize(w, h);
			sprite.setAlphaMult(alphaMult);
			sprite.setNormalBlend();
			sprite.renderAtCenter(loc.x, loc.y);
		}
		
		
		float glowAlpha = getGlowAlpha();
		
		float glowAngle1 = (((phase * 1.3f) % 1) - 0.5f) * 12f;
		float glowAngle2 = (((phase * 1.9f) % 1) - 0.5f) * 12f;
		
		glow.setColor(getGlowColor());
		
		w = 600f;
		h = 600f;
		
		glow.setSize(w, h);
		glow.setAlphaMult(alphaMult * glowAlpha * 0.5f);
		glow.setAdditiveBlend();
		
		glow.renderAtCenter(loc.x, loc.y);
		
		for (int i = 0; i < 5; i++) {
			w *= 0.3f;
			h *= 0.3f;
			//glow.setSize(w * 0.1f, h * 0.1f);
			glow.setSize(w, h);
			glow.setAlphaMult(alphaMult * glowAlpha * 0.67f);
			glow.renderAtCenter(loc.x, loc.y);
		}
		
//		glow.setSize(w, h);
//		glow.setAlphaMult(alphaMult * glowAlpha);
//		glow.setAdditiveBlend();
//		
//		glow.setAngle(entity.getFacing() - 90f + glowAngle1);
//		glow.renderAtCenter(loc.x, loc.y);
//		
//		glow.setAngle(entity.getFacing() - 90f + glowAngle2);
//		glow.setAlphaMult(alphaMult * glowAlpha * 0.5f);
//		glow.renderAtCenter(loc.x, loc.y);
	}
	

//	@Override
//	public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
//		String post = "";
//		Color color = entity.getFaction().getBaseUIColor();
//		Color postColor = color;
//		if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.DESTROYED.getBeaconFlag())) {
//			post = " - Low";
//			postColor = Misc.getPositiveHighlightColor();
//		} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.SUPPRESSED.getBeaconFlag())) {
//			post = " - Medium";
//			postColor = Misc.getHighlightColor();
//		} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.RESURGENT.getBeaconFlag())) {
//			post = " - High";
//			postColor = Misc.getNegativeHighlightColor();
//		}
//		
//		tooltip.addPara(entity.getName() + post, 0f, color, postColor, post.replaceFirst(" - ", ""));
//	}
//
//	@Override
//	public boolean hasCustomMapTooltip() {
//		return true;
//	}
//	
//	@Override
//	public void appendToCampaignTooltip(TooltipMakerAPI tooltip, VisibilityLevel level) {
//		if (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS || 
//				level == VisibilityLevel.COMPOSITION_DETAILS) {
//			
//			String post = "";
//			Color color = Misc.getTextColor();
//			Color postColor = color;
//			if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.DESTROYED.getBeaconFlag())) {
//				post = "low";
//				postColor = Misc.getPositiveHighlightColor();
//			} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.SUPPRESSED.getBeaconFlag())) {
//				post = "medium";
//				postColor = Misc.getHighlightColor();
//			} else if (entity.getMemoryWithoutUpdate().getBoolean(RemnantSystemType.RESURGENT.getBeaconFlag())) {
//				post = "high";
//				postColor = Misc.getNegativeHighlightColor();
//			}
//			if (!post.isEmpty()) {
//				tooltip.setParaFontDefault();
//				tooltip.addPara(BaseIntelPlugin.BULLET + "Danger level: " + post, 10f, color, postColor, post);
//			}
//		}
//		
//	}
}









