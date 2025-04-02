package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.RoilingSwarmParams;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.util.Misc;

public class DevouringSwarmMissileEffect extends BaseFragmentMissileEffect implements OnHitEffectPlugin {

	public static String DISMANTLING_SWARM = "dismantling_swarm";
	
	public static int NUM_TICKS = 11; // about 10 seconds (duration of damage over time effect)
	
	public static class DismantlingFragmentBaseDamageNegator implements DamageDealtModifier {
		@Override
		public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point,
				boolean shieldHit) {
			if (shieldHit) {
				return null;
			}
			if (param instanceof MissileAPI) {
				MissileAPI missile = (MissileAPI) param;
				RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(missile);
				if (swarm != null && swarm.params.tags.contains(DISMANTLING_SWARM)) {
					damage.getModifier().modifyMult(DISMANTLING_SWARM, 0f);
					return DISMANTLING_SWARM;
				}
			}
			return null;
		}
	}
	
	
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		super.onFire(projectile, weapon, engine);
		if (weapon.getShip() != null && 
				!weapon.getShip().hasListenerOfClass(DismantlingFragmentBaseDamageNegator.class)) {
			weapon.getShip().addListener(new DismantlingFragmentBaseDamageNegator());
		}
	}
	
	protected void configureMissileSwarmParams(RoilingSwarmParams params) {
//		params.flashFringeColor = new Color(183,65,13,255);
//		params.flashCoreColor = Color.white;
//		params.flashRadius = 40f;
//		params.flashCoreRadiusMult = 0.75f;
		
		params.tags.add(DISMANTLING_SWARM);
		
//		params.flashFringeColor = new Color(183,65,13,80);
//		params.flashCoreColor = new Color(183,65,13,127);
		params.flashFringeColor = new Color(130,165,50,50);
		params.flashCoreColor = new Color(130,165,50,127);
		//params.flashCoreColor = new Color(50,165,50,127);
		
//		params.flashFringeColor = new Color(100,165,100,127);
//		params.flashCoreColor = Color.white;
		
		
		//params.flashCoreColor = new Color(183,65,13,127);
		params.flashCoreRadiusMult = 0f;
		params.renderFlashOnSameLayer = true;
		params.flashRadius = 40f;
		params.preFlashDelay = 0.5f * (float) Math.random();
		
		params.flashFrequency = 40f;
		params.flashProbability = 1f;
	}
	
	protected int getNumOtherMembersToTransfer() {
		return 5;
		//return 0;
	}
	
	@Override
	protected int getNumOtherMembersToAdd() {
		return 0;
		//return 5;
	}

	protected int getEMPResistance() {
		return 1;
	}
	
	protected boolean explodeOnFizzling() {
		return false;
	}
	
	@Override
	protected FragmentBehaviorOnImpact getOtherFragmentBehaviorOnImpact() {
		return FragmentBehaviorOnImpact.STOP_AND_FADE;
	}

	protected void reportFragmentHit(MissileAPI missile, SwarmMember p, RoilingSwarmEffect swarm, CombatEntityAPI target) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Color color = swarm.params.flashFringeColor;
		color = Misc.setAlpha(color, 255);
		float size = swarm.params.flashRadius * 2f;
		engine.addHitParticle(p.loc, new Vector2f(), size, 0.5f, color);
		engine.addHitParticle(p.loc, new Vector2f(), size * 0.25f, 1f, Color.white);
	}

	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (shieldHit) return;
		if (projectile.getDamageAmount() <= 0f) return;
		//if (projectile.isFading()) return;
		if (!(target instanceof ShipAPI)) return;
		
		ShipAPI source = projectile.getSource();
		RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(source);
		
		Vector2f offset = Vector2f.sub(point, target.getLocation(), new Vector2f());
		offset = Misc.rotateAroundOrigin(offset, -target.getFacing());
		DisintegratorEffect effect = new DisintegratorEffect(projectile, (ShipAPI) target, offset) {
			protected float getTotalDamage() {
				return projectile.getDamageAmount();
			}
			protected int getNumTicks() {
				return NUM_TICKS;
			}
			protected boolean canDamageHull() {
				return true;
			}
			protected int getNumParticlesPerTick() {
				return 5;
			}
			protected String getSoundLoopId() {
				return "devouring_swarm_loop";
			}
			protected void addParticle() {
				ParticleData p = new ParticleData(25f, 3f + (float) Math.random() * 2f, 1f);
				p.color = new Color(125,100,200,25);
//				p.color = RiftLanceEffect.getColorForDarkening(VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR);
//				p.color = Misc.setAlpha(p.color, 25);
				particles.add(p);
				p.offset = Misc.getPointWithinRadius(p.offset, 10f);
			}
			protected void damageDealt(Vector2f loc, float hullDamage, float armorDamage) {
				if (sourceSwarm == null || source == null || !source.isAlive()) return;
				if (source.isFighter()) return;
				
				if ((float) Math.random() < 0.25f) return;
				if (hullDamage > 0 || armorDamage > 0) {
					SwarmMember p = sourceSwarm.addMember();
					p.loc.set(loc);
					p.fader.setDurationIn(0.3f);
				}
			}
		};
		CombatEntityAPI e = engine.addLayeredRenderingPlugin(effect);
		e.getLocation().set(projectile.getLocation());
		
		if (projectile instanceof MissileAPI) {
			MissileAPI missile = (MissileAPI) projectile;
			missile.setDidDamage(true);
			Global.getSoundPlayer().playSound("devouring_swarm_hit_ship", 1f, 1f, point,
									missile.getVelocity());
		}
	}

	protected boolean withEMPArc() {
		return super.withEMPArc();
	}
	
	protected Color getEMPFringeColor() {
		Color c = weapon.getSpec().getGlowColor();
		c = Misc.scaleColorOnly(c, 0.5f);
		return c;
		//return super.getEMPFringeColor();
	}
	
	protected Color getEMPCoreColor() {
		Color c = Color.white;
		c = Misc.scaleColorOnly(c, 0.5f);
		return c;
		//return super.getEMPCoreColor();
	}
	
//	protected String getExplosionSoundId() {
//		return "devastator_explosion";
//	}

}








