package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class VortexLauncherEffect implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

	//public static String VORTEX_VARIANT = "shrouded_vortex_Churning";
	public static String VORTEX_WING = "shrouded_vortex_wing";
	
	public static float LAUNCH_ARC = 60f;

	public static class DisturbShroudPlugin extends BaseEveryFrameCombatPlugin {
		float elapsed = 0f;
		float dur;
		Vector2f dir;
		DwellerShroud shroud;
		int numMembers;
		Vector2f offset;
		CombatEntityAPI disturber;
		
		public DisturbShroudPlugin(float dur, Vector2f dir, CombatEntityAPI disturber, Vector2f offset, DwellerShroud shroud, int numMembers) {
			this.dur = dur;
			this.dir = dir;
			this.disturber = disturber;
			this.offset = offset;
			this.shroud = shroud;
			this.numMembers = numMembers;
		}
			
		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			if (Global.getCombatEngine().isPaused()) return;
		
			elapsed += amount;
			if (elapsed > dur) {
				CombatEngineAPI engine = Global.getCombatEngine();
				engine.removePlugin(this);
				return;
			}
			
			Vector2f loc = Vector2f.add(shroud.getAttachedTo().getLocation(), offset, new Vector2f());
			
			Vector2f useDir = dir;
			if (disturber != null) {
				useDir = new Vector2f(disturber.getVelocity());
				useDir = Misc.normalise(useDir);
			}
			
			WeightedRandomPicker<SwarmMember> picker = shroud.getPicker(false, true, loc, 70f);
			for (int i = 0; i < numMembers; i++) {
				SwarmMember pick = picker.pickAndRemove();
				if (pick == null) break;
				float accel = ConvulsiveLungeSystemScript.PARTICLE_WINDUP_ACCEL * amount * 3f;
				accel *= 1f + (float) Math.random();
				pick.vel.x += useDir.x * accel;
				pick.vel.y += useDir.y * accel;
			}
	
		}
	}
	
	public VortexLauncherEffect() {
		
	}
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		ShipAPI ship = weapon.getShip();
		if (ship == null) return;

		//weapon.setForceFireOneFrame(true);
	}
	

	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
		//FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(ATTACK_SWARM_WING);

		CombatFleetManagerAPI manager = engine.getFleetManager(projectile.getOwner());
		manager.setSuppressDeploymentMessages(true);
		// wing has size 1, so no other members besides the "leader"
		// point is to spawn a ship but without it showing up in the deployment dialog etc
		ShipAPI vortex = manager.spawnShipOrWing(VORTEX_WING, 
								projectile.getLocation(), projectile.getFacing(), 0f, null);
		manager.setSuppressDeploymentMessages(false);
		
		Vector2f takeoffVel = Misc.getUnitVectorAtDegreeAngle(
				projectile.getFacing() + LAUNCH_ARC/2f - (float) Math.random() * LAUNCH_ARC);
		float velMult = 1f;
		if (Misc.getAngleDiff(projectile.getFacing(), weapon.getShip().getFacing()) > 150f) {
			velMult = 0.5f;
		}
		takeoffVel.scale(vortex.getMaxSpeed() * velMult);
		
		Vector2f.add(vortex.getVelocity(), takeoffVel, vortex.getVelocity());
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(vortex);
		if (shroud != null) {
			shroud.custom1 = weapon.getShip();
		}
		
		DwellerShroud sourceShroud = DwellerShroud.getShroudFor(weapon.getShip());
		if (sourceShroud != null) {
			
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing());
			Vector2f offset = Vector2f.sub(projectile.getLocation(), sourceShroud.getAttachedTo().getLocation(), new Vector2f());
			Global.getCombatEngine().addPlugin(
					new DisturbShroudPlugin(1f, dir, vortex, offset, sourceShroud, 
							(int) (sourceShroud.getNumMembersToMaintain() * 0.1f)));
					//new DisturbShroudPlugin(1f, dir, offset, sourceShroud, 10));
			
		}
		
		engine.removeEntity(projectile);
	}
	
	
	
}








