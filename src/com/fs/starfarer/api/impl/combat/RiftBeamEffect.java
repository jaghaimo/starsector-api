package com.fs.starfarer.api.impl.combat;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class RiftBeamEffect implements EveryFrameWeaponEffectPlugin {

	public static float TARGET_RANGE = 100f;
	public static float RIFT_RANGE = 50f;
	
	protected IntervalUtil interval = new IntervalUtil(0.8f, 1.2f);
	
	public RiftBeamEffect() {
		interval.setElapsed((float) Math.random() * interval.getIntervalDuration());
	}
	
	//public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		List<BeamAPI> beams = weapon.getBeams();
		if (beams.isEmpty()) return;
		BeamAPI beam = beams.get(0);
		if (beam.getBrightness() < 1f) return;
	
		interval.advance(amount * 2f);
		if (interval.intervalElapsed()) {
			if (beam.getLengthPrevFrame() < 10) return;
			
			Vector2f loc;
			CombatEntityAPI target = findTarget(beam, beam.getWeapon(), engine);
			if (target == null) {
				loc = pickNoTargetDest(beam, beam.getWeapon(), engine);
			} else {
				loc = target.getLocation();
			}
			
			Vector2f from = Misc.closestPointOnSegmentToPoint(beam.getFrom(), beam.getRayEndPrevFrame(), loc);
			Vector2f to = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(from, loc));
			//to.scale(Math.max(RIFT_RANGE * 0.5f, Math.min(Misc.getDistance(from, loc), RIFT_RANGE)));
			to.scale(Math.min(Misc.getDistance(from, loc), RIFT_RANGE));
			Vector2f.add(from, to, to);
			
			spawnMine(beam.getSource(), to);
//			float thickness = beam.getWidth();
//			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, null, to, null, thickness, beam.getFringeColor(), Color.white);
//			arc.setCoreWidthOverride(Math.max(20f, thickness * 0.67f));
			//Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 1f, arc.getLocation(), arc.getVelocity());
		}
	}
	
	public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		
		//Vector2f currLoc = mineLoc;
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
															  "riftbeam_minelayer", 
															  mineLoc, 
															  (float) Math.random() * 360f, null);
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.ENERGY, false, mine.getDamage());
		}
		
		
		float fadeInTime = 0.05f;
		mine.getVelocity().scale(0);
		mine.fadeOutThenIn(fadeInTime);
		
		float liveTime = 0f;
		//liveTime = 0.01f;
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		mine.addDamagedAlready(source);
		mine.setNoMineFFConcerns(true);
	}

	public Vector2f pickNoTargetDest(BeamAPI beam, WeaponAPI weapon, CombatEngineAPI engine) {
		Vector2f from = beam.getFrom();
		Vector2f to = beam.getRayEndPrevFrame();
		float length = beam.getLengthPrevFrame();
		
		float f = 0.25f + (float) Math.random() * 0.75f;
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(from, to));
		loc.scale(length * f);
		Vector2f.add(from, loc, loc);
		
		return Misc.getPointWithinRadius(loc, RIFT_RANGE);
	}
	
	public CombatEntityAPI findTarget(BeamAPI beam, WeaponAPI weapon, CombatEngineAPI engine) {
		Vector2f to = beam.getRayEndPrevFrame();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(to,
																			RIFT_RANGE * 2f, RIFT_RANGE * 2f);
		int owner = weapon.getShip().getOwner();
		WeightedRandomPicker<CombatEntityAPI> picker = new WeightedRandomPicker<CombatEntityAPI>();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			if (other instanceof ShipAPI) {
				ShipAPI ship = (ShipAPI) other;
				if (!ship.isFighter() && !ship.isDrone()) continue;
			}
			
			float radius = Misc.getTargetingRadius(to, other, false);
			Vector2f p = Misc.closestPointOnSegmentToPoint(beam.getFrom(), beam.getRayEndPrevFrame(), other.getLocation());
			float dist = Misc.getDistance(p, other.getLocation()) - radius;
			if (dist > TARGET_RANGE) continue;
			
			picker.add(other);
			
		}
		return picker.pick();
	}

}





