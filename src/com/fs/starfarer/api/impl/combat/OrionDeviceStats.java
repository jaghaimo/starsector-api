package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;

/**
 *  The way to provide custom params is to have a derived class that sets p = <params> in its constructor. 
 *  
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public class OrionDeviceStats extends BaseShipSystemScript {

	public static class PusherPlateImpulse {
		public float force;
		public float dur;
		public float elapsed;
	}
	public static class PusherPlateState {
		public float compression;
		public float vel;
		public List<PusherPlateImpulse> impulses = new ArrayList<PusherPlateImpulse>();
		
		public void addImpulse(float force, float dur) {
			PusherPlateImpulse ppi = new PusherPlateImpulse();
			ppi.force = force;
			ppi.dur = dur;
			impulses.add(ppi);
		
		}
		public void advance(float amount) {
			List<PusherPlateImpulse> remove = new ArrayList<PusherPlateImpulse>();
			float totalForce = 0f;
			for (PusherPlateImpulse curr : impulses) {
				totalForce += curr.force;
				curr.elapsed += amount;
				if (curr.elapsed >= curr.dur) {
					remove.add(curr);
				}
			}
			impulses.removeAll(remove);
			
			// assuming k of 1, and a mass of 1
			float springForce = compression;
			float netForce = totalForce - springForce;
			
			vel += netForce * amount;
			compression += vel * amount;
			
			if (compression > 1f) {
				compression = 1f;
				vel = 0f;
			}
			float min = 0f;
			//min = -0.5f;
			if (compression < min) {
				compression = min;
				vel = 0f;
			}
			
		}
	}
	
	
	public static class OrionDeviceParams {
		public float bombFadeInTime = 0.15f;
		public float bombLiveTime = 0.25f;
		public float bombSpeed = 50f;
		public float bombInheritedVelocityFraction = 0.5f;
		
		public float shapedExplosionOffset = 50f;
		public float shapedExplosionEndSizeMin = 1f;
		public float shapedExplosionEndSizeMax = 2f;
		public Color shapedExplosionColor = new Color(255,125,25,155);
		public int shapedExplosionNumParticles = 200;
		public float shapedExplosionMinParticleSize = 80;
		public float shapedExplosionMaxParticleSize = 100;
		public float shapedExplosionScatter = 100f;
		public float shapedExplosionMinParticleVel = 100;
		public float shapedExplosionMaxParticleVel = 350f;
		public float shapedExplosionMinParticleDur = 1f;
		public float shapedExplosionMaxParticleDur = 2f;
		public float shapedExplosionArc = 90f;
		public Color jitterColor = new Color(255,125,25,55);
		public float maxJitterDur = 2f;
		
		public float pusherPlateMaxOffset = 14f;
		public float pusherPlateImpulseForce = 10f;
		public float pusherPlateImpulseDuration = 0.2f;
		
		public float impactAccel = 5000f;
		public float impactRateMult = 4f;
		
		public boolean recolorTowardsEngineColor = false;
		
		public String bombWeaponId = "od_bomblauncher";
	}
	
	
	protected OrionDeviceParams p = new OrionDeviceParams();
	protected PusherPlateState pusherState = new PusherPlateState();
	
	public OrionDeviceStats() {
		p = new OrionDeviceParams();
		//p.recolorTowardsEngineColor = true;
	}
	

	protected Color orig = null;
	protected void recolor(ShipAPI ship) {
		if (ship == null) return;
		if (!p.recolorTowardsEngineColor) return;
		
		if (orig == null) orig = p.shapedExplosionColor;
		
		Color curr = ship.getEngineController().getFlameColorShifter().getCurr();
		
		p.shapedExplosionColor = Misc.interpolateColor(orig, curr, 0.75f);
		p.shapedExplosionColor = Misc.setAlpha(p.shapedExplosionColor, orig.getAlpha());
	}
	
	protected boolean wasIdle = false;
	protected boolean deployedBomb = false;
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		recolor(ship);
		
		if (effectLevel >= 1 && !deployedBomb) {
			for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
				if (slot.isSystemSlot()) {
					spawnBomb(ship, slot);
				}
			}
			deployedBomb = true;
			//pusherPlateOffset = 1f;
		} else if (state == State.COOLDOWN) {
			deployedBomb = false;
		}
		
		
		float amount = Global.getCombatEngine().getElapsedInLastFrame();
		pusherState.advance(amount);
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			Vector2f offset = new Vector2f(p.pusherPlateMaxOffset, 0f);
			//pusherPlateOffset = 1f;
			offset.scale(pusherState.compression);
			if (w.getSpec().hasTag("pusherplate")) {
				w.setRenderOffsetForDecorativeBeamWeaponsOnly(offset);
			}
		}
		
		advanceImpl(amount, ship, state, effectLevel);
	}
	
	protected void advanceImpl(float amount, ShipAPI ship, State state, float effectLevel) {
		
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public void spawnBomb(ShipAPI source, WeaponSlotAPI slot) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Vector2f loc = slot.computePosition(source);
		float angle = slot.computeMidArcAngle(source);
		
		if (pusherState.compression > 0) {
			Vector2f offset = new Vector2f(p.pusherPlateMaxOffset, 0f);
			offset.scale(pusherState.compression);
			offset = Misc.rotateAroundOrigin(offset, source.getFacing());
			Vector2f.add(loc, offset, loc);
		}
		
		MissileAPI bomb = (MissileAPI) engine.spawnProjectile(source, null, 
															  p.bombWeaponId, 
															  loc, 
															  angle, source.getVelocity());
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.MISSILE, false, bomb.getDamage());
		}
		
		float fadeInTime = p.bombFadeInTime;
		Vector2f inheritedVel = new Vector2f(source.getVelocity());
		inheritedVel.scale(p.bombInheritedVelocityFraction);
		float speed = p.bombSpeed;
		
		Vector2f vel = Misc.getUnitVectorAtDegreeAngle(angle);
		vel.scale(speed);
		Vector2f.add(vel, inheritedVel, vel);
		bomb.getVelocity().set(vel);
		bomb.fadeOutThenIn(fadeInTime);
		
		bomb.setCollisionClass(CollisionClass.NONE);
		bomb.setEmpResistance(1000);
		bomb.setEccmChanceOverride(1f);
		
		
		float liveTime = p.bombLiveTime;
		bomb.setMaxFlightTime(liveTime);
		

		Global.getCombatEngine().addPlugin(createBombImpactPlugin(source, slot, bomb, loc, angle));
	}
	
	protected void notifySpawnedExplosionParticles(Vector2f bombLoc) {
		
	}
	
	protected EveryFrameCombatPlugin createBombImpactPlugin(final ShipAPI ship, final WeaponSlotAPI launchSlot,
			final MissileAPI bomb, final Vector2f launchLoc, final float launchAngle) {
		
		return new BaseEveryFrameCombatPlugin() {
			float elapsed = 0f;
			float impactTime = 0f;
			float brakingTime = 0f;
			float forceAngle;
			float jitterTime = 0f;
			boolean triggered = false;
			boolean braking = false;
			boolean done = false;
			
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;
			
				elapsed += amount;
				
				String impactCounterId = "od_system_counter";
				
				if (bomb.isFizzling()) {
					if (!triggered) {
						
						pusherState.addImpulse(p.pusherPlateImpulseForce, p.pusherPlateImpulseDuration);
						
						forceAngle = Misc.getAngleInDegrees(bomb.getLocation(), launchSlot.computePosition(ship));
						float angleToShip = Misc.getAngleInDegrees(bomb.getLocation(), ship.getLocation());
						if (Misc.getAngleDiff(angleToShip, forceAngle) > 90f) {
							forceAngle += 180f;
						}
						
						float diff = Misc.getAngleDiff(angleToShip, forceAngle);
						float turnDir = Misc.getClosestTurnDirection(angleToShip, forceAngle);
						forceAngle = angleToShip + diff * turnDir * 0.2f;
						
						triggered = true;
						ship.getMutableStats().getDynamic().getMod(impactCounterId).modifyFlat("od_launch_" + launchLoc, 1f);
						
						if (Global.getCombatEngine().getViewport().isNearViewport(ship.getLocation(), 800f)) {
							float angle = forceAngle + 180f;
							
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
							
							float launchOffset = p.shapedExplosionOffset;
							float endSizeMin = p.shapedExplosionEndSizeMin;
							float endSizeMax = p.shapedExplosionEndSizeMax;
							
							Vector2f spawnPoint = Misc.getUnitVectorAtDegreeAngle(forceAngle);
							spawnPoint.scale(launchOffset);
							Vector2f.add(bomb.getLocation(), spawnPoint, spawnPoint);
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
							//Global.getCombatEngine().setPaused(true);
							
							notifySpawnedExplosionParticles(bomb.getLocation());
						}
					}
				}
				
				boolean multipleImpacts = ship.getMutableStats().getDynamic().getMod(impactCounterId).computeEffective(0) > 1;
				
				String id = "od_system_mod";
				ship.getMutableStats().getMaxSpeed().unmodifyFlat(id);
				//float maxSpeedWithoutBonus = ship.getMaxSpeedWithoutBoost();
				float maxSpeedWithoutBonus = ship.getMutableStats().getMaxSpeed().getModifiedValue();
				
//				if (!triggered) {
//					ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
//				}
				
				if (triggered) {
					jitterTime += amount;
					float intensity = bomb.getFlightTime() / bomb.getMaxFlightTime();
					if (intensity > 1f) intensity = 1f;
					if (triggered) intensity = 1f;
					if (braking) { 
						intensity = 1f - brakingTime * 2f;
						if (intensity < 0) intensity = 0;
					}
					float alt = 1f - (jitterTime / p.maxJitterDur);
					if (alt < intensity) {
						intensity = Math.max(alt, 0f);
					}
					Color jc = p.jitterColor;
					ship.setJitter(this, jc, intensity, 3, 0f, 0f);
				}
				
				if (triggered && !braking) {
					impactTime += amount * p.impactRateMult;
					
					float mag = (1f - impactTime) * (1f - impactTime);
					
					//mag = (float) Math.sin(impactTime * Math.PI);
					//mag *= mag;
					if (mag > 0) mag = (float) Math.sqrt(mag);
					
					Vector2f forcePoint = launchSlot.computePosition(ship);
					
					float dirToCenter = Misc.getAngleInDegrees(forcePoint, ship.getLocation());
					float angleDiff = Misc.getAngleDiff(forceAngle, dirToCenter);
//					if (angleDiff > 180f) { 
//						angleDiff = 360f - angleDiff;
//					}
					
					float totalAccel = p.impactAccel;
					float portionAppliedToAngularVelocity = angleDiff * 1f / 90f;
					if (portionAppliedToAngularVelocity > 1f) portionAppliedToAngularVelocity = 1f;
					
					Vector2f acc = Misc.getUnitVectorAtDegreeAngle(forceAngle);
					acc.scale(totalAccel * (1f - portionAppliedToAngularVelocity * 0.2f));
					acc.scale(mag * amount);
					
					Vector2f.add(ship.getVelocity(), acc, ship.getVelocity());
					
					float angVelChange = portionAppliedToAngularVelocity * ship.getMaxTurnRate() * 0.25f;
					angVelChange *= mag;
					angVelChange *= Misc.getClosestTurnDirection(forceAngle, dirToCenter);
					ship.setAngularVelocity(ship.getAngularVelocity() + angVelChange * amount);
					
					float maxSpeedBoost = 1000f * Math.max(0f, (1f - portionAppliedToAngularVelocity) * 0.5f);
					
					if (maxSpeedBoost > 0) {
						ship.getMutableStats().getMaxSpeed().modifyFlat(id, maxSpeedBoost);
					} else {
						ship.getMutableStats().getMaxSpeed().unmodifyFlat(id);
					}
					ship.getMutableStats().getDeceleration().modifyFlat(id, 1f * Math.max(maxSpeedWithoutBonus, ship.getVelocity().length() - maxSpeedWithoutBonus));
					//ship.getMutableStats().getTurnAcceleration().modifyFlat(id, 100f * (1f - mag));
					//ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
					ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
					ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
					ship.giveCommand(ShipCommand.DECELERATE, null, 0);
					//ship.getEngineController().forceShowAccelerating();
					if (impactTime >= 1f) {
						braking = true;
						//ship.getMutableStats().getTurnAcceleration().unmodify(id);
						ship.getMutableStats().getMaxSpeed().unmodify(id);
					}
				}
				
				if (braking) {
					if (!multipleImpacts) {
						ship.getMutableStats().getDeceleration().modifyFlat(id, 2f * Math.max(maxSpeedWithoutBonus, ship.getVelocity().length() - maxSpeedWithoutBonus));
						//ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
						ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
						ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
						ship.giveCommand(ShipCommand.DECELERATE, null, 0);
						//ship.getEngineController().forceShowAccelerating();
					}
					brakingTime += amount;
					float threshold = 3f;
					if (multipleImpacts) threshold = 0.1f;
					if (brakingTime >= threshold || ship.getVelocity().length() <= maxSpeedWithoutBonus) {
						done = true;
					}
				}
				
				
				if ((!triggered && elapsed > 1f) || done) {
					Global.getCombatEngine().removePlugin(this);
					
					ship.getMutableStats().getDeceleration().unmodify(id);
					//ship.getMutableStats().getTurnAcceleration().unmodify(id);
					ship.getMutableStats().getMaxSpeed().unmodify(id);
					
					ship.getMutableStats().getDynamic().getMod(impactCounterId).unmodifyFlat("od_launch_" + launchLoc);
				}
			}
		};
	}


	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		if (ship.getEngineController().isFlamedOut() || ship.getEngineController().isFlamingOut()) {
			return false;
		}
		return super.isUsable(system, ship);
	}
	
	
	
}








