package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.RoilingSwarmParams;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;

public class KineticFragmentMissileEffect extends BaseFragmentMissileEffect {

	public static Color FRINGE_COLOR = new Color(235,255,215,235);
	public static Color CORE_COLOR = new Color(225,255,205,200);
	
	public static float OTHER_FRAGMENT_DAMAGE = 100;
	
	protected void configureMissileSwarmParams(RoilingSwarmParams params) {
//		params.flashFringeColor = new Color(235,255,215,235);
//		params.flashCoreColor = new Color(225,255,205,200);
		
//		params.baseSpringConstant *= 2f;
//		params.springConstantNegativeRange *= 2f;
		params.maxOffset = 40f;
		
		params.maxSpeed = missile.getMaxSpeed() + 10f;
		params.outspeedAttachedEntityBy = 0f;
		
		//params.flashFringeColor = Misc.setAlpha(FRINGE_COLOR, 50);
		params.flashFringeColor = FRINGE_COLOR;
		params.flashCoreColor = CORE_COLOR;
		
		params.flashCoreRadiusMult = 0f;
		//params.renderFlashOnSameLayer = true;
		params.flashRadius = 30f;
		params.autoscale = true;
		//params.preFlashDelay = 0.5f * (float) Math.random();
		
//		params.flashFrequency = 40f;
//		params.flashProbability = 1f;
	}
	
	protected void swarmCreated(MissileAPI missile, RoilingSwarmEffect missileSwarm, RoilingSwarmEffect sourceSwarm) {
		boolean first = true;
		for (SwarmMember p : missileSwarm.members) {
			p.scaler.setBrightness(p.scale);
			if (first) {
				p.scaler.setBounceDown(false);
				p.scaler.fadeIn();
			} else {
//				p.scaler.setBounceUp(false);
//				p.scaler.fadeOut();
				p.keepScale = true;
			}
			p.flash = null;
			p.flash();
			p.flash.setBounceDown(false);
			first = false;
		}
	}
	
	protected int getNumOtherMembersToTransfer() {
		return 4;
	}
	
	protected int getEMPResistance() {
		return 3;
	}
	
	protected boolean explodeOnFizzling() {
		return false;
	}
	
	protected boolean shouldMakeMissileFaceTargetOnSpawnIfAny() {
		return true;
	}
	
	@Override
	protected FragmentBehaviorOnImpact getOtherFragmentBehaviorOnImpact() {
		return FragmentBehaviorOnImpact.STOP_AND_FADE;
	}

	protected void reportFragmentHit(MissileAPI missile, SwarmMember p, RoilingSwarmEffect swarm, CombatEntityAPI target) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Color color = FRINGE_COLOR;
		//color = Misc.setAlpha(color, 255);
		float size = 80f; // not radius
		engine.addHitParticle(p.loc, new Vector2f(), size, 0.5f, color);
		engine.addHitParticle(p.loc, new Vector2f(), size * 0.25f, 1f, CORE_COLOR);
		
		float mult = p.fader.getBrightness();
		if (mult > 0.8f) mult = 1f;
		engine.applyDamage(target, p.loc, OTHER_FRAGMENT_DAMAGE * mult, DamageType.KINETIC, 0f,
				false, false, missile.getSource(), true);
		
//		engine.addNegativeParticle(p.loc, new Vector2f(), 300f, 0f, 0.5f, Color.white);
	}


//	protected String getExplosionSoundId() {
//		return "devastator_explosion";
//	}

}








