package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.RoilingSwarmParams;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;

public class UnstableFragmentMissileEffect extends BaseFragmentMissileEffect {

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		super.onFire(projectile, weapon, engine);
	}
	
	protected void configureMissileSwarmParams(RoilingSwarmParams params) {
		params.flashFringeColor = new Color(255,50,50,255);
		params.flashCoreColor = Color.white;
		params.flashRadius = 100f;
		//params.flashCoreRadiusMult = 0.75f;
	}
	
	protected int getNumOtherMembersToTransfer() {
		return 0;
	}
	
	protected int getEMPResistance() {
		return 0;
	}
	
	protected boolean explodeOnFizzling() {
		return false; // handled by the proximity fuse AI
	}

	@Override
	protected boolean makePrimaryFragmentGlow() {
		return false;
	}
	
	@Override
	protected SwarmMember pickPrimaryFragment() {
		if (missile.getAI() instanceof GuidedMissileAI) {
			GuidedMissileAI ai = (GuidedMissileAI) missile.getAI();
			if (ai.getTarget() != null) {
				return pickOuterFragmentWithinRangeClosestTo(150f, ai.getTarget().getLocation());
			}
		}
		return super.pickPrimaryFragment();
	}

	@Override
	protected void swarmAdvance(float amount, MissileAPI missile, RoilingSwarmEffect swarm) {
		if (swarm.custom1 == null) {
			swarm.custom1 = Global.getCombatEngine().createProximityFuseAI(missile);
		}
		((MissileAIPlugin)swarm.custom1).advance(amount);
		
		if (!swarm.members.isEmpty()) {
			SwarmMember primary = swarm.members.get(0);
			if (primary.flash == null) {
				primary.flash();
				primary.flash.setDuration(0.25f, 0.75f);
				primary.flash.setBounce(true, true);
			}
		}
	}
	
	

//	protected String getExplosionSoundId() {
//		return "devastator_explosion";
//	}

	
}








