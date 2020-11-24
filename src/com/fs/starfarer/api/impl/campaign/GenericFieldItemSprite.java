package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class GenericFieldItemSprite {

	//public static final float CELL_SIZE = 32;
	
	protected SpriteAPI shadowMask;
	protected SpriteAPI sprite;
	protected SpriteAPI glow;
	
	protected float width;
	protected float height;

	protected SectorEntityToken entity;
	
	protected Vector2f loc = new Vector2f();
	protected Vector2f vel = new Vector2f();
	protected float timeLeft = 0f;
	protected float facing = 0f;
	protected float angVel = 0f;
	protected FaderUtil fader = new FaderUtil(0f, 0.2f, 0.2f); // days
	
//	protected FaderUtil glowBounce;
//	protected FaderUtil glowFader = new FaderUtil(0f, 0.1f, 0.1f);
	

	public GenericFieldItemSprite(SectorEntityToken entity, String category, String key, float cellSize, float size, float spawnRadius) {
		this.entity = entity;
		
//		glowBounce =  new FaderUtil(0f, 0.1f + (float) Math.random() * 0.1f, 0.1f + (float) Math.random() * 0.1f); // days
//		glowBounce.setBounce(true, true);
//		glowBounce.fadeIn();
		
//		if ((float) Math.random() < field.getPieceGlowProbability()) {
//			glowFader.fadeIn();
//		}
		
		sprite = Global.getSettings().getSprite(category, key);
		//glow = Global.getSettings().getSprite("terrain", "debrisFieldGlowSheet");
		
		
		//float sizeRangeMult = 0.5f + 0.5f * field.params.density;
		this.width = size;
		this.height = width;
		
		float w = sprite.getWidth();
		float h = sprite.getHeight();
		int cols = (int) (w / cellSize);
		int rows = (int) (h / cellSize);
		
		
		float cellX = (int) (Math.random() * cols); 
		float cellY = (int) (Math.random() * rows);
		
		float ctw = sprite.getTextureWidth() / (float) cols;
		float cth = sprite.getTextureHeight() / (float) rows;
		
		sprite.setTexX(cellX * ctw);
		sprite.setTexY(cellY * cth);
		sprite.setTexWidth(ctw);
		sprite.setTexHeight(cth);
		
		if (glow != null) {
			glow.setTexX(cellX * ctw);
			glow.setTexY(cellY * cth);
			glow.setTexWidth(ctw);
			glow.setTexHeight(cth);
			glow.setSize(width, height);
			//glow.setColor(field.getParams().glowColor);
		}
		
		sprite.setSize(width, height);
		
		//glow.setColor(new Color(255,165,100,255));
		
		shadowMask = Global.getSettings().getSprite("graphics/fx/ship_shadow_mask.png");
		shadowMask.setSize(width * 1.5f, height * 1.5f);
		
		fader.fadeIn();
		
		facing = (float) Math.random() * 360f;
		angVel = (float) Math.random() * 360f - 180f;
		
		float r = (float) Math.random();
		r = (float) Math.sqrt(r);
		float dist = r * spawnRadius;
		
		loc = Misc.getUnitVectorAtDegreeAngle((float)Math.random() * 360f);
		loc.scale(dist);
		
		vel = Misc.getUnitVectorAtDegreeAngle((float)Math.random() * 360f);
		
		vel = Misc.getPerp(loc);
		float off = 0.25f;
		vel.x += off - (float) Math.random() * 0.5f * off;
		vel.y += off - (float) Math.random() * 0.5f * off;
		if ((float) Math.random() > 0.5f) {
			vel.negate();
		}
		Misc.normalise(vel);
		
		float speed = 10f + (float) Math.random() * 10f;
		vel.scale(speed);
		
		timeLeft = 1f + (float) Math.random();
		
	}
	
	public void render(float alphaMult) {

		//alphaMult *= fader.getBrightness();
		if (alphaMult <= 0) return;
		
		SectorEntityToken lightSource = entity.getLightSource();
		if (lightSource != null && entity.getLightColor() != null) {
			sprite.setColor(entity.getLightColor());
		} else {
			sprite.setColor(Color.white);
		}
				
		sprite.setAngle(facing - 90);
		sprite.setNormalBlend();
		sprite.setAlphaMult(alphaMult * fader.getBrightness());
		sprite.renderAtCenter(loc.x, loc.y);
				
		if (lightSource != null && !entity.getLightSource().hasTag(Tags.AMBIENT_LS)) {
			float w = shadowMask.getWidth() * 1.41f;
			float h = w;
			
			// clear out destination alpha in area we care about
			GL11.glColorMask(false, false, false, true);
			GL11.glPushMatrix();
			GL11.glTranslatef(loc.x, loc.y, 0);
			Misc.renderQuadAlpha(0 - w/2f - 1f, 0 - h/2f - 1f, w + 2f, h + 2f, Misc.zeroColor, 0f);
			GL11.glPopMatrix();
			sprite.setBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
			sprite.renderAtCenter(loc.x, loc.y);
			
			float lightDir = Misc.getAngleInDegreesStrict(entity.getLocation(), lightSource.getLocation());
			shadowMask.setAlphaMult(alphaMult);
			shadowMask.setAngle(lightDir);
			shadowMask.setBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);
			shadowMask.renderAtCenter(loc.x, loc.y);
			
			GL11.glColorMask(true, true, true, false);
			shadowMask.setBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
			shadowMask.renderAtCenter(loc.x, loc.y);
		}
		
		
//		if (glow != null && glowFader.getBrightness() > 0) {
//			glow.setAngle(facing - 90);
//			glow.setAdditiveBlend();
//			glow.setAlphaMult(alphaMult * fader.getBrightness() * 
//									(0.5f + 0.5f * glowBounce.getBrightness()) *
//									glowFader.getBrightness());
//			glow.renderAtCenter(loc.x, loc.y);
//		}
	}
	
	
	public void advance(float days) {
		fader.advance(days);
//		glowBounce.advance(days);
//		glowFader.advance(days);
		
		facing += angVel * days;
		
		loc.x += vel.x * days;
		loc.y += vel.y * days;
		
		timeLeft -= days;
		if (timeLeft < 0) {
			fader.fadeOut();
		}
	}
	
	
	public boolean isDone() {
		return fader.isFadedOut();
	}

//	public FaderUtil getGlowFader() {
//		return glowFader;
//	}
	
	
}














