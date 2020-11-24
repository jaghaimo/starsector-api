package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class DebrisPiece {

	public static final float CELL_SIZE = 32;
	
	protected SpriteAPI shadowMask;
	protected SpriteAPI sprite;
	protected SpriteAPI glow;
	
	protected float width;
	protected float height;
	protected float radius;

	protected DebrisFieldTerrainPlugin field;
	
	protected Vector2f loc = new Vector2f();
	protected Vector2f vel = new Vector2f();
	protected float timeLeft = 0f;
	protected float facing = 0f;
	protected float angVel = 0f;
	protected FaderUtil fader = new FaderUtil(0f, 0.2f, 0.2f); // days
	
	protected FaderUtil glowBounce;
	protected FaderUtil glowFader = new FaderUtil(0f, 0.1f, 0.1f);
	
	//protected IntervalUtil indCheck = new IntervalUtil(0.0f, 0.2f);
	protected FaderUtil indFader = new FaderUtil(0f, 0.3f, 0.8f, false, true);

	//protected float indProb = 0.05f;
	
	public DebrisPiece(DebrisFieldTerrainPlugin field) {
		this.field = field;
		
		glowBounce =  new FaderUtil(0f, 0.1f + (float) Math.random() * 0.1f, 0.1f + (float) Math.random() * 0.1f); // days
		glowBounce.setBounce(true, true);
		glowBounce.fadeIn();
		
		if ((float) Math.random() < field.getPieceGlowProbability()) {
			glowFader.fadeIn();
		}
		
		sprite = Global.getSettings().getSprite("terrain", "debrisFieldSheet");
		glow = Global.getSettings().getSprite("terrain", "debrisFieldGlowSheet");
		
		
		float sizeRangeMult = 0.5f + 0.5f * field.params.density;
		this.width = field.getParams().minSize + 
					(float) Math.random() * sizeRangeMult * (field.getParams().maxSize - field.getParams().minSize);
		this.height = width;
		
		radius = width * 1.41f * 0.5f;
		
		float w = sprite.getWidth();
		float h = sprite.getHeight();
		int cols = (int) (w / CELL_SIZE);
		int rows = (int) (h / CELL_SIZE);
		
		
		float cellX = (int) (Math.random() * cols); 
		float cellY = (int) (Math.random() * rows);
		
		float ctw = sprite.getTextureWidth() / (float) cols;
		float cth = sprite.getTextureHeight() / (float) rows;
		
		sprite.setTexX(cellX * ctw);
		sprite.setTexY(cellY * cth);
		sprite.setTexWidth(ctw);
		sprite.setTexHeight(cth);
		
		glow.setTexX(cellX * ctw);
		glow.setTexY(cellY * cth);
		glow.setTexWidth(ctw);
		glow.setTexHeight(cth);
		
		sprite.setSize(width, height);
		glow.setSize(width, height);
		
		//glow.setColor(new Color(255,165,100,255));
		glow.setColor(field.getParams().glowColor);
		
		shadowMask = Global.getSettings().getSprite("graphics/fx/ship_shadow_mask.png");
		shadowMask.setSize(width * 1.5f, height * 1.5f);
		
		fader.fadeIn();
		
		facing = (float) Math.random() * 360f;
		angVel = (float) Math.random() * 360f - 180f;
		
		float spawnRadius = field.params.bandWidthInEngine * field.getExpander().getBrightness();
		//spawnRadius *= 0.75f;
		
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
		
		float speed = 20f + (float) Math.random() * 100f;
		vel.scale(speed);
		
		timeLeft = 1f + (float) Math.random();
//		if (Math.random() < indProb) {
//			indFader.fadeIn();
//		}
	}
	
	public void render(float alphaMult) {

		//alphaMult *= fader.getBrightness();
		if (alphaMult <= 0) return;
		
		SectorEntityToken entity = field.getEntity();
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
			
			float lightDir = Misc.getAngleInDegreesStrict(field.getEntity().getLocation(), lightSource.getLocation());
			shadowMask.setAlphaMult(alphaMult);
			shadowMask.setAngle(lightDir);
			shadowMask.setBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_ALPHA);
			shadowMask.renderAtCenter(loc.x, loc.y);
			
			GL11.glColorMask(true, true, true, false);
			shadowMask.setBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
			shadowMask.renderAtCenter(loc.x, loc.y);
		}
		
		
		if (glowFader.getBrightness() > 0) {
			glow.setAngle(facing - 90);
			glow.setAdditiveBlend();
			glow.setAlphaMult(alphaMult * fader.getBrightness() * 
									(0.5f + 0.5f * glowBounce.getBrightness()) *
									glowFader.getBrightness());
			glow.renderAtCenter(loc.x, loc.y);
		}
		
	}
	
	public void renderIndicator(float alphaMult) {
		
		if (indFader.isFadedOut()) return;
		alphaMult *= fader.getBrightness();
		
		if (alphaMult <= 0) return;
		
		
		
		GL11.glPushMatrix();
		GL11.glTranslatef(loc.x, loc.y, 0);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		
		float thickness = 1.5f;
		GL11.glLineWidth(thickness);
		
		GL11.glEnable(GL11.GL_BLEND);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		//Color color = Global.getSector().getPlayerFaction().getBaseUIColor();
		Color color = Global.getSector().getFaction(Factions.NEUTRAL).getBaseUIColor();
		
		float size = width + 5f;
		float half = size * 0.5f;
		float corner = size * 0.25f;
		float x = 0;
		float y = 0;
		
		float b = alphaMult;
		float f = indFader.getBrightness();
		float glow = 0;
		if (f < 0.1f) {
			b *= f / 0.1f;
		} else if (indFader.isFadingIn() && f < 0.4f) {
			float p = (f - 0.1f) / (0.4f - 0.1f);
			glow = (float) (Math.sin(p * (float) Math.PI * 6f - (float) Math.PI) + 1f) / 2f;
			if (glow < 0.5f) {
				glow = 0f;
			} else {
				glow = 1f;
			}
			glow *= 0.25f;
		}
		
		for (int i = 0; i < 2; i++) {
			if (i == 1) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				b = glow;
			}
			if (b <= 0) continue;
			
			GL11.glBegin(GL11.GL_LINE_STRIP);
			Misc.setColor(color, b);
			GL11.glVertex2f(x - half, y + half - corner);
			GL11.glVertex2f(x - half, y + half);
			GL11.glVertex2f(x - half + corner, y + half);
			GL11.glEnd();
			
			GL11.glBegin(GL11.GL_LINE_STRIP);
			Misc.setColor(color, b);
			GL11.glVertex2f(x + half, y + half - corner);
			GL11.glVertex2f(x + half, y + half);
			GL11.glVertex2f(x + half - corner, y + half);
			GL11.glEnd();
			
			GL11.glBegin(GL11.GL_LINE_STRIP);
			Misc.setColor(color, b);
			GL11.glVertex2f(x - half, y - half + corner);
			GL11.glVertex2f(x - half, y - half);
			GL11.glVertex2f(x - half + corner, y - half);
			GL11.glEnd();
			
			GL11.glBegin(GL11.GL_LINE_STRIP);
			Misc.setColor(color, b);
			GL11.glVertex2f(x + half, y - half + corner);
			GL11.glVertex2f(x + half, y - half);
			GL11.glVertex2f(x + half - corner, y - half);
			GL11.glEnd();
		}
		
		
		GL11.glPopMatrix();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		
	}
	
	
