package com.fs.starfarer.api.impl.combat.dweller;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ShroudedEjectaAI implements ShipAIPlugin {

	public static class FlockingData {
		public Vector2f loc;
		public Vector2f vel;
		public float minA;
		public float maxA;
		public float minR;
		public float maxR;
		public float repelAtAngleDist;
		public float minC;
		public float maxC;
		public float attractWeight;
		public float repelWeight;
		public float cohesionWeight;
		public float facing;
	}	
	
	
	/**
	 * Loses hitpoints over time and eventually dissipates.
	 */
	public static float HULL_FRACTION_LOST_PER_SECOND = 0.0667f;
	
	protected ShipwideAIFlags flags = new ShipwideAIFlags();
	protected ShipAPI ship;
	protected boolean exploded = false;
	protected float turnDir = 1f;
	
	protected IntervalUtil updateInterval = new IntervalUtil(0.5f, 1.5f);
	
	public ShroudedEjectaAI(ShipAPI ship) {
		this.ship = ship;
		
		doInitialSetup();
		
		updateInterval.forceIntervalElapsed();
	}
	
	protected void doInitialSetup() {
		turnDir = Math.signum(0.5f - (float) Math.random() * 0.5f);
		if (turnDir == 0) turnDir = 1f;
	}
	
	@Override
	public void advance(float amount) {
		//if (true) return;
		
		updateInterval.advance(amount);
		if (updateInterval.intervalElapsed()) {
		}
		
		CombatEngineAPI engine = Global.getCombatEngine();
		float damage = ship.getMaxHitpoints() * HULL_FRACTION_LOST_PER_SECOND * 1f * amount;
		ship.setHitpoints(ship.getHitpoints() - damage);
		if (ship.getHitpoints() <= 0f) {
			engine.applyDamage(ship, ship.getLocation(), 10000f, DamageType.ENERGY, 0f, true, false, ship, false);
		}
		
		giveMovementCommands();
	}
	
	protected void giveMovementCommands() {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		//ship.giveCommand(ShipCommand.DECELERATE, null, 0);
//		if (turnDir < 0f) {
//			ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
//		} else {
//			ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
//		}

		if (ship.getVelocity().length() > ship.getMaxSpeedWithoutBoost() * 0.1f) {
			ship.giveCommand(ShipCommand.DECELERATE, null, 0);
		}
		
		float heading = Misc.getAngleInDegrees(ship.getVelocity());
		//engine.headInDirectionWithoutTurning(ship, desiredHeading, 10000);
	}
	
	
	@Override
	public ShipwideAIFlags getAIFlags() {
		return flags;
	}
	
	
	
	public void setDoNotFireDelay(float amount) {}
	public void forceCircumstanceEvaluation() {}
	public boolean needsRefit() { return false; }
	public void cancelCurrentManeuver() {}
	public ShipAIConfig getConfig() { return null; }
}













