package com.fs.starfarer.api.impl.combat.dem;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.util.Misc;

/**
 * 
 */
public class DEMScript extends BaseEveryFrameCombatPlugin implements MissileAIPlugin {
	
	public static enum State {
		WAIT,
		TURN_TO_TARGET,
		SIGNAL,
		FIRE,
		DONE,
	}
	
	protected State state = State.WAIT;
	protected MissileAPI missile;
	protected ShipAPI ship;
	protected WeaponAPI weapon;
	protected CombatEntityAPI fireTarget;
	protected ShipAPI demDrone;
	
	//protected Vector2f targetingLaserFireOffset = new Vector2f();
	protected List<Vector2f> targetingLaserFireOffset = new ArrayList<Vector2f>();
	protected List<Vector2f> targetingLaserSweepAngles = new ArrayList<Vector2f>();
	protected List<Vector2f> payloadSweepAngles = new ArrayList<Vector2f>();
	protected List<Float> payloadSweepPhaseShift = new ArrayList<Float>();
	protected float minDelayBeforeTriggering = 0f;
	protected boolean useTriggerAngle = false;
	protected float triggerAngle = 0f;
	protected float allowedDriftFraction = 0f;
	protected float triggerDistance = 0f;
	protected float turnRateBoost = 0f;
	protected float turnRateMultOnSignal = 1f;
	protected float targetingLaserArc = 0f;
	protected float targetingTime = 1f;
	protected float firingTime = 1f;
	protected String targetingLaserId;
	protected String payloadWeaponId;
	protected float preferredMinFireDistance;
	protected float preferredMaxFireDistance;
	protected float targetingLaserRange;
	protected float payloadSweepRateMult;
	protected boolean bombPumped;
	protected boolean fadeOutEngineWhenFiring;
	protected boolean destroyMissleWhenDoneFiring;
	protected boolean randomStrafe;
	protected boolean randomPayloadSweepPhaseShift;
	protected boolean payloadCenterSweepOnOriginalOffset;
	protected boolean snapFacingToTargetIfCloseEnough = true;
	protected Color destroyedExplosionColor;
	
	protected float elapsedWaiting = 0f;
	protected float elapsedTargeting = 0f;
	protected float elapsedFiring = 0f;
	//protected DamagingProjectileAPI explosion;
	protected int explosionDelayFrames = 0;
	protected float strafeDur = 0f;
	protected float strafeDir = 0f;
	protected boolean exploded = false;
	
	protected ShapedExplosionParams p;
	
