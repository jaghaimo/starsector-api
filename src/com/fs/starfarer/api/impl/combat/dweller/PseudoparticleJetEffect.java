package com.fs.starfarer.api.impl.combat.dweller;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.CryofluxTransducerEffect;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class PseudoparticleJetEffect extends CryofluxTransducerEffect {
	
	public static float PARTICLE_SCALE_MULT = 0.7f;
	
	protected IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);
	protected List<ParticleData> negativeParticles = new ArrayList<>();
	
	public PseudoparticleJetEffect() {
		super();
	}

	public PseudoparticleJetEffect(DamagingProjectileAPI projectile, DamagingProjectileAPI prev) {
		super(projectile, prev);
		
		int num = 2;
		num = 1;
		for (int i = 0; i < num; i++) {
			negativeParticles.add(new ParticleData(proj, this));
		}
		
		//PARTICLE_SCALE_MULT = 0.7f;
		
		float size = proj.getProjectileSpec().getWidth() * 0.6f;
		
		for (ParticleData p : negativeParticles) {
			p.sprite = Global.getSettings().getSprite(super.getParticleSpriteCat(), super.getParticleSpriteKey());
			float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			p.sprite.setTexWidth(0.25f);
			p.sprite.setTexHeight(0.25f);
			p.sprite.setTexX(i * 0.25f);
			p.sprite.setTexY(j * 0.25f);
			
			p.scale = 2f + (float) Math.random();
			p.scale *= 1.5f; // 1 particle instead of 2
			p.scale *= PARTICLE_SCALE_MULT;
			p.scaleIncreaseRate *= 1f + (float) Math.random();
			
			Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
			v.scale(size + size * (float) Math.random() * 0.5f);
			v.scale(0.67f * p.scale);
			p.vel.set(v);
			//Vector2f.add(vel, v, v);
			
//			p.fader.setDuration(0.25f, 0.75f);
//			p.fader.setBounceDown(true);
			
//			float f = index / Math.max(1f, (negativeParticles.size() - 1));
//			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
//			dir.scale(length * f);
//			Vector2f.add(p.offset, dir, p.offset);
			
			//p.offset = Misc.getPointWithinRadius(p.offset, width * 0.5f);
			p.offset = Misc.getPointWithinRadiusUniform(p.offset, size * 0.1f * p.scale, Misc.random);
			
			float offsetAngle = Misc.getAngleInDegrees(p.vel) + 180f;
			offsetAngle += 90f * ((float) Math.random() - 0.5f);
			p.offset = Misc.getUnitVectorAtDegreeAngle(offsetAngle);
			p.offset.scale(size * 1f * p.scale);
			
			//p.scale *= 1.5f;
			p.scale *= 0.7f;
			
			// don't apply scale increase to negative particles or they cover up too much
			p.scaleIncreaseRate /= getParticleScaleIncreaseRateMult();
			
			//p.scale = 0.25f + 0.75f * (1 - f);
		}
	}
	
	protected PseudoparticleJetEffect createTrail(DamagingProjectileAPI projectile, DamagingProjectileAPI prev) {
		return new PseudoparticleJetEffect(projectile, prev);
	}
	
	
	public void advance(float amount) {
		super.advance(amount);
		
		if (Global.getCombatEngine().isPaused()) return;
		
		for (ParticleData p : negativeParticles) {
			p.advance(amount);
		}
	}
	
	
	@Override
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		super.render(layer, viewport);
		//if (true) return;
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
	
		
		Color color = RiftLanceEffect.getColorForDarkening(proj.getProjectileSpec().getFringeColor());
		color = Misc.setAlpha(color, 100);
		
		float b = proj.getBrightness();
		b *= viewport.getAlphaMult();
		
		GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
		
		for (ParticleData p : negativeParticles) {
			float size = proj.getProjectileSpec().getWidth() * 0.6f;
			size *= p.scale;
			
			float alphaMult = 1f;
			Vector2f offset = p.offset;
			float diff = Misc.getAngleDiff(baseFacing, proj.getFacing());
			if (Math.abs(diff) > 0.1f) {
				offset = Misc.rotateAroundOrigin(offset, diff);
			}
			Vector2f loc = new Vector2f(x + offset.x, y + offset.y);
			
			p.sprite.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			p.sprite.setAngle(p.angle);
			p.sprite.setSize(size, size);
			p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
			p.sprite.setColor(color);
			p.sprite.renderAtCenter(loc.x, loc.y);
		}
		
		GL14.glBlendEquation(GL14.GL_FUNC_ADD);
	}
	
	
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		super.onHit(projectile, target, point, shieldHit, damageResult, engine);
		
		Color c = RiftLanceEffect.getColorForDarkening(projectile.getProjectileSpec().getFringeColor());
		c = Misc.setAlpha(c, 100);
		float baseDuration = 1f;
		float baseSize = projectile.getProjectileSpec().getLength() * 0.5f;
		float size = baseSize * 1.5f;
		float sizeMult = Misc.getHitGlowSize(100f, projectile.getDamage().getBaseDamage(), damageResult) / 100f;
		size *= sizeMult;
		
		size *= 1.5f;
		
		float extraDur = 0f;
		
		point = Misc.getPointWithinRadiusUniform(point, baseSize * 0.75f, Misc.random);
		float dur = baseDuration + baseDuration * (float) Math.random();
		dur += extraDur;
		float nSize = size;
		Vector2f pt = point;
		
		Vector2f vel = new Vector2f();
		if (target instanceof ShipAPI) {
			vel.set(target.getVelocity());
		}
		
		Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		v.scale(nSize + nSize * (float) Math.random() * 0.5f);
		v.scale(0.5f);
		Vector2f.add(vel, v, v);
		
		float maxSpeed = nSize * 1.5f * 0.2f; 
		float minSpeed = nSize * 1f * 0.2f; 
		float overMin = v.length() - minSpeed;
		if (overMin > 0) {
			float durMult = 1f - overMin / (maxSpeed - minSpeed);
			if (durMult < 0.1f) durMult = 0.1f;
			dur *= 0.5f + 0.5f * durMult;
		}
		
		engine.addNegativeNebulaParticle(pt, v, nSize * 1f, 2f,
										0.25f / dur, 0f, dur, c);
	}
	

	protected String getParticleSpriteCat() {
		return "dweller";
	}
	protected String getParticleSpriteKey() {
		return "dweller_pieces";
	}
	protected float getParticleScale() {
		return 2.5f * PARTICLE_SCALE_MULT;
	}
	protected float getParticleScaleIncreaseRateMult() {
		return 1.5f;
	}
	protected int getNumParticles() {
		return 2;
	}
	public Color getParticleColor() {
		Color color = proj.getProjectileSpec().getFringeColor();
		color = Misc.setAlpha(color, 75);
		return color;
	}

	protected String getLoopId() {
		return "pseudoparticle_jet_loop";
	}
	protected float getThresholdDist() {
		//return 50f;
		return super.getThresholdDist();
	}
	
	protected void playImpactSound(ApplyDamageResultAPI damageResult, Vector2f point, Vector2f vel) {
		Misc.playSound(damageResult, point, vel,
				"pseudoparticle_jet_hit_shield_light",
				"pseudoparticle_jet_hit_shield_solid",
				"pseudoparticle_jet_hit_shield_heavy",
				"pseudoparticle_jet_hit_light",
				"pseudoparticle_jet_hit_solid",
				"pseudoparticle_jet_hit_heavy");
	}
}
