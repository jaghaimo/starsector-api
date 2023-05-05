package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Used for ship explosions when there's no whiteout.
 * 
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public class ShockwaveVisual extends BaseCombatLayeredRenderingPlugin {

	public static class ShockwaveParams implements Cloneable {
		public Color color;
		public float radius;
		public Vector2f loc;
		
		public ShockwaveParams() {
		}

		public ShockwaveParams(Color color, float radius, Vector2f loc) {
			super();
			this.color = color;
			this.radius = radius;
			this.loc = loc;
		}
		
		@Override
		protected ShockwaveParams clone() {
			try {
				return (ShockwaveParams) super.clone();
			} catch (CloneNotSupportedException e) {
				return null; // should never happen
			}
		}
		
	}
	
	
	protected ShockwaveParams p;

	
	public static void spawnShockwave(ShockwaveParams params) {
		CombatEngineAPI engine = Global.getCombatEngine();

		float baseSize = params.radius * 0.08f;
		float maxParticleSize = baseSize * 2f;
		
		float fullArea = (float) (Math.PI * params.radius * params.radius);
		float particleArea = (float) (Math.PI * baseSize * baseSize);
		
		int count = (int) Math.round(fullArea / particleArea * 1f);
		count *= 2;
		if (count > 300) count = 300;
		
		float durMult = 2f;
		//durMult = params.durationMult;

		float maxSpeed = params.radius * 2f;
		
		Color negativeColor = new Color(0, 0, 0, 255);
		
		//baseSize *= 0.5f;
		for (int i = 0; i < count; i++) {
			float size = baseSize * (1f + (float) Math.random());
			
			Color randomColor = new Color(Misc.random.nextInt(256), 
						Misc.random.nextInt(256), Misc.random.nextInt(256), params.color.getAlpha());			
			Color adjustedColor = Misc.interpolateColor(params.color, randomColor, 0.2f);
			adjustedColor = params.color;
			//adjustedColor = Misc.setAlpha(adjustedColor, 50);
//			ParticleData data = new ParticleData(adjustedColor, size, 
//						(0.25f + (float) Math.random()) * 2f * durMult, 3f);
			
			float r = (float) Math.random();
			float dist = params.radius * 0.2f * (0.1f + r * 0.9f);
			float dir = (float) Math.random() * 360f;
			//data.setOffset(dir, dist, dist);
			Vector2f offset = Misc.getUnitVectorAtDegreeAngle(dir);
			//offset.scale(dist + (dist - dist) * (float) Math.random());
			offset.scale(dist);
			
//			dir = Misc.getAngleInDegrees(data.offset);
//			data.setVelocity(dir, baseSize * 0.25f, baseSize * 0.5f);
//			data.vel.scale(1f / durMult);
			
//			data.swImpact = (float) Math.random();
//			if (i > count / 2) data.swImpact = 1;
			
			Vector2f loc = Vector2f.add(params.loc, offset, new Vector2f());
			
			float speed = maxSpeed * (0.25f + 0.75f * (float) Math.random());
			Vector2f vel = Misc.getUnitVectorAtDegreeAngle(dir);
			vel.scale(speed);
			
			float rampUp = 0f;
			float dur = 1f;
			
			float mult = 0.33f;
			mult = 1f;
			mult = 0.5f;
			rampUp *= mult;
			dur *= mult;
			
			engine.addNebulaParticle(loc, vel, size, 3f, rampUp, 0f, dur, adjustedColor);	
			//engine.addNegativeNebulaParticle(loc, vel, size, 3f, rampUp, 0f, dur, negativeColor);	
			//engine.addNebulaSmokeParticle(loc, vel, size, 3f, rampUp, 0f, dur, negativeColor);	
		}
		
		
	}
	
	/*
	public ShockwaveVisual(ShockwaveParams p) {
		this.p = p;
	}
	
	public float getRenderRadius() {
		return p.radius + 500f;
	}
	
	
	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
	}

	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		
		
		CombatEngineAPI engine = Global.getCombatEngine();
		engine.addNebulaParticle(loc, entity.getVelocity(), s, 1.5f, rampUp, 0f, dur, c);
	}

	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}

	public boolean isExpired() {
		return false;
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
	
		float f = fader.getBrightness();
		float alphaMult = viewport.getAlphaMult();
		if (f < 0.5f) {
			alphaMult *= f * 2f;
		}
		
		float r = p.radius;
		float tSmall = p.thickness;
		
		if (fader.isFadingIn()) {
			r *= 0.75f + Math.sqrt(f) * 0.25f;
		} else {
			r *= 0.1f + 0.9f * f;
			tSmall = Math.min(r * 1f, p.thickness);
		}
		
//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		GL11.glScalef(6f, 6f, 1f);
//		x = y = 0;

		//GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
		if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {
			float a = 1f;
			renderAtmosphere(x, y, r, tSmall, alphaMult * a, segments, atmosphereTex, noise, p.color, true);
			renderAtmosphere(x, y, r - 2f, tSmall, alphaMult * a, segments, atmosphereTex, noise, p.color, true);
		} else if (layer == CombatEngineLayers.ABOVE_PARTICLES) {
			float circleAlpha = 1f;
			if (alphaMult < 0.5f) {
				circleAlpha = alphaMult * 2f;
			}
			float tCircleBorder = 1f;
			renderCircle(x, y, r, circleAlpha, segments, Color.black);
			renderAtmosphere(x, y, r, tCircleBorder, circleAlpha, segments, atmosphereTex, noise, Color.black, false);
		}
		//GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		
//		GL11.glPopMatrix();
	}

	
	private void renderCircle(float x, float y, float radius, float alphaMult, int segments, Color color) {
		if (fader.isFadingIn()) alphaMult = 1f;
		
		float startRad = (float) Math.toRadians(0);
		float endRad = (float) Math.toRadians(360);
		float spanRad = Misc.normalizeAngle(endRad - startRad);
		float anglePerSegment = spanRad / segments;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		GL11.glRotatef(0, 0, 0, 1);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float) color.getAlpha() * alphaMult));
		
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2f(0, 0);
		for (float i = 0; i < segments + 1; i++) {
			boolean last = i == segments;
			if (last) i = 0;
			float theta = anglePerSegment * i;
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);
			
			float m1 = 0.75f + 0.65f * noise[(int)i];
			if (p.noiseMag <= 0) {
				m1 = 1f;
			}
			
			float x1 = cos * radius * m1;
			float y1 = sin * radius * m1;
			
			GL11.glVertex2f(x1, y1);
			
			if (last) break;
		}
		
		
		GL11.glEnd();
		GL11.glPopMatrix();
		
	}
	
	
	private void renderAtmosphere(float x, float y, float radius, float thickness, float alphaMult, int segments, SpriteAPI tex, float [] noise, Color color, boolean additive) {
		
		float startRad = (float) Math.toRadians(0);
		float endRad = (float) Math.toRadians(360);
		float spanRad = Misc.normalizeAngle(endRad - startRad);
		float anglePerSegment = spanRad / segments;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		GL11.glRotatef(0, 0, 0, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		tex.bindTexture();

		GL11.glEnable(GL11.GL_BLEND);
		if (additive) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		} else {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float) color.getAlpha() * alphaMult));
		float texX = 0f;
		float incr = 1f / segments;
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		for (float i = 0; i < segments + 1; i++) {
			boolean last = i == segments;
			if (last) i = 0;
			float theta = anglePerSegment * i;
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);
			
			float m1 = 0.75f + 0.65f * noise[(int)i];
			float m2 = m1;
			if (p.noiseMag <= 0) {
				m1 = 1f;
				m2 = 1f;
			}
			
			float x1 = cos * radius * m1;
			float y1 = sin * radius * m1;
			float x2 = cos * (radius + thickness * m2);
			float y2 = sin * (radius + thickness * m2);
			
			GL11.glTexCoord2f(0.5f, 0.05f);
			GL11.glVertex2f(x1, y1);
			
			GL11.glTexCoord2f(0.5f, 0.95f);
			GL11.glVertex2f(x2, y2);
			
			texX += incr;
			if (last) break;
		}
		
		GL11.glEnd();
		GL11.glPopMatrix();
	}
	*/
}


