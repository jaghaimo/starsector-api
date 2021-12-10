package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class PhaseCloakStats extends BaseShipSystemScript {

	public static Color JITTER_COLOR = new Color(255,175,255,255);
	public static float JITTER_FADE_TIME = 0.5f;
	
	public static float SHIP_ALPHA_MULT = 0.25f;
	//public static float VULNERABLE_FRACTION = 0.875f;
	public static float VULNERABLE_FRACTION = 0f;
	public static float INCOMING_DAMAGE_MULT = 0.25f;
	
	
	public static float MAX_TIME_MULT = 3f;
	
//	/**
//	 * Top speed multiplier when at 100% disruption.  
//	 */
//	public static float DISRUPT_SPEED_MULT = 0.33f;
//	/**
//	 * Disruption clears up at this rate. Always clears up fully when cloak is turned off.
//	 */
//	public static float DISRUPT_DECAY_RATE = 0.25f;
//	/**
//	 * Seconds that need to elapse since a disruption increase before it starts to decay.
//	 */
//	public static float DISRUPT_DECAY_DELAY = 2f;
//	
//	// Compared to ship's max flux to figure out how quickly things disrupt.
//	public static float PROJECTILE_DAMAGE_MULT = 3f;
//	public static float BEAM_DAMAGE_MULT = 0.1f;
//	public static float MASS_DAMAGE_MULT = 1f;
	
	public static boolean FLUX_LEVEL_AFFECTS_SPEED = true;
	public static float MIN_SPEED_MULT = 0.33f;
	public static float BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f;
	
	protected Object STATUSKEY1 = new Object();
	protected Object STATUSKEY2 = new Object();
	protected Object STATUSKEY3 = new Object();
	protected Object STATUSKEY4 = new Object();
	
	
	public static float getMaxTimeMult(MutableShipStatsAPI stats) {
		return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
	}
	
	protected boolean isDisruptable(ShipSystemAPI cloak) {
		return cloak.getSpecAPI().hasTag(Tags.DISRUPTABLE);
	}
	
	protected float getDisruptionLevel(ShipAPI ship) {
		//return disruptionLevel;
		//if (true) return 0f;
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			float threshold = ship.getMutableStats().getDynamic().getMod(
					Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED);
			if (threshold <= 0) return 1f;
			float level = ship.getHardFluxLevel() / threshold;
			if (level > 1f) level = 1f;
			return level;
		}
		return 0f;
	}
	
	protected void maintainStatus(ShipAPI playerShip, State state, float effectLevel) {
		float level = effectLevel;
		float f = VULNERABLE_FRACTION;
		
		ShipSystemAPI cloak = playerShip.getPhaseCloak();
		if (cloak == null) cloak = playerShip.getSystem();
		if (cloak == null) return;
		
		if (level > f) {
//			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
//					cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "can not be hit", false);
			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
					cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "time flow altered", false);
		} else {
//			float INCOMING_DAMAGE_MULT = 0.25f;
//			float percent = (1f - INCOMING_DAMAGE_MULT) * getEffectLevel() * 100;
//			Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
//					spec.getIconSpriteName(), cloak.getDisplayName(), "damage mitigated by " + (int) percent + "%", false);
		}
		
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			if (level > f) {
				if (getDisruptionLevel(playerShip) <= 0f) {
					Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
							cloak.getSpecAPI().getIconSpriteName(), "phase coils stable", "top speed at 100%", false);
				} else {
					//String disruptPercent = "" + (int)Math.round((1f - disruptionLevel) * 100f) + "%";
					//String speedMultStr = Strings.X + Misc.getRoundedValue(getSpeedMult());
					String speedPercentStr = (int) Math.round(getSpeedMult(playerShip, effectLevel) * 100f) + "%";
					Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
							cloak.getSpecAPI().getIconSpriteName(),
							//"phase coils at " + disruptPercent, 
							"phase coil stress", 
							"top speed at " + speedPercentStr, true);
				}
			}
		}
	}
	
