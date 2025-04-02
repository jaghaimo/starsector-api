package com.fs.starfarer.api.impl.combat.dweller;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.util.Misc;

public class ConvulsiveLungeSystemScript extends BaseShipSystemScript {
	
	public static float PULL_DIST = 1000f;
	public static float PARTICLE_WINDUP_ACCEL = 1500f;
	public static float SPRING_CONSTANT = 4f;
	public static float FRICTION = 1000f;
	
	public static float MAW_WINDUP_MULT = 3f;
	
	protected Vector2f dest;
	protected boolean fadedFlash = false;
	
	protected void init(ShipAPI ship) {
	}
	
	
//	public static boolean isMaw(ShipAPI ship) {
//		boolean isMaw = ship != null && ship.isCapital();
//		return isMaw;
//	}
	
	@Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		if (state == State.IN) {
			boolean destWasNull = dest == null;
			if (dest == null) {
				dest = new Vector2f(ship.getMouseTarget());
				if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)){
					dest = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
				}
			}
			if (dest == null) {
				dest = Misc.getUnitVectorAtDegreeAngle(ship.getFacing() + 180f);
				dest.scale(PULL_DIST);
				Vector2f.add(dest, ship.getLocation(), dest);
			}
			
//			float dist = Misc.getDistance(ship.getLocation(), dest);
//			if (dist < PULL_DIST) {
			if (destWasNull) {
				Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(ship.getLocation(), dest));
				dest = dir;
				dest.scale(PULL_DIST);
				Vector2f.add(dest, ship.getLocation(), dest);
			}
		}
		
		if (dest != null) {
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(ship.getLocation(), dest));
			float amount = Global.getCombatEngine().getElapsedInLastFrame();
			
			if (state == State.IN) {
				boolean isMaw = DwellerCombatStrategyAI.isMaw(ship);
				float accel = PARTICLE_WINDUP_ACCEL * amount * effectLevel;
				if (isMaw) {
					accel *= MAW_WINDUP_MULT;
				}
				if (shroud != null) {
					boolean affect = true;
					for (SwarmMember p : shroud.getMembers()) {
						if (affect) {
							p.vel.x += dir.x * accel;
							p.vel.y += dir.y * accel;
						}
						affect = !affect;
					}
					//shroud.getParams().baseMembersToMaintain = 150;
				}
			}
			
			if (state == State.ACTIVE) {
				if (!fadedFlash) {
					//shroud.getParams().baseMembersToMaintain = 100;
					if (shroud != null) {
						for (SwarmMember p : shroud.getMembers()) {
							if (p.flash != null) {
								p.flash.fadeOut();
							}
						}
					}
					fadedFlash = true;
				}
				
				Vector2f loc = ship.getLocation();
				float dist = Misc.getDistance(loc, dest);
				
				//Vector2f perp = new Vector2f(-dir.y, dir.x);
				
				float friction = FRICTION;
				float k = SPRING_CONSTANT;
				float freeLength = 0f;
				float stretch = dist - freeLength;
	
				float forceMag = k * Math.abs(stretch);
				
				float forceMagReduction = Math.min(Math.abs(forceMag), friction);
				forceMag -= forceMagReduction;
				friction -= forceMagReduction;
				
				Vector2f force = new Vector2f(dir);
				force.scale(forceMag * Math.signum(stretch));
				
				Vector2f acc = new Vector2f(force);
				acc.scale(amount);
				Vector2f.add(ship.getVelocity(), acc, ship.getVelocity());
			}
			
		}
		
		if (state == State.OUT) {
			dest = null;
			fadedFlash = false;
		} else if (state == State.ACTIVE) {
		}
		
		if (effectLevel > 0.85f && state != State.OUT) {
			if (ship.getShield() != null) {
				if (ship.getShield().isOn()) {
					ship.getShield().toggleOff();
				}
				ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
			}
		}
		
		
		if (effectLevel > 0f) {
		//if (state == State.OUT) {
			ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
			ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
			ship.giveCommand(ShipCommand.DECELERATE, null, 0);
		}
		
		//stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
	
		
		//ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
		//ship.getAIFlags().setFlag(AIFlags.DO_NOT_VENT, 1f);
		//ship.getAIFlags().setFlag(AIFlags.IGNORES_ORDERS, 1f);
	}
	
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
	
}








