package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.RoilingSwarmParams;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;

public class SeekerFragmentMissileEffect extends BaseFragmentMissileEffect {

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		super.onFire(projectile, weapon, engine);
	}
	
	protected void configureMissileSwarmParams(RoilingSwarmParams params) {
//		params.flashFringeColor = new Color(255,50,50,255);
//		params.flashFringeColor = new Color(255,165,30,255);
		params.flashFringeColor = new Color(255,255,50,255);
		params.flashCoreColor = Color.white;
		params.flashRadius = 70f;
		params.flashCoreRadiusMult = 0.75f;
	}
	
	protected void swarmCreated(MissileAPI missile, RoilingSwarmEffect missileSwarm, RoilingSwarmEffect sourceSwarm) {
		if (!missileSwarm.members.isEmpty()) {
			SwarmMember p = missileSwarm.members.get(0);
			p.scaler.setBrightness(p.scale);
			p.scaler.setBounceDown(false);
			p.scaler.fadeIn();
		}
	}
	
	protected int getNumOtherMembersToTransfer() {
		return 9;
		//return 0;
		//return 12;
	}
	
	protected int getEMPResistance() {
		return 3;
	}
	
	protected boolean explodeOnFizzling() {
		return false;
	}

	
	
//	protected String getExplosionSoundId() {
//		return "devastator_explosion";
//	}

	
}








