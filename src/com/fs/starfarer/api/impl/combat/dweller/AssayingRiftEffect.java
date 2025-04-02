package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.impl.combat.RiftTrailEffect;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per missile) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class AssayingRiftEffect implements OnFireEffectPlugin, OnHitEffectPlugin, EveryFrameWeaponEffectPlugin {

	public static String HUNGERING_RIFT_HEAL_MULT_STAT = "hungering_rift_heal_mult_stat";
	
	public static float HEAL_AMOUNT = 1000f;
	
	//public static float HITPOINTS_MULT_WHEN_BY_DWELLER_SHIP = 2f;
	
	public static String ASSAYING_RIFT = "assaying_rift";
	
	/**
	 * One Hungering Rift weapon can produce up to 5 or so rifts at a time if they're fired non-stop and don't hit anything early.
	 */
	public static int MAX_RIFTS = 10; 
	
	
	public static class AssayingRiftCount {
		int count = 0;
		float totalElapsed = 0;
		
		public void update() {
			float elapsed = Global.getCombatEngine().getTotalElapsedTime(false);
			if (totalElapsed >= elapsed) return;
			
			totalElapsed = elapsed;
			
			count = 0;
			for (MissileAPI m : Global.getCombatEngine().getMissiles()) {
				if (m.hasTag(ASSAYING_RIFT)) {
					count++;
				}
			}
		}
	}
	

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (weapon.getShip() != null && weapon.getShip().getHullSpec().hasTag(Tags.DWELLER)) {
			return;
		}
		
		String key = "AssayingRiftSharedDataKey";
		AssayingRiftCount data = (AssayingRiftCount) engine.getCustomData().get(key);
		if (data == null) {
			data = new AssayingRiftCount();
			engine.getCustomData().put(key, data);
		}
		
		data.update();
		
		boolean disable = data.count >= MAX_RIFTS;
		weapon.setForceDisabled(disable);		
	}	
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		Color color = RiftCascadeEffect.STANDARD_RIFT_COLOR;
		Object o = projectile.getWeapon().getSpec().getProjectileSpec();
		if (o instanceof MissileSpecAPI) {
			MissileSpecAPI spec = (MissileSpecAPI) o;
			color = spec.getExplosionColor();
		}
		
		if (!shieldHit && target instanceof ShipAPI) {
			ShipAPI targetShip = (ShipAPI) target;
			ShipAPI source = projectile.getSource();
			
//			DwellerShroud shroud = DwellerShroud.getShroudFor(source);
//			if (shroud != null && source != null && !source.isHulk() && !targetShip.isHulk()) {
//				source.setHitpoints(Math.min(source.getMaxHitpoints(), source.getHitpoints() + HEAL_AMOUNT));
//			}
			
			if (!targetShip.isHulk()) {
				WeightedRandomPicker<ShipAPI> healTargets = new WeightedRandomPicker<>();
				WeightedRandomPicker<ShipAPI> healNeedLess = new WeightedRandomPicker<>();
				for (ShipAPI other : Global.getCombatEngine().getShips()) {
					if (other.isHulk()) continue;
					if (other.isFighter()) continue;
					if (other.getOwner() != source.getOwner()) continue;
					
					DwellerShroud otherShroud = DwellerShroud.getShroudFor(source);
					if (otherShroud == null) continue;
					
					float missingHp = other.getMaxHitpoints() - other.getHitpoints();
					if (missingHp < HEAL_AMOUNT * 0.7f && missingHp > 0f) {
						healNeedLess.add(other, missingHp);
					} else {
						healTargets.add(other, missingHp);
					}
				}
				
				ShipAPI toHeal = healTargets.pick();
				if (toHeal == null) toHeal = healNeedLess.pick();
				if (toHeal != null) {
					float healAmount = HEAL_AMOUNT;
					healAmount *= toHeal.getMutableStats().getDynamic().getValue(HUNGERING_RIFT_HEAL_MULT_STAT);
					toHeal.setHitpoints(Math.min(toHeal.getMaxHitpoints(), toHeal.getHitpoints() + healAmount));
				}
			}
		}
		
		
		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color, 15f);
		p.fadeOut = 1f;
		p.hitGlowSizeMult = 1f;
		//p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
		RiftCascadeMineExplosion.spawnStandardRift(projectile, p);
		
		Vector2f vel = new Vector2f();
		if (target != null) vel.set(target.getVelocity());
		Global.getSoundPlayer().playSound("assaying_rift_explosion", 1f, 1f, point, vel);
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		MissileAIPlugin proxAI = Global.getCombatEngine().createProximityFuseAI((MissileAPI)projectile);
		RiftTrailEffect trail = new RiftTrailEffect((MissileAPI) projectile, null) {
			boolean exploded = false;
			float elapsed = 0f;
			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				super.advance(amount, events);
				proxAI.advance(amount);
				if (!exploded && !missile.didDamage() && missile.wasRemoved()) {// !engine.isMissileAlive(missile)) {
					onHit(missile, null, missile.getLocation(), false, null, engine);
					exploded = true;
				}
				
//				if (!exploded) {
//					elapsed += amount;
//					if (elapsed > 1f) {
//						float speedBonus = Math.min(100f, (elapsed - 1f) * 100f);
//						String id = "AssayingRiftSpeedBonus";
//						missile.getEngineStats().getMaxSpeed().modifyFlat(id, speedBonus);
//						missile.getEngineStats().getAcceleration().modifyFlat(id, speedBonus * 2f);
//						missile.getEngineStats().getDeceleration().modifyFlat(id, speedBonus * 2f);
//						missile.updateMaxSpeed();
//					}
//				}
			}
			protected Color getUndercolor() {
				//return new Color(100, 0, 20, 255);
				return DwellerShroud.SHROUD_COLOR;
			}
			protected Color getDarkeningColor() {
				return RiftLanceEffect.getColorForDarkening(getUndercolor());
			}
			@Override
			protected float getBaseParticleDuration() {
				return 1.5f;
			}
			
			
		};
		
		MissileAPI missile = ((MissileAPI) projectile);
		
		missile.setEmpResistance(1000);
		missile.setEccmChanceOverride(1f);
		missile.addTag(ASSAYING_RIFT);
		
//		if (weapon.getShip().getHullSpec().hasTag(Tags.DWELLER)) {
//			missile.setHitpoints(missile.getHitpoints() * HITPOINTS_MULT_WHEN_BY_DWELLER_SHIP);
//		}
		
		Global.getCombatEngine().addPlugin(trail);
	}

}














