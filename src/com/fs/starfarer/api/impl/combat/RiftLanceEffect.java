package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc;

public class RiftLanceEffect implements BeamEffectPlugin { //WithReset {

	public static float MINE_SPAWN_CHANCE = 0f;
	
	protected boolean done = false;
//	public void reset() {
//		done = false;
//	}
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (beam.getBrightness() < 1f || done) return;
		float range = beam.getWeapon().getRange();
		float length = beam.getLengthPrevFrame();
	
		if (length > range - 10f || beam.getDamageTarget() != null) {
		//if (beam.getDamageTarget() != null) {
			done = true;
			
			Vector2f loc = beam.getRayEndPrevFrame();
			
			Color color = getColorForDarkening(beam.getFringeColor());
			Color undercolor = RiftCascadeEffect.EXPLOSION_UNDERCOLOR;
			float a = 1f;
			color = Misc.scaleAlpha(color, a);
			undercolor = Misc.scaleAlpha(undercolor, a);
			float size = 75f;
			size *= 0.5f;
			float dur = beam.getWeapon().getSpec().getBurstDuration() + beam.getWeapon().getSpec().getBeamChargedownTime();
			
			if ((float) Math.random() < MINE_SPAWN_CHANCE) {
				spawnMine(beam.getSource(), loc);
			} else {
				Vector2f vel = new Vector2f();
				if (beam.getDamageTarget() != null) {
					vel.set(beam.getDamageTarget().getVelocity());
				}
				spawnHitDarkening(color, undercolor, loc, vel, size, dur);
			}
		}
	}
	
	public void spawnHitDarkening(Color color, Color undercolor, Vector2f point, Vector2f vel, float size, float baseDuration) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (!engine.getViewport().isNearViewport(point, 100f + size * 2f)) return;
		
		Color c = color;
		
		
		for (int i = 0; i < 5; i++) {
			float dur = baseDuration + baseDuration * (float) Math.random();
			//float nSize = size * (1f + 0.0f * (float) Math.random());
			//float nSize = size * (0.75f + 0.5f * (float) Math.random());
			float nSize = size;
			Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
			Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
			v.scale(nSize + nSize * (float) Math.random() * 0.5f);
			v.scale(0.2f);
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
											0.5f / dur, 0f, dur, c);
		}
		
		float dur = baseDuration; 
		float rampUp = 0f;
		c = undercolor;
		for (int i = 0; i < 12; i++) {
			Vector2f loc = new Vector2f(point);
			loc = Misc.getPointWithinRadius(loc, size * 1f);
			float s = size * 3f * (0.5f + (float) Math.random() * 0.5f);
			engine.addNebulaParticle(loc, vel, s, 1.5f, rampUp, 0f, dur, c);
		}
	}
	
	public static Color getColorForDarkening(Color from) {
		Color c = new Color(255 - from.getRed(),
				255 - from.getGreen(),
				255 - from.getBlue(), 127);
		c = Misc.interpolateColor(c, Color.white, 0.4f);
		return c;
	}
	
	
	public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		
		//Vector2f currLoc = mineLoc;
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
															  "riftlance_minelayer", 
															  mineLoc, 
															  (float) Math.random() * 360f, null);
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.MISSILE, false, mine.getDamage());
//			float extraDamageMult = source.getMutableStats().getMissileWeaponDamageMult().getModifiedValue();
//			mine.getDamage().setMultiplier(mine.getDamage().getMultiplier() * extraDamageMult);
		}
		
		
		float fadeInTime = 0.05f;
		mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);
		
		//Global.getCombatEngine().addPlugin(createMissileJitterPlugin(mine, fadeInTime));
		
		//mine.setFlightTime((float) Math.random());
		float liveTime = 0f;
		//liveTime = 0.01f;
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		mine.addDamagedAlready(source);
		mine.setNoMineFFConcerns(true);
	}

}