//	protected float disruptionLevel = 0f;
//	//protected Set<CombatEntityAPI> hitBy = new LinkedHashSet<CombatEntityAPI>();
//	protected TimeoutTracker<Object> hitBy = new TimeoutTracker<Object>();
//	protected float sinceHit = 1000f;
	
	public float getSpeedMult(ShipAPI ship, float effectLevel) {
		if (getDisruptionLevel(ship) <= 0f) return 1f;
		return MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(ship) * effectLevel); 
	}

	/*
	public void advanceDisrupt(float amount, ShipAPI ship, ShipSystemAPI cloak, 
				MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		
		sinceHit += amount;
		if (state == State.COOLDOWN || state == State.IDLE || state == State.OUT) {
			disruptionLevel = 0f;
			hitBy.clear();
		} else {
			checkForHits(ship, cloak);
		}
		hitBy.advance(amount);
		
		if (sinceHit > DISRUPT_DECAY_DELAY) {
			disruptionLevel -= DISRUPT_DECAY_RATE * amount;
			if (disruptionLevel < 0) disruptionLevel = 0;
			if (disruptionLevel > 1) disruptionLevel = 1;
		}
		
		((PhaseCloakSystemAPI)cloak).setMinCoilJitterLevel(disruptionLevel * 1f);
		
//		float mult = getSpeedMult(effectLevel);
//		if (mult < 1f) {
//			stats.getMaxSpeed().modifyMult(id, mult);
//		} else {
//			stats.getMaxSpeed().unmodifyMult(id);
//		}
	}
	
	public void checkForHits(ShipAPI ship, ShipSystemAPI cloak) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f loc = new Vector2f(ship.getLocation());
		float radius = ship.getCollisionRadius();
		
		float fluxCap = ship.getMaxFlux();
		if (fluxCap < 1000) fluxCap = 1000;
		
		Color core = cloak.getSpecAPI().getEffectColor1();
		core = Color.white;
		Color fringe = cloak.getSpecAPI().getEffectColor2();
		fringe = Misc.interpolateColor(fringe, Color.white, 0.5f);
//		fringe = Misc.setAlpha(fringe, 255);
		
		Iterator<Object> iter = engine.getAllObjectGrid().getCheckIterator(loc, radius * 2f, radius * 2f);
		while (iter.hasNext()) {
			Object curr = iter.next();
			if (!(curr instanceof CombatEntityAPI)) continue; 
			if (curr == ship) continue;
			
			CombatEntityAPI entity = (CombatEntityAPI) curr;
			if (hitBy.contains(entity)) continue;
			
			float tr = Misc.getTargetingRadius(entity.getLocation(), ship, false);
			tr *= 1.2f;
			float dist = Misc.getDistance(entity.getLocation(), loc);
			
			boolean hit = false;
			Vector2f glowLoc = null;
			float glowDir = 0f;
			float damage = 0f;
			float hitMult = 1f;
			float timeout = 1f;
			if (entity instanceof CombatAsteroidAPI) {
				if (dist < tr + entity.getCollisionRadius()) {
					hit = true;
					hitMult = 1f - dist / (tr + entity.getCollisionRadius());
					hitMult = 0.5f + 0.5f * hitMult;
					damage = entity.getMass() * MASS_DAMAGE_MULT * hitMult;
					timeout = 0.5f;
					
					glowLoc = entity.getLocation();
					float dirToEntity = Misc.getAngleInDegrees(loc, entity.getLocation());
					glowLoc = Misc.getUnitVectorAtDegreeAngle(dirToEntity);
					glowLoc.scale(tr);
					Vector2f.add(glowLoc, loc, glowLoc);
					glowDir = Misc.getAngleInDegrees(entity.getLocation(), loc);
				}
			} else if (entity instanceof ShipAPI) {
				if (dist < tr + entity.getCollisionRadius()) {
					hit = true;
					hitMult = 1f - dist / (tr + entity.getCollisionRadius());
					hitMult = 0.5f + 0.5f * hitMult;
					damage = entity.getMass() * MASS_DAMAGE_MULT * hitMult;
					timeout = 0.5f;
					
					glowLoc = entity.getLocation();
					float dirToEntity = Misc.getAngleInDegrees(loc, entity.getLocation());
					glowLoc = Misc.getUnitVectorAtDegreeAngle(dirToEntity);
					glowLoc.scale(tr);
					Vector2f.add(glowLoc, loc, glowLoc);
					glowDir = Misc.getAngleInDegrees(entity.getLocation(), loc);
				}
			} else if (entity instanceof DamagingProjectileAPI) {
				DamagingProjectileAPI proj = (DamagingProjectileAPI) entity;
				if (proj.getSource() == ship) continue;
				
				float check = tr + entity.getCollisionRadius();
				check = tr;
				if (dist < check) {
					hit = true;
					hitMult = 1f - dist / (check);
					hitMult = 0.5f + 0.5f * hitMult;
					damage = proj.getDamageAmount() * PROJECTILE_DAMAGE_MULT * hitMult;
					//damage *= proj.getDamageType().getShieldMult();
					damage += proj.getDamage().getFluxComponent() * hitMult;
					if (entity instanceof MissileAPI) {
						timeout = 0.5f;
					} else {
						timeout = 10f;
					}
					glowLoc = entity.getLocation();
					glowDir = Misc.getAngleInDegrees(entity.getVelocity());
				}
			}
			
			if (hit && damage > 0) {
				float disruptAmount = damage / fluxCap;
				disruptionLevel += disruptAmount;
				if (disruptionLevel > 1f) disruptionLevel = 1f;
				hitBy.add(entity, timeout);
				sinceHit = 0f;
				if (glowLoc != null) {
					//float size = 2000f * hitMult * Math.min(damage/2000f, 1f);
					//float size = 1000f * hitMult * Math.max(0.05f, disruptAmount + 0.1f); 
					float size = 1000f * hitMult * (disruptAmount + 0.05f); 
					if (size < 20) size = 20;
					if (size > 150) size = 150;
					
					//size *= 0.5f;
					Vector2f glow = new Vector2f(glowLoc);
//					Vector2f per = Misc.getUnitVectorAtDegreeAngle(glowDir);
//					per.scale(size * 0.1f);
//						engine.addHitParticle(glow, ship.getVelocity(), size, hitMult, 1f, fringe);
//						engine.addNegativeParticle(glow, ship.getVelocity(), size * 0.5f, 0f, 1f, core);
					Vector2f vel = new Vector2f(ship.getVelocity());
					Vector2f move = Misc.getUnitVectorAtDegreeAngle(glowDir);
					move.scale(Math.max(300f, entity.getVelocity().length() * (0.5f + (float) Math.random() * 0.5f)));
					move.scale(-0.1f);
					Vector2f.add(vel, move, vel);
					
					engine.addNebulaParticle(
							glow, vel, size,         1.5f, 0.3f, 0.5f, 1f, Misc.scaleAlpha(fringe, hitMult));
					engine.addNegativeNebulaParticle(
							glow, vel, size * 0.67f, 1.5f, 0.3f, 0.5f, 1f, core);
					if (entity instanceof DamagingProjectileAPI) {
						engine.removeEntity(entity);
					}
				}
			}
		}
		
		for (BeamAPI beam : engine.getBeams()) {
			if (beam.getDamage().getMultiplier() <= 0) continue;
			Vector2f p = Misc.closestPointOnSegmentToPoint(beam.getFrom(), beam.getTo(), loc);
			float tr = Misc.getTargetingRadius(p, ship, false); 
			//tr *= 1.2f;
			tr += 20f;
			float dist = Misc.getDistance(p, loc);
			if (dist < tr) {
				float hitMult = 1f - dist / tr;
				hitMult = 0.5f + 0.5f * hitMult;
				float damage = beam.getDamage().getDamage() * BEAM_DAMAGE_MULT * hitMult;
				//damage *= beam.getDamage().getType().getShieldMult();
				damage += beam.getDamage().getFluxComponent() * hitMult;
				float disruptAmount = damage / fluxCap;
				disruptionLevel += disruptAmount;
				if (disruptionLevel > 1f) disruptionLevel = 1f;
				sinceHit = 0f;
//				float dirToEntity = Misc.getAngleInDegrees(loc, p);
//				Vector2f glowLoc = Misc.getUnitVectorAtDegreeAngle(dirToEntity);
//				glowLoc.scale(tr);
//				Vector2f.add(glowLoc, loc, glowLoc);
				Vector2f glowLoc = Misc.intersectSegmentAndCircle(beam.getFrom(), beam.getTo(), loc, tr * 1.2f);
				if (glowLoc != null) {
					//beam.getTo().set(glowLoc);
					float size = 1000f * hitMult * (disruptAmount + 0.05f); 
					if (size < 20) size = 20;
					if (size > 150) size = 150;
					//size *= 0.5f;
//					engine.addHitParticle(glowLoc, ship.getVelocity(), size, hitMult, 0.3f, fringe);
//					//engine.addHitParticle(glowLoc, ship.getVelocity(), size * 0.25f, hitMult, 0.1f, core);
//					engine.addNegativeParticle(glowLoc, ship.getVelocity(), size * 0.5f, 0f, 0.3f, core);
					float glowDir = Misc.getAngleInDegrees(beam.getTo(), beam.getFrom());
					glowDir += (float) Math.random() * 180f - 90f;
					Vector2f move = Misc.getUnitVectorAtDegreeAngle(glowDir);
					move.scale(500f * (0.5f + (float) Math.random() * 0.5f));
					move.scale(0.2f);
					Vector2f vel = new Vector2f();
					Vector2f.add(vel, move, vel);
					
					engine.addNebulaParticle(
							glowLoc, vel, size,         1.5f, 0.3f, 0.5f, 0.5f, Misc.scaleAlpha(fringe, hitMult));
					engine.addNegativeNebulaParticle(
							glowLoc, vel, size * 0.67f, 1.5f, 0.3f, 0.5f, 0.5f, core);
				}
			}
		}
	}
	*/
	
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		
//		if (effectLevel > 0) {
//			stats.getMaxSpeed().modifyMult(id, 0.3333333f);
//		} else {
//			stats.getMaxSpeed().unmodifyMult(id);
//		}
		
		if (player) {
			maintainStatus(ship, state, effectLevel);
		}
		
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		
		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null) cloak = ship.getSystem();
		if (cloak == null) return;
		
