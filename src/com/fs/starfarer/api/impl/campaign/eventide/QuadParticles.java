package com.fs.starfarer.api.impl.campaign.eventide;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.Misc;

public class QuadParticles {
	
	public static class QuadParticleData {
		public Vector2f loc = new Vector2f();
		public Vector2f vel = new Vector2f();
		public Color color;
		public float size;
		public float elapsed;
		public float maxDur;
		public float fadeTime;
		public float floorMod = 0;
	}
	
	public List<QuadParticleData> particles = new ArrayList<QuadParticleData>();
	public float gravity = 0f;
	public float floor = -10000000f;
	public float maxFloorMod = 0f;
	public float floorFriction = 1f; // fraction of speed lost per second
	public boolean additiveBlend = false;
	
	public Color minColor = null;
	public Color maxColor = null;
	public float minDur, maxDur;
	public float minSize, maxSize;
	public float fadeTime = 0.5f;
	
	public void addParticle(float x, float y, float dx, float dy) {
		float size = (float) Math.random() * (maxSize - minSize) + minSize;
		addParticle(x, y, dx, dy, size);
	}
	public void addParticle(float x, float y, float dx, float dy, float size) {
		float r1 = (float) Math.random();
		int r = (int) Math.round(Misc.interpolate(minColor.getRed(), maxColor.getRed(), r1));
		if (r < 0) r = 0;
		if (r > 255) r = 255;
		
		r1 = (float) Math.random();
		int g = (int) Math.round(Misc.interpolate(minColor.getGreen(), maxColor.getGreen(), r1));
		if (g < 0) g = 0;
		if (g > 255) g = 255;
		
		r1 = (float) Math.random();
		int b = (int) Math.round(Misc.interpolate(minColor.getBlue(), maxColor.getBlue(), r1));
		if (b < 0) b = 0;
		if (b > 255) b = 255;
		
		r1 = (float) Math.random();
		int a = (int) Math.round(Misc.interpolate(minColor.getAlpha(), maxColor.getAlpha(), r1));
		if (a < 0) a = 0;
		if (a > 255) a = 255;
		
		addParticle(x, y, dx, dy, size, new Color(r, g, b, a));
	}
	
	public void addParticle(float x, float y, float dx, float dy, float size, Color color) {
		float dur = (float) Math.random() * (maxDur - minDur) + minDur;
		addParticle(x, y, dx, dy, size, color, dur, fadeTime);
	}
	public void addParticle(float x, float y, float dx, float dy, float size, Color color, float maxDur) {
		addParticle(x, y, dx, dy, size, color, maxDur, fadeTime);
	}
	public void addParticle(float x, float y, float dx, float dy, float size, Color color, float maxDur, float fadeTime) {
		QuadParticleData p = new QuadParticleData();
		p.loc.x = x;
		p.loc.y = y;
		p.vel.x = dx;
		p.vel.y = dy;
		p.size = size;
		p.color = color;
		p.maxDur = maxDur;
		p.fadeTime = fadeTime;
		p.floorMod = (float) Math.random() * maxFloorMod;
		particles.add(p);
	}
	
	public boolean isParticleOnFloor(QuadParticleData curr) {
		return curr.loc.y <= floor + curr.floorMod;
	}
	
	public void advance(float amount) {
		Iterator<QuadParticleData> iter = particles.iterator();
		while (iter.hasNext()) {
			QuadParticleData curr = iter.next();
			curr.elapsed += amount;
			
			if (gravity > 0) {
				curr.vel.y -= gravity * amount;
			}
			
			curr.loc.x += curr.vel.x * amount;
			curr.loc.y += curr.vel.y * amount;
			if (curr.loc.y < floor + curr.floorMod) {
				curr.loc.y = floor + curr.floorMod;
				
				if (floorFriction > 0) {
					curr.vel.scale(Math.max(0, (1f - floorFriction * amount)));
				}
			}
			
			if (curr.elapsed > curr.maxDur) {
				iter.remove();
			}
		}
	}
	
	public void render(float alphaMult, boolean onFloor) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		
		if (additiveBlend) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		} else {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		for (QuadParticleData p : particles) {
			if (isParticleOnFloor(p) != onFloor) continue;
			
			float a = alphaMult;
			float left = p.maxDur - p.elapsed;
			if (left < 0) left = 0;
			if (left < p.fadeTime) {
				a *= left / p.fadeTime;
			}
			Misc.renderQuad(p.loc.x - p.size/2f, p.loc.y - p.size/2f, p.size, p.size, p.color, a);
		}
	}
	

	public boolean isDone() {
		return particles.isEmpty();
	}
	

}














