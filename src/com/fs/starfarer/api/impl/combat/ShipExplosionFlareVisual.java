package com.fs.starfarer.api.impl.combat;

import java.util.EnumSet;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * Used for ship explosions when there's no whiteout.
 * 
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public class ShipExplosionFlareVisual extends BaseCombatLayeredRenderingPlugin {

	public static class ShipExplosionFlareParams implements Cloneable {
		public CombatEntityAPI attachedTo;
		public Color color;
		public float flareWidth;
		public float flareHeight;
		public float fadeIn = 0.25f;
		public float fadeOut = 2f;
		
		public ShipExplosionFlareParams() {
		}

		public ShipExplosionFlareParams(CombatEntityAPI attachedTo, Color color, float flareWidth, float flareHeight) {
			this.attachedTo = attachedTo;
			this.color = color;
			this.flareWidth = flareWidth;
			this.flareHeight = flareHeight;
		}
		
		@Override
		protected ShipExplosionFlareParams clone() {
			try {
				return (ShipExplosionFlareParams) super.clone();
			} catch (CloneNotSupportedException e) {
				return null; // should never happen
			}
		}
		
	}
	
	protected ShipExplosionFlareParams p;
	protected SpriteAPI sprite;
	protected FaderUtil fader;

	public ShipExplosionFlareVisual(ShipExplosionFlareParams p) {
		this.p = p;
		//fader = new FaderUtil(1f, 0f, 2f);
		//fader.fadeOut();
		fader = new FaderUtil(0f, p.fadeIn, p.fadeOut);
		fader.setBounceDown(true);
		fader.fadeIn();
		sprite = Global.getSettings().getSprite("graphics/fx/starburst_glow1.png");
	}
	
	public float getRenderRadius() {
		return Math.max(p.flareWidth, p.flareHeight) + 500f;
	}
	
	
	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
	}

	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		
		fader.advance(amount);
		
		if (entity != null && p.attachedTo != null) {
			if (p.attachedTo instanceof ShipAPI) {
				entity.getLocation().set(((ShipAPI)p.attachedTo).getShieldCenterEvenIfNoShield());
			} else {
				entity.getLocation().set(p.attachedTo.getLocation());
			}
		}
	}

	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}

	public boolean isExpired() {
		return fader.isFadedOut();
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
	
		float b = fader.getBrightness();
		if (fader.isFadingIn() && b > 0.01f) {
			b = (float) Math.sqrt(b);
		} else {
			b *= b;
		}
		float alphaMult = viewport.getAlphaMult();
		
		alphaMult *= b;
		
		float f = 0.5f + 0.5f * b;
		f = 1f;
		
		sprite.setColor(p.color);
		sprite.setSize(p.flareWidth * f, p.flareHeight * f);
		sprite.setAdditiveBlend();
		sprite.setAlphaMult(alphaMult);
		sprite.renderAtCenter(x, y);
		
		//f *= 0.75f;
		sprite.setColor(Misc.scaleAlpha(Color.white, 1f));
		sprite.setSize(p.flareWidth * f, p.flareHeight * f * 0.33f);
		sprite.setAdditiveBlend();
		sprite.setAlphaMult(alphaMult);
		sprite.renderAtCenter(x, y);
		
//		f = 0.5f + 0.5f * b;
//		sprite.setColor(p.color);
//		sprite.setSize(p.flareHeight * f, p.flareWidth * f * 0.5f);
//		sprite.setAdditiveBlend();
//		sprite.setAlphaMult(alphaMult);
//		sprite.renderAtCenter(x, y);
		
//		f *= 0.5f;
//		sprite.setColor(Misc.scaleAlpha(Color.white, b));
//		sprite.setSize(p.flareHeight * f, p.flareWidth * f * 0.5f);
//		sprite.setAdditiveBlend();
//		sprite.setAlphaMult(alphaMult);
//		sprite.renderAtCenter(x, y);
	}

	

}