//		if (isDisruptable(cloak)) {
//			advanceDisrupt(Global.getCombatEngine().getElapsedInLastFrame(),
//					ship, cloak, stats, id, state, effectLevel);
//		}
		
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			if (state == State.ACTIVE || state == State.OUT || state == State.IN) {
				float mult = getSpeedMult(ship, effectLevel);
				if (mult < 1f) {
					stats.getMaxSpeed().modifyMult(id + "_2", mult);
				} else {
					stats.getMaxSpeed().unmodifyMult(id + "_2");
				}
				((PhaseCloakSystemAPI)cloak).setMinCoilJitterLevel(getDisruptionLevel(ship));
			}
		}
		
		if (state == State.COOLDOWN || state == State.IDLE) {
			unapply(stats, id);
			return;
		}
		
		float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
		float accelPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f);
		stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);
		stats.getAcceleration().modifyPercent(id, accelPercentMod * effectLevel);
		stats.getDeceleration().modifyPercent(id, accelPercentMod * effectLevel);
		
		float speedMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult();
		float accelMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult();
		stats.getMaxSpeed().modifyMult(id, speedMultMod * effectLevel);
		stats.getAcceleration().modifyMult(id, accelMultMod * effectLevel);
		stats.getDeceleration().modifyMult(id, accelMultMod * effectLevel);
		
		float level = effectLevel;
		//float f = VULNERABLE_FRACTION;
		

		
		float jitterLevel = 0f;
		float jitterRangeBonus = 0f;
		float levelForAlpha = level;
		
