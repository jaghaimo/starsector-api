package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

public class GenericCampaignEntitySprite {

	//public static final float CELL_SIZE = 32;
	
	protected SpriteAPI shadowMask;
	protected SpriteAPI sprite;
	protected SpriteAPI glow;
	protected SpriteAPI overlay;
	private float overlayAngleOffset = 0f;
	
	private float scale;

	
	protected SectorEntityToken entity;
	
	public GenericCampaignEntitySprite(SectorEntityToken entity, String spriteName, float scale) {
		this.entity = entity;
		this.scale = scale;
		sprite = Global.getSettings().getSprite(spriteName);

		float width = sprite.getWidth() * scale;
		float height = sprite.getHeight() * scale;
		sprite.setSize(width, height);
		
		shadowMask = Global.getSettings().getSprite("graphics/fx/ship_shadow_mask.png");
		float max = Math.max(width, height);
		shadowMask.setSize(max * 1.5f, max * 1.5f);
	}
	
	public SpriteAPI getGlow() {
		return glow;
	}

	public void setGlow(SpriteAPI glow) {
		this.glow = glow;
	}

	public SpriteAPI getOverlay() {
		return overlay;
	}

	public void setOverlay(SpriteAPI overlay) {
		this.overlay = overlay;
	}
	
	public float getOverlayAngleOffset() {
		return overlayAngleOffset;
	}

	public void setOverlayAngleOffset(float overlayAngleOffset) {
		this.overlayAngleOffset = overlayAngleOffset;
	}

	public void render(float cx, float cy, float facing, float alphaMult) {
		if (alphaMult <= 0) return;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(entity.getLocation().x, entity.getLocation().y, 0);
		
		SectorEntityToken lightSource = entity.getLightSource();
		
		if (lightSource != null && entity.getLightColor() != null) {
			sprite.setColor(entity.getLightColor());
			if (overlay != null) {
				overlay.setColor(entity.getLightColor());
			}
		} else {
			sprite.setColor(Color.white);
			if (overlay != null) {
				overlay.setColor(Color.white);
			}
		}
				
		sprite.setAngle(facing - 90f);
		sprite.setNormalBlend();
		sprite.setAlphaMult(alphaMult);
		sprite.renderAtCenter(cx, cy);
				
		if (glow != null) {
			glow.setAngle(facing - 90f);
			glow.setAdditiveBlend();
			glow.setAlphaMult(alphaMult);
			glow.renderAtCenter(cx, cy);
		}
		
		if (overlay != null) {
//			overlay.setAngle(facing - 90f);
//			overlay.setNormalBlend();
//			overlay.setAlphaMult(alphaMult);
//			overlay.renderAtCenter(cx, cy);
			
			float w = overlay.getWidth() * 1.41f;
			float h = w;
			
			// clear out destination alpha in area we care about
			GL11.glColorMask(false, false, false, true);
			Misc.renderQuadAlpha(0 - w/2f - 1f, 0 - h/2f - 1f, w + 2f, h + 2f, Misc.zeroColor, 0f);
			
			sprite.setBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
			sprite.renderAtCenter(cx, cy);
			
			overlay.setAlphaMult(alphaMult);
			overlay.setAngle(facing - 90f + overlayAngleOffset);
			overlay.setBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);
			overlay.renderAtCenter(cx, cy);
			
			GL11.glColorMask(true, true, true, false);
			overlay.setBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
			overlay.renderAtCenter(cx, cy);
		}
		
		if (lightSource != null && !entity.getLightSource().hasTag(Tags.AMBIENT_LS)) {
			float w = shadowMask.getWidth() * 1.41f;
			float h = w;
			
			// clear out destination alpha in area we care about
			GL11.glColorMask(false, false, false, true);
			Misc.renderQuadAlpha(0 - w/2f - 1f, 0 - h/2f - 1f, w + 2f, h + 2f, Misc.zeroColor, 0f);
			
			sprite.setBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
			sprite.renderAtCenter(cx, cy);
			
			float lightDir = Misc.getAngleInDegreesStrict(entity.getLocation(), lightSource.getLocation());
			shadowMask.setAlphaMult(alphaMult);
			shadowMask.setAngle(lightDir);
			shadowMask.setBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);
			shadowMask.renderAtCenter(cx, cy);
			
			GL11.glColorMask(true, true, true, false);
			shadowMask.setBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
			shadowMask.renderAtCenter(cx, cy);
		}
		
		GL11.glPopMatrix();
	}
	
	
}