	public DEMScript(MissileAPI missile, ShipAPI ship, WeaponAPI weapon) {
		this.missile = missile;
		this.ship = ship;
		this.weapon = weapon;
		
		JSONObject json = missile.getSpec().getBehaviorJSON();
		//minDelayBeforeTriggering = (float) json.optDouble("minDelayBeforeTriggering", 1f); 
		minDelayBeforeTriggering = getValue(json, "minDelayBeforeTriggering", 1f); 
		allowedDriftFraction = (float) json.optDouble("allowedDriftFraction", 0.33f); 
		//triggerDistance = (float) json.optDouble("triggerDistance", 500f);
		//preferredMinFireDistance = (float) json.optDouble("preferredMinFireDistance", 0f);
		triggerDistance = getValue(json, "triggerDistance", 500f);
		
		try {
			if (json.optBoolean("withShapedExplosion")) {
				p = new ShapedExplosionParams();
				p.load(json);;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		snapFacingToTargetIfCloseEnough = json.optBoolean("snapFacingToTargetIfCloseEnough", false);
		
		if (json.has("triggerAngle")) {
			useTriggerAngle = true;
			triggerAngle = getValue(json, "triggerAngle", 0f);
		}
		
		preferredMaxFireDistance = getValue(json, "preferredMaxFireDistance", triggerDistance);
		preferredMinFireDistance = getValue(json, "preferredMinFireDistance", 0f);
		if (json.has("targetingLaserRange")) {
			targetingLaserRange = (float) json.optDouble("targetingLaserRange", 600f);
		} else {
			targetingLaserRange = Math.max(triggerDistance, preferredMinFireDistance) + 200f;
		}
		turnRateBoost = (float) json.optDouble("turnRateBoost", 100f);
		turnRateMultOnSignal = (float) json.optDouble("turnRateMultOnSignal", 1f);
		//targetingTime = (float) json.optDouble("targetingTime", 1f);
		targetingTime = getValue(json, "targetingTime", 1f);
		firingTime = (float) json.optDouble("firingTime", 1.25f);
		targetingLaserId = json.optString("targetingLaserId", null);
		payloadWeaponId = json.optString("payloadWeaponId", null);
		targetingLaserArc = (float) json.optDouble("targetingLaserArc", 10f);
		payloadSweepRateMult = (float) json.optDouble("payloadSweepRateMult", 1f);
		bombPumped = json.optBoolean("bombPumped", false);
		fadeOutEngineWhenFiring = json.optBoolean("fadeOutEngineWhenFiring", false);
		destroyMissleWhenDoneFiring = json.optBoolean("destroyMissleWhenDoneFiring", false);
		randomStrafe = json.optBoolean("randomStrafe", false);
		randomPayloadSweepPhaseShift = json.optBoolean("randomPayloadSweepPhaseShift", false);
		payloadCenterSweepOnOriginalOffset = json.optBoolean("payloadCenterSweepOnOriginalOffset", false);
		if (json.has("destroyedExplosionColor")) {
			try {
				destroyedExplosionColor = Misc.optColor(json, "destroyedExplosionColor", null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		JSONArray arr = json.optJSONArray("targetingLaserFireOffset");
		if (arr != null) {
			for (int i = 0; i < arr.length(); i += 2) {
				Vector2f v = new Vector2f((float) arr.optDouble(i), (float) arr.optDouble(i + 1));
				targetingLaserFireOffset.add(v);
			}
		}
		arr = json.optJSONArray("targetingLaserSweepAngles");
		if (arr != null) {
			for (int i = 0; i < arr.length(); i += 2) {
				Vector2f v = new Vector2f((float) arr.optDouble(i), (float) arr.optDouble(i + 1));
				targetingLaserSweepAngles.add(v);
			}
		}
		arr = json.optJSONArray("payloadSweepAngles");
		if (arr != null) {
			for (int i = 0; i < arr.length(); i += 2) {
				Vector2f v = new Vector2f((float) arr.optDouble(i), (float) arr.optDouble(i + 1));
				
				if (payloadCenterSweepOnOriginalOffset) {
					float orig = Global.getSettings().getWeaponSpec(payloadWeaponId).getTurretAngleOffsets().get(i/2);
					v.x += orig;
					v.y += orig;
				}
				
				payloadSweepAngles.add(v);
			}
		}
		if (randomPayloadSweepPhaseShift) {
			for (int i = 0; i < payloadSweepAngles.size(); i++) {
				payloadSweepPhaseShift.add((float) Math.random());
			}
		}
		float maxSpeed = Math.max(50f, missile.getMaxSpeed());
		float etaMod = -1f * triggerDistance / maxSpeed;
		missile.setEtaModifier(etaMod);
	}
	
	public static float getValue(JSONObject json, String key, float defaultValue) {
		JSONArray arr = json.optJSONArray(key);
		if (arr != null) {
			Vector2f v = new Vector2f((float) arr.optDouble(0), (float) arr.optDouble(1));
			return v.x + (v.y - v.x) * (float) Math.random();
		}
		return (float) json.optDouble(key, defaultValue);
	}


	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		if (Global.getCombatEngine().isPaused()) return;

		// so that the AI doesn't treat fizzled missiles as a threat due to the drone still being there
		if (missile.isFizzling()) {
			if (demDrone != null) {
				Global.getCombatEngine().removeEntity(demDrone);
			}
		}
		
		boolean doCleanup = state == State.DONE || 
				(!bombPumped || state.ordinal() < State.FIRE.ordinal()) && 
				(missile.isExpired() || missile.didDamage() || 
				!Global.getCombatEngine().isEntityInPlay(missile));
		if (doCleanup) {
			if (demDrone != null) {
				Global.getCombatEngine().removeEntity(demDrone);
			}
			Global.getCombatEngine().removePlugin(this);
			return;
		}
		
		if (state == State.WAIT && missile.isArmed() && !missile.isFizzling() && !missile.isFading()) {
			CombatEntityAPI target = null;
			if (missile.getAI() instanceof GuidedMissileAI) {
				GuidedMissileAI ai = (GuidedMissileAI) missile.getAI();
				target = ai.getTarget();
			}
			elapsedWaiting += amount;
			
			if (useTriggerAngle && target != null) {
				Vector2f from = target.getLocation();
				if (target instanceof ShipAPI) {
					from = ((ShipAPI) target).getShieldCenterEvenIfNoShield();
				}
				float toMissile = Misc.getAngleInDegrees(from, missile.getLocation());
				//float diff = Misc.getAngleDiff(target.getFacing(), toMissile);
				//float toShip = Misc.getAngleInDegrees(from, ship.getLocation());
				float toShip = Misc.getAngleInDegrees(from, missile.getSpawnLocation());
				float diff = Misc.getAngleDiff(toShip, toMissile);
				if (diff >= triggerAngle) {
					elapsedWaiting = minDelayBeforeTriggering;
				}
			}
			
			if (target != null && elapsedWaiting >= minDelayBeforeTriggering) {
				float dist = Misc.getDistance(target.getLocation(), missile.getLocation());
				dist -= Global.getSettings().getTargetingRadius(missile.getLocation(), target, false);
				
				if (dist < triggerDistance) {
					missile.setMaxFlightTime(10000f);
					state = State.TURN_TO_TARGET;
					fireTarget = target;
					
					// turn off the normal missile AI; this script is taking over
					missile.setMissileAI(this);
					
					missile.getEngineStats().getMaxTurnRate().modifyFlat("dem", turnRateBoost);
					missile.getEngineStats().getTurnAcceleration().modifyFlat("dem", turnRateBoost * 2f);
					
					ShipHullSpecAPI spec = Global.getSettings().getHullSpec("dem_drone");
					ShipVariantAPI v = Global.getSettings().createEmptyVariant("dem_drone", spec);
					v.addWeapon("WS 000", targetingLaserId);
					WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
					g.addSlot("WS 000");
					v.addWeaponGroup(g);
					v.addWeapon("WS 001", payloadWeaponId);
					g = new WeaponGroupSpec(WeaponGroupType.LINKED);
					g.addSlot("WS 001");
					v.addWeaponGroup(g);
					
					demDrone = Global.getCombatEngine().createFXDrone(v);
					demDrone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
					demDrone.setOwner(ship.getOriginalOwner());
					demDrone.getMutableStats().getBeamWeaponRangeBonus().modifyFlat("dem", targetingLaserRange);
					demDrone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
					demDrone.setDrone(true);
					demDrone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, ship);
					demDrone.getMutableStats().getEnergyWeaponDamageMult().applyMods(ship.getMutableStats().getMissileWeaponDamageMult());
					demDrone.getMutableStats().getMissileWeaponDamageMult().applyMods(ship.getMutableStats().getMissileWeaponDamageMult());
					demDrone.getMutableStats().getBallisticWeaponDamageMult().applyMods(ship.getMutableStats().getMissileWeaponDamageMult());
					
					demDrone.getMutableStats().getDamageToCapital().applyMods(ship.getMutableStats().getDamageToCapital());
					demDrone.getMutableStats().getDamageToCruisers().applyMods(ship.getMutableStats().getDamageToCruisers());
					demDrone.getMutableStats().getDamageToDestroyers().applyMods(ship.getMutableStats().getDamageToDestroyers());
					demDrone.getMutableStats().getDamageToFrigates().applyMods(ship.getMutableStats().getDamageToFrigates());
					demDrone.getMutableStats().getDamageToFighters().applyMods(ship.getMutableStats().getDamageToFighters());
					demDrone.getMutableStats().getDamageToMissiles().applyMods(ship.getMutableStats().getDamageToMissiles());
					demDrone.getMutableStats().getDamageToTargetEnginesMult().applyMods(ship.getMutableStats().getDamageToTargetEnginesMult());
					demDrone.getMutableStats().getDamageToTargetHullMult().applyMods(ship.getMutableStats().getDamageToTargetHullMult());
					demDrone.getMutableStats().getDamageToTargetShieldsMult().applyMods(ship.getMutableStats().getDamageToTargetShieldsMult());
					demDrone.getMutableStats().getDamageToTargetWeaponsMult().applyMods(ship.getMutableStats().getDamageToTargetWeaponsMult());
					
					demDrone.setCollisionClass(CollisionClass.NONE);
					demDrone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
					Global.getCombatEngine().addEntity(demDrone);
					
					if (targetingLaserFireOffset.size() > 0) {
						WeaponAPI tLaser = demDrone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
						tLaser.ensureClonedSpec();
						tLaser.getSpec().getTurretFireOffsets().clear();
						tLaser.getSpec().getTurretFireOffsets().addAll(targetingLaserFireOffset);
					}
				}
			}
		} else if (state == State.TURN_TO_TARGET) {
			float angle = Misc.getAngleInDegrees(missile.getLocation(), fireTarget.getLocation());
			
			if (Misc.isInArc(missile.getFacing(), targetingLaserArc, angle)) {
				missile.getEngineStats().getMaxTurnRate().modifyMult("dem_mult", turnRateMultOnSignal);
				//missile.getEngineStats().getTurnAcceleration().modifyMult("dem_mult", turnRateMultOnSignal);
				
				state = State.SIGNAL;
			}
		} else if (state == State.SIGNAL) {
			
			if (targetingLaserSweepAngles.size() > 0) {
				float progress = elapsedTargeting / targetingTime;
				WeaponAPI tLaser = demDrone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
				tLaser.ensureClonedSpec();
				tLaser.getSpec().getTurretAngleOffsets().clear();
				for (Vector2f curr : targetingLaserSweepAngles) {
					float angle = 0f;
					if (progress < 0.5f) {
						angle = curr.x + (curr.y - curr.x) * progress * 2f;
					} else {
						angle = curr.x + (curr.y - curr.x) * (1f - progress) * 2f;
					}
					tLaser.getSpec().getTurretAngleOffsets().add(angle);
				}
			}
			
			if (targetingLaserRange > 0 && targetingTime > 0) {
				demDrone.giveCommand(ShipCommand.FIRE, fireTarget.getLocation(), 0);
			}
			
			elapsedTargeting += amount;
			if (elapsedTargeting >= targetingTime) {
				state = State.FIRE;
				demDrone.giveCommand(ShipCommand.SELECT_GROUP, null, 1);
				
				if (!bombPumped) {
					//missile.flameOut();
					missile.setFlightTime(0f);
					missile.setMaxFlightTime(firingTime);
					missile.setNoFlameoutOnFizzling(true);
					missile.setNoGlowTime(0f);
					missile.setFizzleTime(0.5f);
					missile.setFadeTime(0.5f);
					missile.setEtaModifier(0f);
				}
			}
		} else if (state == State.FIRE) {
			//Global.getCombatEngine().setPaused(true);
			
			if (bombPumped && !exploded && explosionDelayFrames >= 1) {
				missile.explode();
				Global.getCombatEngine().removeEntity(missile);
				
//				ShapedExplosionParams p = new ShapedExplosionParams();
//				p.shapedExplosionNumParticles = 50;
//				p.shapedExplosionMinParticleDur = 0.7f;
//				p.shapedExplosionMaxParticleDur = 1.1f;
//				p.shapedExplosionMinParticleSize = 50f;
//				p.shapedExplosionMaxParticleSize = 70f;
//				p.shapedExplosionColor = new Color(255,40,40,155);
//				p.shapedExplosionArc = 45f;
//				p.shapedExplosionMinParticleVel = 50f;
//				p.shapedExplosionMaxParticleVel = 250f;
//				
////				p.shapedExplosionMinParticleDur = 1f;
////				p.shapedExplosionMaxParticleDur = 3f;
//				
//				float speedMult = 1f;
//				p.shapedExplosionMinParticleVel *= speedMult;
//				p.shapedExplosionMaxParticleVel *= speedMult;
//				p.shapedExplosionMinParticleDur /= speedMult;
//				p.shapedExplosionMaxParticleDur /= speedMult;
				if (p != null) {
					spawnShapedExplosion(missile.getLocation(), missile.getFacing(), p);
				}
				exploded = true;
			}
			explosionDelayFrames++;
			
			if (fadeOutEngineWhenFiring) {
				float progress = elapsedFiring / firingTime;
				progress *= 2f;
				if (progress > 1f) progress = 1f;
				missile.getEngineController().fadeToOtherColor(this, Misc.zeroColor, Misc.zeroColor, progress, 1f);
			}
			
			if (payloadSweepAngles.size() > 0) {
				WeaponAPI payload = demDrone.getWeaponGroupsCopy().get(1).getWeaponsCopy().get(0);
				payload.ensureClonedSpec();
				payload.getSpec().getTurretAngleOffsets().clear();
				int index = 0;
				for (Vector2f curr : payloadSweepAngles) {
					float angle = 0f;
					float progress = elapsedFiring / firingTime;
					if (progress < 0.5f) {
						angle = curr.x + (curr.y - curr.x) * progress * 2f;
					} else {
						angle = curr.x + (curr.y - curr.x) * (1f - progress) * 2f;
					}
					if (randomPayloadSweepPhaseShift) {
						progress += payloadSweepPhaseShift.get(index);
						progress = (float) Math.sin(progress * Math.PI * payloadSweepRateMult);
						progress = Math.abs(progress);
						
						angle = curr.x + (curr.y - curr.x) * progress;
					}
					
					
					payload.getSpec().getTurretAngleOffsets().add(angle);
					index++;
				}
			}
			
			
			// use payload's normal range as defined in weapon_data.csv
			demDrone.getMutableStats().getBeamWeaponRangeBonus().unmodifyFlat("dem");
			demDrone.giveCommand(ShipCommand.FIRE, fireTarget.getLocation(), 0);
			
			elapsedFiring += amount;
			if (elapsedFiring >= firingTime) {
				missile.setNoGlowTime(10f);
				state = State.DONE;
				
				if (destroyMissleWhenDoneFiring) {
					missile.getVelocity().set(0, 0);
					if (destroyedExplosionColor != null) {
						missile.setDestroyedExplosionColorOverride(destroyedExplosionColor);
					}
					Global.getCombatEngine().applyDamage(missile, missile.getLocation(), 100000f, DamageType.ENERGY, 0f, false, false, demDrone, false);
				} else {
					missile.setFizzleTime(1f);
					missile.setArmedWhileFizzling(false);
				}
			}
		}
		
		doMissileControl(amount);
		updateDroneState(amount);
		
	}
	
	protected void updateDroneState(float amount) {
		if (demDrone != null) {
			//System.out.println("FIRE FACING: " + missile.getFacing());
			//if (explosion == null) {
				demDrone.setOwner(missile.getOwner());
				demDrone.getLocation().set(missile.getLocation());
				demDrone.setFacing(missile.getFacing());
				demDrone.getVelocity().set(missile.getVelocity());
				demDrone.setAngularVelocity(missile.getAngularVelocity());
				//demDrone.getMouseTarget().set(fireTarget.getLocation());
			//}
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing());
			dir.scale(1000f);
			Vector2f.add(dir, missile.getLocation(), dir);
			demDrone.getMouseTarget().set(dir);
			
			//demDrone.getMutableStats().getWeaponTurnRateBonus().modifyMult("dem", 0f);
			
			WeaponAPI tLaser = demDrone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
			WeaponAPI payload = demDrone.getWeaponGroupsCopy().get(1).getWeaponsCopy().get(0);
			tLaser.setFacing(missile.getFacing());
			payload.setFacing(missile.getFacing());
			tLaser.setKeepBeamTargetWhileChargingDown(true);
			payload.setKeepBeamTargetWhileChargingDown(true);
			tLaser.setScaleBeamGlowBasedOnDamageEffectiveness(false);
			if (firingTime <= 2f) {
				payload.setScaleBeamGlowBasedOnDamageEffectiveness(false);
			}
			tLaser.updateBeamFromPoints();
			payload.updateBeamFromPoints();
		}
	}
	
	protected void doMissileControl(float amount) {
		if (state == State.TURN_TO_TARGET || state == State.SIGNAL ||
				(state == State.FIRE && !bombPumped && !fadeOutEngineWhenFiring)) {
			
			float dist = Misc.getDistance(fireTarget.getLocation(), missile.getLocation());
			dist -= Global.getSettings().getTargetingRadius(missile.getLocation(), fireTarget, false);
			if (dist < preferredMinFireDistance) {
				missile.giveCommand(ShipCommand.ACCELERATE_BACKWARDS);
			} else if (dist > preferredMaxFireDistance) {
				missile.giveCommand(ShipCommand.ACCELERATE);
			} else if (missile.getVelocity().length() > missile.getMaxSpeed() * allowedDriftFraction) {
				missile.giveCommand(ShipCommand.DECELERATE);
			}
			float dir = Misc.getAngleInDegrees(missile.getLocation(), fireTarget.getLocation());
			float diff = Misc.getAngleDiff(missile.getFacing(), dir);
			float rate = missile.getMaxTurnRate() * amount;
//			float turnDir1 = Misc.getClosestTurnDirection(missile.getFacing(), dir);
//			boolean turningTowardsDesiredFacing = Math.signum(turnDir1) == Math.signum(missile.getAngularVelocity());
			boolean turningTowardsDesiredFacing = true;
			//snapFacingToTargetIfCloseEnough = true;
			boolean phased = fireTarget instanceof ShipAPI && ((ShipAPI)fireTarget).isPhased();
			if (!phased) {
				if (diff <= rate * 0.25f && turningTowardsDesiredFacing && snapFacingToTargetIfCloseEnough) {
					missile.setFacing(dir);
				} else {
					Misc.turnTowardsPointV2(missile, fireTarget.getLocation(), 0f);
				}
			}
			
			if (randomStrafe) {
				if (strafeDur <= 0) {
					float r = (float) Math.random();
					
					if (strafeDir == 0) {
						if (r < 0.4f) {
							strafeDir = 1f;
						} else if (r < 0.8f) {
							strafeDir = -1f;
						} else {
							strafeDir = 0f;
						}
					} else {
						if (r < 0.8f) {
							strafeDir = -strafeDir;
						} else {
							strafeDir = 0f;
						}
					}
					
					strafeDur = 0.5f + (float) Math.random() * 0.5f;
					//strafeDur *= 0.5f;
				}
				
				Vector2f driftDir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + 90f);
				if (strafeDir == 1f) driftDir.negate();
				
				float distToShip = Misc.getDistance(ship.getLocation(), missile.getLocation());
				float shipToFireTarget = Misc.getDistance(ship.getLocation(), fireTarget.getLocation());
				float extra = 0f;
				if (dist > shipToFireTarget) extra = dist - shipToFireTarget;
				if (distToShip < ship.getCollisionRadius() * 1f + extra) {
					float away = Misc.getAngleInDegrees(ship.getLocation(), missile.getLocation());
					float turnDir = Misc.getClosestTurnDirection(away, missile.getFacing());
					strafeDir = turnDir;
				}
				
				float maxDrift = missile.getMaxSpeed() * allowedDriftFraction;
				float speedInDir = Vector2f.dot(driftDir, missile.getVelocity());
				
				if (speedInDir < maxDrift) {
					if (strafeDir == 1f) {
						missile.giveCommand(ShipCommand.STRAFE_RIGHT);
					} else if (strafeDir == -1f) {
						missile.giveCommand(ShipCommand.STRAFE_LEFT);
					}
				}
				
				strafeDur -= amount;
			}
		}
	}

	public void advance(float amount) {
		// MissileAIPlugin.advance()
		// unused, but just want the missile to have a non-null AI
	}
	
	
	
	public static class ShapedExplosionParams {
		public float shapedExplosionEndSizeMin = 1f;
		public float shapedExplosionEndSizeMax = 2f;
		public Color shapedExplosionColor = new Color(255,150,130,155);
		public int shapedExplosionNumParticles = 200;
		public float shapedExplosionMinParticleSize = 80;
		public float shapedExplosionMaxParticleSize = 100;
		public float shapedExplosionScatter = 100f;
		public float shapedExplosionMinParticleVel = 100;
		public float shapedExplosionMaxParticleVel = 350f;
		public float shapedExplosionMinParticleDur = 1f;
		public float shapedExplosionMaxParticleDur = 2f;
		public float shapedExplosionArc = 90f;
		
		public void load(JSONObject json) throws JSONException {
			shapedExplosionEndSizeMin = (float)json.optDouble("shapedExplosionEndSizeMin", 1f);
			shapedExplosionEndSizeMax = (float)json.optDouble("shapedExplosionEndSizeMax", 2f);
			shapedExplosionNumParticles = json.optInt("shapedExplosionNumParticles");
			shapedExplosionMinParticleSize = (float)json.optDouble("shapedExplosionMinParticleSize", 80f);
			shapedExplosionMaxParticleSize = (float)json.optDouble("shapedExplosionMaxParticleSize", 100f);
			shapedExplosionScatter = (float)json.optDouble("shapedExplosionScatter", 100f);
			shapedExplosionMinParticleVel = (float)json.optDouble("shapedExplosionMinParticleVel", 100f);
			shapedExplosionMaxParticleVel = (float)json.optDouble("shapedExplosionMaxParticleVel", 350f);
			shapedExplosionMinParticleDur = (float)json.optDouble("shapedExplosionMinParticleDur", 1f);
			shapedExplosionMaxParticleDur = (float)json.optDouble("shapedExplosionMaxParticleDur", 2f);
			shapedExplosionArc = (float)json.optDouble("shapedExplosionArc", 90f);
			shapedExplosionColor = Misc.optColor(json, "shapedExplosionColor", null);
		}
	}
	
	public void spawnShapedExplosion(Vector2f loc, float angle, ShapedExplosionParams p) {
		
		if (Global.getCombatEngine().getViewport().isNearViewport(ship.getLocation(), 800f)) {
			int numParticles = p.shapedExplosionNumParticles;
			float minSize = p.shapedExplosionMinParticleSize;
			float maxSize = p.shapedExplosionMaxParticleSize;
			Color pc = p.shapedExplosionColor;
			
			float minDur = p.shapedExplosionMinParticleDur;
			float maxDur = p.shapedExplosionMaxParticleDur;
			
			float arc = p.shapedExplosionArc; 
			float scatter = p.shapedExplosionScatter;
			float minVel = p.shapedExplosionMinParticleVel;
			float maxVel = p.shapedExplosionMaxParticleVel;
			
			float endSizeMin = p.shapedExplosionEndSizeMin;
			float endSizeMax = p.shapedExplosionEndSizeMax;
			
			Vector2f spawnPoint = new Vector2f(loc);
			for (int i = 0; i < numParticles; i++) {
				//p.setMaxAge(500 + (int)(Math.random() * 1000f));
				float angleOffset = (float) Math.random();
				if (angleOffset > 0.2f) {
					angleOffset *= angleOffset;
				}
				float speedMult = 1f - angleOffset;
				speedMult = 0.5f + speedMult * 0.5f;
				angleOffset *= Math.signum((float) Math.random() - 0.5f);
				angleOffset *= arc/2f;
				float theta = (float) Math.toRadians(angle + angleOffset);
				float r = (float) (Math.random() * Math.random() * scatter);
				float x = (float)Math.cos(theta) * r;
				float y = (float)Math.sin(theta) * r;
				Vector2f pLoc = new Vector2f(spawnPoint.x + x, spawnPoint.y + y);

				float speed = minVel + (maxVel - minVel) * (float) Math.random();
				speed *= speedMult;
				
				Vector2f pVel = Misc.getUnitVectorAtDegreeAngle((float) Math.toDegrees(theta));
				pVel.scale(speed);
				
				float pSize = minSize + (maxSize - minSize) * (float) Math.random();
				float pDur = minDur + (maxDur - minDur) * (float) Math.random();
				float endSize = endSizeMin + (endSizeMax - endSizeMin) * (float) Math.random();
				//Global.getCombatEngine().addSmoothParticle(pLoc, pVel, pSize, 1f, pDur, pc);
				Global.getCombatEngine().addNebulaParticle(pLoc, pVel, pSize, endSize, 0.1f, 0.5f, pDur, pc);
				//Global.getCombatEngine().addNebulaSmoothParticle(pLoc, pVel, pSize, endSize, 0.1f, 0.5f, pDur, pc);
				//Global.getCombatEngine().addSwirlyNebulaParticle(pLoc, pVel, pSize, endSize, 0.1f, 0.5f, pDur, pc, false);
			}
		}
	}
}