//		ShipSystemAPI cloak = ship.getPhaseCloak();
//		if (cloak == null) cloak = ship.getSystem();
		
		
		if (state == State.IN || state == State.ACTIVE) {
			ship.setPhased(true);
			levelForAlpha = level;
		} else if (state == State.OUT) {
			if (level > 0.5f) {
				ship.setPhased(true);
			} else {
				ship.setPhased(false);
			}
			levelForAlpha = level;
//			if (level >= f) {
//				ship.setPhased(true);
//				if (f >= 1) {
//					levelForAlpha = level;
//				} else {
//					levelForAlpha = (level - f) / (1f - f);
//				}
//				float time = cloak.getChargeDownDur();
//				float fadeLevel = JITTER_FADE_TIME / time;
//				if (level >= f + fadeLevel) {
//					jitterLevel = 0f;
//				} else {
//					jitterLevel = (fadeLevel - (level - f)) / fadeLevel;
//				}
//			} else {
//				ship.setPhased(false);
//				levelForAlpha = 0f;
//				
//				float time = cloak.getChargeDownDur();
//				float fadeLevel = JITTER_FADE_TIME / time;
//				if (level < fadeLevel) {
//					jitterLevel = level / fadeLevel;
//				} else {
//					jitterLevel = 1f;
//				}
//				//jitterLevel = level / f;
//				//jitterLevel = (float) Math.sqrt(level / f);
//			}
		}
		
