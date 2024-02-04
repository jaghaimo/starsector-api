package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * IMPORTANT: will be multiple instances of this, as this doubles as the every frame effect and the on fire effect (same instance)
 * But also as the visual for each individual shot (created via onFire, using the non-default constructor)
 */
public class RealityDisruptorChargeGlow extends CombatEntityPluginWithParticles {

	public static enum EMPArcHitType {
		SOURCE,
		DEST,
		DEST_NO_TARGET,
	}
	
	public static float ARC_RATE_MULT = 2f;
	
	//public static int MAX_ARC_RANGE = 300;
	public static int MAX_ARC_RANGE = 600;
	//public static int ARCS_ON_HIT = 15;
	
	public static float REPAIR_RATE_MULT = 0.5f;
	public static float REPAIR_RATE_DEBUFF_DUR = 10f;
	
	public static Color UNDERCOLOR = RiftCascadeEffect.EXPLOSION_UNDERCOLOR;
	public static Color RIFT_COLOR = RiftCascadeEffect.STANDARD_RIFT_COLOR;
	
	
	public static Object STATUS_KEY = new Object();
	
	
	public static class RDRepairRateDebuff implements AdvanceableListener {
		public static String DEBUFF_ID = "reality_disruptor_repair_debuff";
		
		public ShipAPI ship;
		public float dur = REPAIR_RATE_DEBUFF_DUR;
		public RDRepairRateDebuff(ShipAPI ship) {
			this.ship = ship;
			
			ship.getMutableStats().getCombatEngineRepairTimeMult().modifyMult(DEBUFF_ID, 1f/REPAIR_RATE_MULT);
			ship.getMutableStats().getCombatWeaponRepairTimeMult().modifyMult(DEBUFF_ID, 1f/REPAIR_RATE_MULT);
		}

		public void resetDur() {
			dur = REPAIR_RATE_DEBUFF_DUR;
		}
		
		public void advance(float amount) {
			dur -= amount;
			
			if (Global.getCurrentState() == GameState.COMBAT &&
					Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship) {
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY,
						Global.getSettings().getSpriteName("ui", "icon_tactical_reality_disruptor"),
						"REALITY DISRUPTOR", "SLOWER REPAIRS: " + (int)Math.max(1, Math.round(dur)) + " SEC", true);
			}
			
			if (dur <= 0) {
				ship.removeListener(this);
				ship.getMutableStats().getCombatEngineRepairTimeMult().unmodify(DEBUFF_ID);
				ship.getMutableStats().getCombatWeaponRepairTimeMult().unmodify(DEBUFF_ID);
			}
		}
	}
	
	
	
	protected WeaponAPI weapon;
	protected DamagingProjectileAPI proj;
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
	protected IntervalUtil arcInterval = new IntervalUtil(0.17f, 0.23f);
	protected float delay = 1f;
	
	public RealityDisruptorChargeGlow(WeaponAPI weapon) {
		super();
		this.weapon = weapon;
		arcInterval = new IntervalUtil(0.17f, 0.23f);
		delay = 0.5f;
		setSpriteSheetKey("fx_particles2");
	}
	
	public void attachToProjectile(DamagingProjectileAPI proj) {
		this.proj = proj;
	}
	
	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		if (proj != null) {
			entity.getLocation().set(proj.getLocation());
		} else {
			entity.getLocation().set(weapon.getFirePoint(0));
		}
		super.advance(amount);
		
		boolean keepSpawningParticles = isWeaponCharging(weapon) || 
					(proj != null && !isProjectileExpired(proj) && !proj.isFading());
		if (keepSpawningParticles) {
			interval.advance(amount);
			if (interval.intervalElapsed()) {
				addChargingParticles(weapon);
			}
		}
		
		if (proj != null && !isProjectileExpired(proj) && !proj.isFading()) {
			delay -= amount;
			if (delay <= 0) {
				arcInterval.advance(amount * ARC_RATE_MULT);
				if (arcInterval.intervalElapsed()) {
					spawnArc();
				}
			}
		}
		if (proj != null) {
			Global.getSoundPlayer().playLoop("realitydisruptor_loop", proj, 1f, 1f * proj.getBrightness(),
											 proj.getLocation(), proj.getVelocity());
		}
		
