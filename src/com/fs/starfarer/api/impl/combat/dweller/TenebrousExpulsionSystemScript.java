package com.fs.starfarer.api.impl.combat.dweller;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.dweller.VortexLauncherEffect.DisturbShroudPlugin;
import com.fs.starfarer.api.util.Misc;

public class TenebrousExpulsionSystemScript extends BaseShipSystemScript {
	
	public static String EJECTA_WING = "shrouded_ejecta_wing";
	
	public static float REFIRE_DELAY = 0.15f;
	public static float LAUNCH_ARC = 90f;
	public static float BACK_OFF_ACCEL = 500f;
	
	protected Vector2f fireDir;
	protected float elapsedActive = REFIRE_DELAY;
	
	protected void init(ShipAPI ship) {
	}
	
	
	@Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		if (state == State.IN) {
			if (fireDir == null) {
				float dir = ship.getFacing();
				if (ship.getMouseTarget() != null) {
					dir = Misc.getAngleInDegrees(ship.getLocation(), ship.getMouseTarget());
				}
				if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)){
					dir = Misc.getAngleInDegrees(ship.getLocation(), 
							(Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS));
				}
				fireDir = Misc.getUnitVectorAtDegreeAngle(dir);
			}
		}

		
		if (state == State.ACTIVE && fireDir != null) {
			float amount = Global.getCombatEngine().getElapsedInLastFrame();
			elapsedActive += amount;
			while (elapsedActive >= REFIRE_DELAY) {
				elapsedActive -= REFIRE_DELAY;
				fireShroudedEjecta(ship, fireDir);
			}
		}
		
		if (state == State.OUT && fireDir != null) {
			float amount = Global.getCombatEngine().getElapsedInLastFrame();
			ship.getVelocity().x -= fireDir.x * BACK_OFF_ACCEL * amount;			
			ship.getVelocity().y -= fireDir.y * BACK_OFF_ACCEL * amount;			
		}
		
		if (state == State.IDLE || state == State.COOLDOWN) {
			fireDir = null;
			elapsedActive = REFIRE_DELAY;
		}
		
	}
	
	public static void fireShroudedEjecta(ShipAPI ship, Vector2f fireDir) {
		float dir = Misc.getAngleInDegrees(fireDir);
		CombatEngineAPI engine = Global.getCombatEngine();
		CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOriginalOwner());
		manager.setSuppressDeploymentMessages(true);
		
		// wing has size 1, so no other members besides the "leader"
		// point is to spawn a ship but without it showing up in the deployment dialog etc
		ShipAPI ejecta = manager.spawnShipOrWing(EJECTA_WING, 
								ship.getLocation(), dir, 0f, null);
		manager.setSuppressDeploymentMessages(false);
		
		Vector2f takeoffVel = Misc.getUnitVectorAtDegreeAngle(
								dir + LAUNCH_ARC/2f - (float) Math.random() * LAUNCH_ARC);
		float velMult = 1f;
		velMult = 0.5f + (float) Math.random() * 1f;
		takeoffVel.scale(ejecta.getMaxSpeed() * velMult);
		
		Vector2f.add(ejecta.getVelocity(), takeoffVel, ejecta.getVelocity());
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ejecta);
		if (shroud != null) {
			shroud.custom1 = ship;
		}
		
		DwellerShroud sourceShroud = DwellerShroud.getShroudFor(ship);
		if (sourceShroud != null) {
			Vector2f offset = Vector2f.sub(ejecta.getLocation(), sourceShroud.getAttachedTo().getLocation(), new Vector2f());
			Global.getCombatEngine().addPlugin(
					new DisturbShroudPlugin(1f, fireDir, ejecta, offset, sourceShroud, 
							(int) (sourceShroud.getNumMembersToMaintain() * 0.1f)));
			
		}
		
		Global.getSoundPlayer().playSound("system_tenebrous_expulsion_fire", 1f, 1f, ejecta.getLocation(), ejecta.getVelocity());
	}
	
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
	
}