//		ship.setJitter(JITTER_COLOR, jitterLevel, 1, 0, 0 + jitterRangeBonus);
//		ship.setJitterUnder(JITTER_COLOR, jitterLevel, 11, 0f, 7f + jitterRangeBonus);
		//ship.getEngineController().fadeToOtherColor(this, spec.getEffectColor1(), new Color(0,0,0,0), jitterLevel, 1f);
		//ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
		
		ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha);
		ship.setApplyExtraAlphaToEngines(true);
		
		
		//float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * levelForAlpha;
		float extra = 0f;
//		if (isDisruptable(cloak)) {
//			extra = disruptionLevel;
//		}
		float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha * (1f - extra);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			if (ship.areAnyEnemiesInRange()) {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			} else {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 2f / shipTimeMult);
//			}
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
//		float mitigationLevel = jitterLevel;
//		if (mitigationLevel > 0) {
//			stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * mitigationLevel);
//			stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * mitigationLevel);
//			stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * mitigationLevel);
//		} else {
//			stats.getHullDamageTakenMult().unmodify(id);
//			stats.getArmorDamageTakenMult().unmodify(id);
//			stats.getEmpDamageTakenMult().unmodify(id);
//		}
	}


	public void unapply(MutableShipStatsAPI stats, String id) {
//		stats.getHullDamageTakenMult().unmodify(id);
//		stats.getArmorDamageTakenMult().unmodify(id);
//		stats.getEmpDamageTakenMult().unmodify(id);
		
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			//player = ship == Global.getCombatEngine().getPlayerShip();
			//id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
		
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxSpeed().unmodifyMult(id + "_2");
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		
		ship.setPhased(false);
		ship.setExtraAlphaMult(1f);
		
		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null) cloak = ship.getSystem();
		if (cloak != null) {
			((PhaseCloakSystemAPI)cloak).setMinCoilJitterLevel(0f);
		}
		
//		stats.getMaxSpeed().unmodify(id);
//		stats.getMaxTurnRate().unmodify(id);
//		stats.getTurnAcceleration().unmodify(id);
//		stats.getAcceleration().unmodify(id);
//		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
//		if (index == 0) {
//			return new StatusData("time flow altered", false);
//		}
//		float percent = (1f - INCOMING_DAMAGE_MULT) * effectLevel * 100;
//		if (index == 1) {
//			return new StatusData("damage mitigated by " + (int) percent + "%", false);
//		}
		return null;
	}
}