//	public void renderIndicatorOld(float alphaMult) {
//		
//		alphaMult *= fader.getBrightness();
//		alphaMult *= indFader.getBrightness();
//		
//		if (alphaMult <= 0) return;
//		
//		
//		float r = Math.max(10f, radius + 5f);
//		float lineLength = 10f;
//		float thickness = 2f;
//		
//		float spanRad = (float) Math.PI * 2f;
//		float arcLength = spanRad * radius;
//		
//		float numSegments = (float) Math.ceil(arcLength/lineLength);
//		if (numSegments < 16) numSegments = 16;
//		if ((int)numSegments % 4 != 0) {
//			numSegments = ((int) numSegments / 4) * 4 + 4;
//		}
//		
//		
//		float anglePerIter = spanRad / numSegments;
//		
//		GL11.glPushMatrix();
//		GL11.glTranslatef(loc.x, loc.y, 0);
//
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glEnable(GL11.GL_LINE_SMOOTH);
//		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
//		GL11.glLineWidth(thickness);
//		
//		GL11.glEnable(GL11.GL_BLEND);
//		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		
//		//Color color = Global.getSector().getPlayerFaction().getBaseUIColor();
//		Color color = Global.getSector().getFaction(Factions.NEUTRAL).getBaseUIColor();
//		
//		GL11.glBegin(GL11.GL_LINE_LOOP);
//		for (float i = 0; i < numSegments + 1; i++) {
//			float b = alphaMult;
//			Misc.setColor(color, b);
//			float theta = anglePerIter * i;
//			float cos = (float) Math.cos(theta);
//			float sin = (float) Math.sin(theta);
//			float x1 = cos * (r - thickness / 2f);
//			float y1 = sin * (r - thickness / 2f);
//			GL11.glVertex2f(x1, y1);
//		}
//		GL11.glEnd();
//		
//		
//		GL11.glPopMatrix();
//		
//		GL11.glDisable(GL11.GL_LINE_SMOOTH);
//		
//	}
	
	
	public void advance(float days) {
		fader.advance(days);
		glowBounce.advance(days);
		glowFader.advance(days);
		
		facing += angVel * days;
		
		loc.x += vel.x * days;
		loc.y += vel.y * days;
		
		timeLeft -= days;
		if (timeLeft < 0) {
			fader.fadeOut();
		}
		
//		indCheck.advance(days);
//		if (indCheck.intervalElapsed() && Math.random() < indProb) {
//			indFader.fadeIn();
//		}
		indFader.advance(days);
	}
	
	public void showIndicator() {
		if (indFader.isIdle()) {
			float in = 0.3f;
			float out = 0.3f + 0.7f * (float) Math.random();
			indFader.setDuration(in, out);
		}
		indFader.fadeIn();
	}
	
	public boolean hasIndicator() {
		return !indFader.isIdle();
	}
	
	
	public boolean isDone() {
		return fader.isFadedOut();
	}

	public FaderUtil getGlowFader() {
		return glowFader;
	}
	
	
}














