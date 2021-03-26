package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class CombatEntityPluginWithParticles extends BaseCombatLayeredRenderingPlugin {
	
	public static class ParticleData {
		public SpriteAPI sprite;
		public Vector2f offset = new Vector2f();
		public Vector2f vel = new Vector2f();
		public float scale = 1f;
		public float scaleIncreaseRate = 1f;
		public float turnDir = 1f;
		public float angle = 1f;
		
		public float maxDur;
		public FaderUtil fader;
		public float elapsed = 0f;
		public float baseSize;
		protected Color color;
		
		public ParticleData(float baseSize, float durIn, float durOut, float endSizeMult, float maxDriftVel, float maxAngVel, Color color, String spriteSheetKey) {
			this.color = color;
			if (spriteSheetKey == null) {
				spriteSheetKey = "nebula_particles";
			}
			sprite = Global.getSettings().getSprite("misc", spriteSheetKey);
			//sprite = Global.getSettings().getSprite("misc", "dust_particles");
			float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			sprite.setTexWidth(0.25f);
			sprite.setTexHeight(0.25f);
			sprite.setTexX(i * 0.25f);
			sprite.setTexY(j * 0.25f);
			sprite.setAdditiveBlend();
			
			angle = (float) Math.random() * 360f;
			
			this.maxDur = durIn + durOut;
			scaleIncreaseRate = endSizeMult / maxDur;
			if (endSizeMult < 1f) {
				scaleIncreaseRate = -1f * endSizeMult;
			}
			scale = 1f;
			
			this.baseSize = baseSize;
			turnDir = Math.signum((float) Math.random() - 0.5f) * maxAngVel * (float) Math.random();
			//turnDir = 0f;
			
			float driftDir = (float) Math.random() * 360f;
			vel = Misc.getUnitVectorAtDegreeAngle(driftDir);
			//vel.scale(proj.getProjectileSpec().getLength() / maxDur * (0f + (float) Math.random() * 3f));
			vel.scale(maxDriftVel * (0.5f + (float) Math.random() * 0.5f));
			
			fader = new FaderUtil(0f, durIn, durOut);
			fader.setBounceDown(true);
			fader.forceOut();
			fader.fadeIn();
		}
		
		public void advance(float amount) {
			scale += scaleIncreaseRate * amount;
			
			offset.x += vel.x * amount;
			offset.y += vel.y * amount;
				
			angle += turnDir * amount;
			
			elapsed += amount;
//			if (maxDur - elapsed <= fader.getDurationOut() + 0.1f) {
//				fader.fadeOut();
//			}
			fader.advance(amount);
		}
	}
	
	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	protected List<ParticleData> darkParticles = new ArrayList<ParticleData>();
	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
	protected CombatEngineLayers normalLayer;
	protected CombatEngineLayers darkLayer;
	
	public CombatEntityPluginWithParticles() {
		this(CombatEngineLayers.ABOVE_PARTICLES_LOWER, CombatEngineLayers.ABOVE_PARTICLES);
	}
	public CombatEntityPluginWithParticles(CombatEngineLayers normalLayer, CombatEngineLayers darkLayer) {
		this.normalLayer = normalLayer;
		this.darkLayer = darkLayer;
		layers = EnumSet.of(normalLayer, darkLayer);
	}
	
	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}

	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}
	
	protected ParticleData prev;

	protected String spriteSheetKey;
	protected String darkSpriteSheetKey;
	public String getSpriteSheetKey() {
		return spriteSheetKey;
	}
	public void setSpriteSheetKey(String spriteSheetKey) {
		this.spriteSheetKey = spriteSheetKey;
	}
	public String getDarkSpriteSheetKey() {
		return darkSpriteSheetKey;
	}
	public void setDarkSpriteSheetKey(String darkSpriteSheetKey) {
		this.darkSpriteSheetKey = darkSpriteSheetKey;
	}
	
	public ParticleData addParticle(float baseSize, float durIn, float durOut, float endSizeMult, float maxDriftVel, float maxAngVel, Color color) {
		ParticleData p = new ParticleData(baseSize, durIn, durOut, endSizeMult, maxDriftVel, maxAngVel, color, spriteSheetKey);
		particles.add(p);
		prev = p;
		return p;
	}
	public ParticleData addDarkParticle(float baseSize, float durIn, float durOut, float endSizeMult, float maxDriftVel, float maxAngVel, Color color) {
		ParticleData p = new ParticleData(baseSize, durIn, durOut, endSizeMult, maxDriftVel, maxAngVel, color, darkSpriteSheetKey);
		darkParticles.add(p);
		prev = p;
		return p;
	}
	
	public void randomizePrevParticleLocation(float maxOffset) {
		Vector2f loc = Misc.getPointWithinRadius(prev.offset, maxOffset);
		prev.offset.set(loc);
	}
	
	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		
		List<ParticleData> remove = new ArrayList<ParticleData>();
		for (ParticleData p : particles) {
			p.advance(amount);
			if (p.elapsed >= p.maxDur) {
				remove.add(p);
			}
		}
		particles.removeAll(remove);
		
		remove = new ArrayList<ParticleData>();
		for (ParticleData p : darkParticles) {
			p.advance(amount);
			if (p.elapsed >= p.maxDur) {
				remove.add(p);
			}
		}
		darkParticles.removeAll(remove);
	}


	public boolean isExpired() {
		return particles.isEmpty() && darkParticles.isEmpty();
	}
	
	protected float getGlobalAlphaMult() {
		return 1f;
	}

	protected Float baseFacing = null;
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		render(layer, viewport, null);
	}
	
	public void render(CombatEngineLayers layer, ViewportAPI viewport, DamagingProjectileAPI proj) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		
		float b = viewport.getAlphaMult();
		
		if (proj != null && baseFacing == null) {
			baseFacing = proj.getFacing();
		}
		
		if (layer == normalLayer) {
			for (ParticleData p : particles) {
				float size = p.baseSize * p.scale;
				
				Vector2f offset = p.offset;
				float diff = 0f;
				if (baseFacing != null && proj != null) {
					diff = Misc.getAngleDiff(baseFacing, proj.getFacing());
					if (Math.abs(diff) > 0.1f) {
						offset = Misc.rotateAroundOrigin(offset, diff);
					}
				}
				Vector2f loc = new Vector2f(x + offset.x, y + offset.y);
				
				float alphaMult = getGlobalAlphaMult();
				
				p.sprite.setAngle(p.angle);
				p.sprite.setSize(size, size);
				p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
				p.sprite.setColor(p.color);
				p.sprite.renderAtCenter(loc.x, loc.y);
			}
		} else if (layer == darkLayer) {
			GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
			
			for (ParticleData p : darkParticles) {
				//float size = proj.getProjectileSpec().getWidth() * 0.6f;
				float size = p.baseSize * p.scale;
				
				Vector2f offset = p.offset;
				float diff = 0f;
				if (baseFacing != null && proj != null) {
					diff = Misc.getAngleDiff(baseFacing, proj.getFacing());
					if (Math.abs(diff) > 0.1f) {
						offset = Misc.rotateAroundOrigin(offset, diff);
					}
				}
				Vector2f loc = new Vector2f(x + offset.x, y + offset.y);
				
				float alphaMult = getGlobalAlphaMult();
				
				p.sprite.setAngle(p.angle);
				p.sprite.setSize(size, size);
				p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
				p.sprite.setColor(p.color);
				p.sprite.renderAtCenter(loc.x, loc.y);
			}
			
			GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		}
	}

}




