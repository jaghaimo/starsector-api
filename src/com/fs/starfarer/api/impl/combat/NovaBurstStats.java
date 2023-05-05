package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

/**
 * Copyright 2022 Fractal Softworks, LLC
 */
public class NovaBurstStats extends OrionDeviceStats {

	public NovaBurstStats() {
		p = new OrionDeviceParams();
		
		p.bombWeaponId = "nb_bomblauncher";
		p.shapedExplosionColor = new Color(100,165,255,155);
		p.shapedExplosionColor = new Color(100,165,255,85);
		p.shapedExplosionScatter = 0f;
		
		p.shapedExplosionNumParticles = 200;
		p.shapedExplosionOffset = 20f;
		p.shapedExplosionEndSizeMin = 1.5f;
		p.shapedExplosionEndSizeMax = 2f;
		p.shapedExplosionMinParticleSize = 50;
		p.shapedExplosionMaxParticleSize = 80;
		p.shapedExplosionMinParticleVel = 100;
		p.shapedExplosionMaxParticleVel = 500f;
		p.shapedExplosionArc = 270f;
		p.shapedExplosionArc = 60f;
		
		p.jitterColor = new Color(100,165,255,35);
		p.maxJitterDur = 1f;
		
		p.impactAccel = 10000f;
		p.impactRateMult = 1f;
		

		p.bombFadeInTime = 1f;
		p.bombLiveTime = 0f;
		p.bombSpeed = 0f;
	}
	
	

	@Override
	protected void advanceImpl(float amount, ShipAPI ship, State state, float effectLevel) {
		super.advanceImpl(amount, ship, state, effectLevel);

		if (effectLevel > 0) {
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.isDecorative() && w.getSpec().hasTag(Tags.NOVA)) {
					w.setForceFireOneFrame(true);
					w.forceShowBeamGlow();
				}
			}
		}
	}



	@Override
	protected void notifySpawnedExplosionParticles(Vector2f bombLoc) {
		Color c = new Color(100,165,255,255);
		float expSize = 800f;
		float durFringe = 1f;
		float durFringe2 = 0.75f;
		float dur = 0.75f;
//		durFringe2 = 1;
//		dur = 1;
		Global.getCombatEngine().addHitParticle(bombLoc, new Vector2f(), expSize, 1f, durFringe, c);
		Global.getCombatEngine().addHitParticle(bombLoc, new Vector2f(), expSize * 0.67f, 1f, durFringe2, c);
		Global.getCombatEngine().addHitParticle(bombLoc, new Vector2f(), expSize * 0.33f, 1f, dur, Color.white);
		
	}
	
	
	
}












