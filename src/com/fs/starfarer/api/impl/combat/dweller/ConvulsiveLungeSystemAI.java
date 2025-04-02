package com.fs.starfarer.api.impl.combat.dweller;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


/**
 * The goal of this AI is *not* to make optimal decisions for when to use the system, and the system stats are not
 * balanced for that. Rather, the goal is to use the system to create predictable/interesting movement behaviors.
 * 
 * @author Alex
 *
 */
public class ConvulsiveLungeSystemAI implements ShipSystemAIScript {

	public static float HULL_LOSS_FOR_PULLBACK = 0.25f;
	public static float MAW_LUNGE_ARC = 30f;
	
	public static class SharedLungeAIData {
		public float usedByMawToAttack;
		public float usedByMawToPullBack;
	}
	
	public static SharedLungeAIData getSharedData() {
		String key = "lunge_AI_shared";
		SharedLungeAIData data = (SharedLungeAIData)Global.getCombatEngine().getCustomData().get(key);
		if (data == null) {
			data = new SharedLungeAIData();
			Global.getCombatEngine().getCustomData().put(key, data);
		}
		return data;
	}
	
	public static float MIN_AGGRO_USE_INTERVAL = 5f;
	
	public static float USE_SCORE_PER_USE = 20f;
	public static float USE_SCORE_THRESHOLD = 45f;
	
	
	protected ShipAPI ship;
	protected CombatEngineAPI engine;
	protected ShipwideAIFlags flags;
	protected ShipSystemAPI system;
	protected ConvulsiveLungeSystemScript script;
	
	protected float sinceUsedForAttackOrMove = 100f;
	protected float recentAggroUseScore = 0f;
	protected boolean allowAggroUse = true;
	
	protected IntervalUtil tracker = new IntervalUtil(0.75f, 1.25f);
	