//		if (proj != null) {
//			proj.setFacing(proj.getFacing() + 30f * amount);
//		}
	}
	
	@Override
	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		// pass in proj as last argument to have particles rotate
		super.render(layer, viewport, null);
	}

	public boolean isExpired() {
		boolean keepSpawningParticles = isWeaponCharging(weapon) || 
					(proj != null && !isProjectileExpired(proj) && !proj.isFading());
		return super.isExpired() && (!keepSpawningParticles || (!weapon.getShip().isAlive() && proj == null));
	}

	
	public float getRenderRadius() {
		return 500f;
	}
	
	
	@Override
	protected float getGlobalAlphaMult() {
		if (proj != null && proj.isFading()) {
			return proj.getBrightness();
		}
		return super.getGlobalAlphaMult();
	}
	
	
	
	public void spawnArc() {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		float emp = proj.getEmpAmount();
		float dam = proj.getDamageAmount();
	
		CombatEntityAPI target = findTarget(proj, weapon, engine);
		float thickness = 20f;
		float coreWidthMult = 0.67f;
		Color color = weapon.getSpec().getGlowColor();
		//color = new Color(255,100,100,255);
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArc(proj.getSource(), proj.getLocation(), null,
					   target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "realitydisruptor_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(255,255,255,255)
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			
			spawnEMPParticles(EMPArcHitType.SOURCE, proj.getLocation(), null);
			spawnEMPParticles(EMPArcHitType.DEST, arc.getTargetLocation(), target);
			
			if (target instanceof ShipAPI) {
				ShipAPI s = (ShipAPI) target;
				List<RDRepairRateDebuff> listeners = s.getListeners(RDRepairRateDebuff.class);
				if (listeners.isEmpty()) {
					s.addListener(new RDRepairRateDebuff(s));
				} else {
					listeners.get(0).resetDur();
				}
			}
			
		} else {
			Vector2f from = new Vector2f(proj.getLocation());
			Vector2f to = pickNoTargetDest(proj, weapon, engine);
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, null, to, null, thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			Global.getSoundPlayer().playSound("realitydisruptor_emp_impact", 1f, 1f, to, new Vector2f());
			
			spawnEMPParticles(EMPArcHitType.SOURCE, from, null);
			spawnEMPParticles(EMPArcHitType.DEST_NO_TARGET, to, null);
		}
	}
	
	

	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = 200f;
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, range * 0.25f);
		return dir;
	}
	
	public CombatEntityAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = MAX_ARC_RANGE;
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					//!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isHulk()) continue;
				if (otherShip.isPhased()) continue;
				if (!otherShip.isTargetable()) continue;
			}
			if (other.getCollisionClass() == CollisionClass.NONE) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius - 50f;
			if (dist > range) continue;
			
			//float angleTo = Misc.getAngleInDegrees(from, other.getLocation());
			//float score = Misc.getAngleDiff(weapon.getCurrAngle(), angleTo);
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
		}
		return best;
	}
	
	public void addChargingParticles(WeaponAPI weapon) {
		//CombatEngineAPI engine = Global.getCombatEngine();
		Color color = RiftLanceEffect.getColorForDarkening(RIFT_COLOR);
		
//		float b = 1f;
//		color = Misc.scaleAlpha(color, b);
		//undercolor = Misc.scaleAlpha(undercolor, b);
		
		float size = 50f;
		float underSize = 75f;
		//underSize = 100f;
		
		float in = 0.25f;
		float out = 0.75f;
		
		out *= 3f;
		
		float velMult = 0.2f;
		
		if (isWeaponCharging(weapon)) {
			size *= 0.25f + weapon.getChargeLevel() * 0.75f;
		}
		
		addDarkParticle(size, in, out, 1f, size * 0.5f * velMult, 0f, color);
		randomizePrevParticleLocation(size * 0.33f);
		
		if (proj != null) {
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
			//size = 40f;
			if (proj.getElapsed() > 0.2f) {
				addDarkParticle(size, in, out, 1.5f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 0.6f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}
			if (proj.getElapsed() > 0.4f) {
				addDarkParticle(size * 1f, in, out, 1.3f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 1.2f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}
			if (proj.getElapsed() > 0.6f) {
				addDarkParticle(size * .8f, in, out, 1.1f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 1.6f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}
			
			if (proj.getElapsed() > 0.8f) {
				addDarkParticle(size * .8f, in, out, 1.1f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 2.0f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}
//			int num = (int) Math.round(proj.getElapsed() / 0.5f * 10f);
//			if (num > 15) num = 15;
//			for (int i = 0; i < num; i++) {
//				addDarkParticle(size, in, out, 1f, size * 0.5f, 0f, color);
//				Vector2f offset = new Vector2f(dir);
//				offset.scale(size * 0.1f * i);
//				Vector2f.add(prev.offset, offset, prev.offset);
//			}
		}
		
//		UNDERCOLOR = new Color(100, 0, 100, 100);
//		UNDERCOLOR = NSProjEffect.EXPLOSION_UNDERCOLOR;
		
		// defaults:
		//public static Color EXPLOSION_UNDERCOLOR = new Color(100, 0, 25, 100);
		//public static Color STANDARD_RIFT_COLOR = new Color(100,60,255,255);
		
		//"glowColor":[100,200,255,255], #ion cannon
//		RIFT_COLOR = new Color(100, 200, 255, 255);
		
//		UNDERCOLOR = NSProjEffect.EXPLOSION_UNDERCOLOR;
//		RIFT_COLOR = NSProjEffect.STANDARD_RIFT_COLOR;
		
//		UNDERCOLOR = new Color(255, 0, 25, 100);
//		UNDERCOLOR = new Color(100, 0, 25, 100);
		
		addParticle(underSize * 0.5f, in, out, 1.5f * 3f, 0f, 0f, UNDERCOLOR);
		randomizePrevParticleLocation(underSize * 0.67f);
		addParticle(underSize * 0.5f, in, out, 1.5f * 3f, 0f, 0f, UNDERCOLOR);
		randomizePrevParticleLocation(underSize * 0.67f);
		
//		float facing = weapon.getCurrAngle();
//		if (proj != null) facing = proj.getFacing();
//		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(facing + 210f * ((float) Math.random() - 0.5f));
//		dir.scale(underSize * 0.25f * (float) Math.random());
//		Vector2f.add(prev.offset, dir, prev.offset);
	}
	
	public void spawnEMPParticles(EMPArcHitType type, Vector2f point, CombatEntityAPI target) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Color color = RiftLanceEffect.getColorForDarkening(RIFT_COLOR);
		
		float size = 30f;
		float baseDuration = 1.5f;
		Vector2f vel = new Vector2f();
		int numNegative = 5;
		switch (type) {
		case DEST:
			size = 50f;
			vel.set(target.getVelocity());
			if (vel.length() > 100f) {
				vel.scale(100f / vel.length());
			}
			break;
		case DEST_NO_TARGET:
			break;
		case SOURCE:
			size = 40f;
			numNegative = 10;
			break;
		}
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
		//dir.negate();
		//numNegative = 0;
		for (int i = 0; i < numNegative; i++) {
			float dur = baseDuration + baseDuration * (float) Math.random();
			//float nSize = size * (1f + 0.0f * (float) Math.random());
			//float nSize = size * (0.75f + 0.5f * (float) Math.random());
			float nSize = size;
			if (type == EMPArcHitType.SOURCE) {
				nSize *= 1.5f;
			}
			Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
			Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
			v.scale(nSize + nSize * (float) Math.random() * 0.5f);
			v.scale(0.2f);
			
			float endSizeMult = 2f;
			if (type == EMPArcHitType.SOURCE) {
				pt = Misc.getPointWithinRadius(point, nSize * 0f);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 0.2f * i);
				Vector2f.add(pt, offset, pt);
				endSizeMult = 1.5f;
				v.scale(0.5f);
			}
			Vector2f.add(vel, v, v);
			
			float maxSpeed = nSize * 1.5f * 0.2f; 
			float minSpeed = nSize * 1f * 0.2f; 
			float overMin = v.length() - minSpeed;
			if (overMin > 0) {
				float durMult = 1f - overMin / (maxSpeed - minSpeed);
				if (durMult < 0.1f) durMult = 0.1f;
				dur *= 0.5f + 0.5f * durMult;
			}
			
//			if (type == EMPArcHitType.DEST || type == EMPArcHitType.DEST_NO_TARGET) {
//				v.set(0f, 0f);
//			}
			
			engine.addNegativeNebulaParticle(pt, v, nSize * 1f, endSizeMult,
			//engine.addNegativeSwirlyNebulaParticle(pt, v, nSize * 1f, endSizeMult,
											0.25f / dur, 0f, dur, color);
		}
		
		float dur = baseDuration; 
		float rampUp = 0.5f / dur;
		color = UNDERCOLOR;
		for (int i = 0; i < 7; i++) {
			Vector2f loc = new Vector2f(point);
			loc = Misc.getPointWithinRadius(loc, size * 1f);
			float s = size * 4f * (0.5f + (float) Math.random() * 0.5f);
			engine.addSwirlyNebulaParticle(loc, vel, s, 1.5f, rampUp, 0f, dur, color, false);
		}
	}
	

	public static boolean isProjectileExpired(DamagingProjectileAPI proj) {
		return proj.isExpired() || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj);
	}
	
	public static boolean isWeaponCharging(WeaponAPI weapon) {
		return weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
	}
}






