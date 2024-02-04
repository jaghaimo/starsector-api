package com.fs.starfarer.api.util;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class CampaignEngineGlowIndividualEngine {

	protected float angle;
	protected float length;
	protected float glowSize;
	protected float width;
	protected float flameTexSpanMult = 1f;
	protected Vector2f offset;
	
	protected Color fringe;
	protected Color core;
	protected Color flameColor;
	protected FaderUtil fader = new FaderUtil(0f, 1f, 1f);
	
	protected FlickerUtilV2 flicker = new FlickerUtilV2();
	protected float texOffset;
	
	
	protected CampaignEngineGlowUtil main;
	
	transient protected SpriteAPI glow;
	transient protected SpriteAPI flame;
	
	public CampaignEngineGlowIndividualEngine(float angle, float length, float width, float glowSize, 
										Vector2f offset,
										CampaignEngineGlowUtil main) {
		this.angle = angle;
		this.length = length;
		this.glowSize = glowSize;
		this.width = width;
		this.offset = offset;
		this.main = main;
		
		fader.fadeIn();
		
		readResolve();
	}

	protected Object readResolve() {
		glow = Global.getSettings().getSprite("campaignEntities", "campaign_engine_glow");
		flame = Global.getSettings().getSprite("campaignEntities", "campaign_engine_flame");
//		flame = Global.getSettings().getSprite("graphics/fx/beam_chunky_fringe.png");
//		flame = Global.getSettings().getSprite("graphics/fx/beamfringe.png");
//		flame = Global.getSettings().getSprite("graphics/fx/beamfringec.png");
//		flame = Global.getSettings().getSprite("graphics/fx/engineglow32.png");
		return this;
	}
	
	public void advance(float amount) {
		fader.advance(amount);
		flicker.advance(amount * main.getFlickerRateMult().getCurr());
		
		texOffset -= amount * main.getTextureScrollMult().getCurr();
		while (texOffset < 0) texOffset += 1f;
	}
	

	public void render(Vector2f center, float facing, float alphaMult) {
		alphaMult *= fader.getBrightness();
		
		float f = main.getFlickerMult().getCurr();
		if (f > 0) {
			alphaMult *= (1f - flicker.getBrightness() * f);
			//alphaMult *= flicker.getBrightness() * f;
		}
		
		if (alphaMult <= 0) return;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(center.x, center.y, 0f);
		GL11.glRotatef(facing, 0, 0, 1);
		GL11.glTranslatef(offset.x, offset.y, 0f);
		
		
		renderFlame(alphaMult);
		renderGlow(alphaMult);
		
		
		GL11.glPopMatrix();
	}
	
	protected void renderFlame(float alphaMult) {
		float lengthMult = main.getLengthMult().getCurr();
		float widthMult = main.getWidthMult().getCurr();
		
		if (lengthMult <= 0f || widthMult <= 0f || width <= 0f || length <= 0f) return;
		
		flame.bindTexture();
		
		Color base = main.getFlameColor().getCurr();
		if (flameColor != null) base = flameColor;
		Color dim = Misc.setAlpha(base, 0);
		
		//System.out.println("FC red: " + base.getRed());
		
		float w = width * widthMult;
		float len = length * lengthMult; 
		
//		len *= 2f;
//		w *= 1.5f;
		
		float bit = 0.01f;
		float fadeInPortion = Math.min(w/2f, len/4f);
		float fadeInTex = fadeInPortion / len;
		float flameTexSpan = 1f;
		//flameTexSpan = len / flame.getWidth();
		
		flameTexSpan = len / w * 0.25f;
		//System.out.println("FTX: " + flameTexSpan);
		flameTexSpan *= flameTexSpanMult;
		//flameTexSpan *= 0.5f;
		//flameTexSpan *= 2f;
		
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		float num = 11f;
		//num = 6f;
		for (float f = 1f; f <= num; f++) {
			float b = alphaMult * f / num; 
			float wMult = 1f - ((f - 1f) / (num - 1f));
			float lMult = f / num;
			lMult = 0.5f + 0.5f * lMult;
			//lMult = (float) Math.sqrt(lMult);
			
			float texPlus = (f - 1f) * 1.0f / num;
			
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			Misc.setColor(dim, alphaMult * b);
			GL11.glTexCoord2f(texOffset + texPlus, 0 + bit);
			GL11.glVertex2f(0f, -w/2f * wMult);
			GL11.glTexCoord2f(texOffset + texPlus, 1 - bit);
			GL11.glVertex2f(0f, w/2f * wMult);
			
			Misc.setColor(base, alphaMult * b);
			GL11.glTexCoord2f(texOffset + fadeInTex + texPlus, 0 + bit);
			GL11.glVertex2f(-fadeInPortion * lMult, -w/2f * wMult);
			GL11.glTexCoord2f(texOffset + fadeInTex + texPlus, 1 - bit);
			GL11.glVertex2f(-fadeInPortion * lMult, w/2f * wMult);
			
			Misc.setColor(dim, alphaMult * b);
			GL11.glTexCoord2f(texOffset + flameTexSpan + texPlus, 0 + bit);
			GL11.glVertex2f(-len * lMult, -w/2f * wMult);
			GL11.glTexCoord2f(texOffset + flameTexSpan + texPlus, 1 - bit);
			GL11.glVertex2f(-len * lMult, w/2f * wMult);
			GL11.glEnd();
		}
		
	}
	
	
	protected void renderGlow(float alphaMult) {
		float w = glowSize;
		float h = glowSize;
		
		float glowScale = main.getGlowMult().getCurr();
		//glowScale = 0.1f;
		float fringeScale = glowScale * main.getGlowFringeMult().getCurr();
		float coreScale = glowScale * main.getGlowCoreMult().getCurr();
		
		
		if (glowScale <= 0f) return;
		
		Color fringeColor = main.getGlowColorFringe().getCurr();
		if (fringe != null) fringeColor = fringe;
		
		Color coreColor = main.getGlowColorCore().getCurr();
		if (core != null) coreColor = core;
		
		glow.setColor(fringeColor);
		glow.setAdditiveBlend();
		
		glow.setAlphaMult(alphaMult * 0.5f);
		glow.setSize(w * fringeScale, h * fringeScale);
		glow.renderAtCenter(0f, 0f);
		
		glow.setColor(coreColor);
		//for (int i = 0; i < 5; i++) {
		for (int i = 0; i < 2; i++) {
			w *= 0.3f;
			h *= 0.3f;
			glow.setSize(w * coreScale, h * coreScale);
			glow.setAlphaMult(alphaMult * 0.67f);
			glow.renderAtCenter(0f, 0f);
		}
	}
	

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public float getGlowSize() {
		return glowSize;
	}

	public void setGlowSize(float glowSize) {
		this.glowSize = glowSize;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public Vector2f getOffset() {
		return offset;
	}

	public void setOffset(Vector2f offset) {
		this.offset = offset;
	}

	public Color getFringe() {
		return fringe;
	}

	public void setFringe(Color fringe) {
		this.fringe = fringe;
	}

	public Color getCore() {
		return core;
	}

	public void setCore(Color core) {
		this.core = core;
	}

	public CampaignEngineGlowUtil getMain() {
		return main;
	}

	public void setMain(CampaignEngineGlowUtil main) {
		this.main = main;
	}

	public SpriteAPI getGlow() {
		return glow;
	}

	public void setGlow(SpriteAPI glow) {
		this.glow = glow;
	}

	public FaderUtil getFader() {
		return fader;
	}

	public Color getFlameColor() {
		return flameColor;
	}

	public void setFlameColor(Color flameColor) {
		this.flameColor = flameColor;
	}

	public float getFlameTexSpanMult() {
		return flameTexSpanMult;
	}

	public void setFlameTexSpanMult(float flameTexSpanMult) {
		this.flameTexSpanMult = flameTexSpanMult;
	}
	
	
}