	protected float hullLevelAtPrevSystemUse = 1f;
	protected float prevHardFluxLevel = 0f;
	
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		
		script = (ConvulsiveLungeSystemScript)system.getScript();
	}
	
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (ship == null) return;
		
		tracker.advance(amount);
		
		sinceUsedForAttackOrMove += amount;
		
		boolean isMaw = DwellerCombatStrategyAI.isMaw(ship);
		
		if (isMaw) {
			recentAggroUseScore -= amount;
			if (recentAggroUseScore < 0f) recentAggroUseScore = 0f;
			if (!allowAggroUse && recentAggroUseScore <= 0) {
				allowAggroUse = true;
			} else if (allowAggroUse && recentAggroUseScore >= USE_SCORE_THRESHOLD) {
				allowAggroUse = false;
			}
		}
		
		SharedLungeAIData data = getSharedData();
		float now = Global.getCombatEngine().getTotalElapsedTime(false);
		
		boolean forceUseForPullback = !isMaw && data.usedByMawToPullBack + 0.1f > now;
		boolean forceUseForAttack = !isMaw && data.usedByMawToAttack + 0.1f > now;
		
		if (ship.getFluxLevel() > 0.95f && ship.getHullLevel() > 0.25f && 
				ship.getShield() != null && ship.getShield().isOn()) {
			ship.getShield().toggleOff();
			ship.getAIFlags().setFlag(AIFlags.DO_NOT_USE_SHIELDS, 3f);
			forceUseForPullback = true;
		}
		
		if (tracker.intervalElapsed() || forceUseForPullback || forceUseForAttack) {
			if (!isSystemUsable()) return;
			if (ship.getFluxTracker().isOverloadedOrVenting()) return;
			
			float hullLevel = ship.getHullLevel();
			
			hullLevelAtPrevSystemUse = Math.max(hullLevelAtPrevSystemUse, hullLevel);
			
			float hardFluxLevel = ship.getHardFluxLevel();
			float fluxLevel = ship.getFluxLevel();
			
			boolean useSystemForPullback = hullLevel <= hullLevelAtPrevSystemUse - HULL_LOSS_FOR_PULLBACK;
			
			if (((hardFluxLevel >= prevHardFluxLevel && hardFluxLevel >= 0.33f) || fluxLevel > 0.65f) &&
					ship.getAIFlags().hasFlag(AIFlags.BACKING_OFF)) {// && (float) Math.random() > 0.75f) {
				if (target != null) {
					float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
					dist -= ship.getCollisionRadius() + target.getCollisionRadius();
					if (dist < 1000f || hardFluxLevel > prevHardFluxLevel + 0.02f) {
						useSystemForPullback = true;
					}
				}
			}
			
			prevHardFluxLevel = hardFluxLevel;
			
			useSystemForPullback |= forceUseForPullback;
			
			if (useSystemForPullback) {
				float angle = ship.getFacing() + 180f;
				if (target != null) {
					angle = Misc.getAngleInDegrees(target.getLocation(), ship.getLocation());
				}
				if (missileDangerDir != null) {
					angle = Misc.getAngleInDegrees(missileDangerDir) + 180f;
				}
				
				if (isMaw && Misc.getAngleDiff(ship.getFacing() + 180f, angle) > MAW_LUNGE_ARC * 0.5f) {
					return;
				}
				
				
				Vector2f point = Misc.getUnitVectorAtDegreeAngle(angle);
				point.scale(2000f);
				Vector2f.add(point, ship.getLocation(), point);
				
				giveCommand(point);
				hullLevelAtPrevSystemUse = hullLevel;
				
				if (isMaw) {
					data.usedByMawToPullBack = now;
				}
				return;
			}
			
			
			boolean useSystemForAttackOrMovement = false;
			
			float arc = 30f;
			float checkDist = 700f;
			if (!isMaw && forceUseForAttack) checkDist = 300f;
			boolean blocked = false;
			float angle = ship.getFacing();
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			if (target != null) {
				angle = Misc.getAngleInDegrees(ship.getLocation(), target.getLocation());
			}
			
			
			
			for (ShipAPI other : Global.getCombatEngine().getShips()) {
				if (other.isFighter()) continue;
				
				if (other.getOwner() != ship.getOwner() && 
						(!other.isHulk() || (isMaw && other.getMassWithModules() < ship.getMass() * 0.25f)) &&
						(ship.getHullSize().ordinal() > other.getHullSize().ordinal() || (!isMaw && forceUseForAttack)) &&
						ship.getHullLevel() > 0.5f &&
						other != target) {
					continue;
				}
				
				
				float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
				dist -= (ship.getCollisionRadius() + other.getCollisionRadius()) * 0.6f;
				if (dist > checkDist) continue;
				
				if (Misc.isInArc(angle, arc, ship.getLocation(), other.getLocation())) {
					blocked = true;
					break;
				}
			}

			float speed = ship.getVelocity().length();
			float speedInDir = Vector2f.dot(dir, ship.getVelocity());
			boolean aligned = speedInDir > speed * 0.65f && speed >= ship.getMaxSpeed() * 0.9f;
			
			useSystemForAttackOrMovement = !blocked && aligned;
			if (sinceUsedForAttackOrMove < MIN_AGGRO_USE_INTERVAL || !allowAggroUse) {
				useSystemForAttackOrMovement = false;
			}
			
			//if (!isMaw && (float) Math.random() > 0.1f) useSystemForAttackOrMovement = false;
			if (!isMaw) useSystemForAttackOrMovement = false;
			useSystemForAttackOrMovement |= (forceUseForAttack && !blocked);
			
			
			if (useSystemForAttackOrMovement) {
				if (isMaw && Misc.getAngleDiff(ship.getFacing(), angle) > MAW_LUNGE_ARC * 0.5f) {
					return;
				}
				
				Vector2f point = Misc.getUnitVectorAtDegreeAngle(angle);
				point.scale(2000f);
				Vector2f.add(point, ship.getLocation(), point);
				
				giveCommand(point);
				
				if (isMaw) {
					data.usedByMawToAttack = now;
				}
				sinceUsedForAttackOrMove = 0f;
				recentAggroUseScore += USE_SCORE_PER_USE;
				return;
			}
			
			
		}
	}
	
	public boolean isSystemUsable() {
		if (system.getCooldownRemaining() > 0) return false;
		if (system.isOutOfAmmo()) return false;
		if (system.isActive()) return false;
		return true;
	}
	
	public void giveCommand(Vector2f target) {
		if (ship.getAIFlags() != null) {
			ship.getAIFlags().setFlag(AIFlags.SYSTEM_TARGET_COORDS, 1f, target);
		}
		ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
	}

}






















