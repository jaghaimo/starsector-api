package com.fs.starfarer.api.impl.combat.dweller;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


public class TenebrousExpulsionSystemAI implements ShipSystemAIScript {

	public static float HULL_LOSS_FOR_ACTIVATION = 0.25f;
	
	protected ShipAPI ship;
	protected CombatEngineAPI engine;
	protected ShipwideAIFlags flags;
	protected ShipSystemAPI system;
	protected TenebrousExpulsionSystemScript script;
	
	protected IntervalUtil tracker = new IntervalUtil(0.75f, 1.25f);
	
	protected float hullLevelAtPrevSystemUse = 1f;
	protected float prevHardFluxLevel = 0f;
	
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		
		script = (TenebrousExpulsionSystemScript)system.getScript();
	}
	
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (ship == null) return;
		
		tracker.advance(amount);
		
		boolean forceUse = false;
		if (ship.getFluxLevel() > 0.95f && ship.getHullLevel() > 0.25f && 
				ship.getShield() != null && ship.getShield().isOn()) {
			forceUse = true;
		}
		
		if (tracker.intervalElapsed() || forceUse) {
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (system.isActive()) return;
			if (ship.getFluxTracker().isOverloadedOrVenting()) return;
			
			float hullLevel = ship.getHullLevel();
			float hardFluxLevel = ship.getHardFluxLevel();
			float fluxLevel = ship.getFluxLevel();
			
			boolean useSystem = hullLevel <= hullLevelAtPrevSystemUse - HULL_LOSS_FOR_ACTIVATION;
			
			if (((hardFluxLevel >= prevHardFluxLevel && hardFluxLevel >= 0.33f) || fluxLevel > 0.65f) &&
					ship.getAIFlags().hasFlag(AIFlags.BACKING_OFF)) {// && (float) Math.random() > 0.75f) {
				if (target != null) {
					float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
					dist -= ship.getCollisionRadius() + target.getCollisionRadius();
					if (dist < 1000f || hardFluxLevel > prevHardFluxLevel + 0.02f) {
						useSystem = true;
					}
				}
			}
			
			prevHardFluxLevel = hardFluxLevel;
			
			useSystem |= forceUse;
			
			if (useSystem) {
				float angle = ship.getFacing();
				if (target != null) {
					angle = Misc.getAngleInDegrees(ship.getLocation(), target.getLocation());
				}
				if (missileDangerDir != null) {
					float angle2 = Misc.getAngleInDegrees(missileDangerDir);
					if (target != null) {
						angle = angle + Misc.getClosestTurnDirection(angle, angle2) * 0.5f * Misc.getAngleDiff(angle, angle2);
					} else {
						angle = angle2;
					}
				}
				
				Vector2f point = Misc.getUnitVectorAtDegreeAngle(angle);
				point.scale(2000f);
				Vector2f.add(point, ship.getLocation(), point);
				
				giveCommand(point);
				hullLevelAtPrevSystemUse = hullLevel;
				return;
			}
		}
	}
	
	public void giveCommand(Vector2f target) {
		if (ship.getAIFlags() != null) {
			ship.getAIFlags().setFlag(AIFlags.SYSTEM_TARGET_COORDS, 1f, target);
		}
		ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
	}

}






















