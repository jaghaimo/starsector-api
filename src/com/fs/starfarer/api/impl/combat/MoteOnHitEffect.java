package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class MoteOnHitEffect implements OnHitEffectPlugin {

//	public static float ANTI_FIGHTER_DAMAGE = 500;
//	public static float ANTI_FIGHTER_DAMAGE_HF = 1000;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		boolean withEMP = false;
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (!ship.isFighter()) {
				float pierceChance = 1f;
				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
				boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
				
				if (!shieldHit || piercedShield) {
					float emp = projectile.getEmpAmount();
					float dam = projectile.getDamageAmount(); // this should be 1 for regular and a bunch for high-frequency
					
					engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
									   projectile.getDamageType(), 
									   dam,
									   emp, // emp 
									   100000f, // max range 
									   "mote_attractor_impact_emp_arc",
									   20f, // thickness
									   //new Color(100,165,255,255),
									   MoteControlScript.getEMPColor(projectile.getSource()),
									   new Color(255,255,255,255)
									   );
					withEMP = true;
				}
				
				//ship.getFluxTracker().increaseFlux(FLUX_RAISE_AMOUNT, shieldHit);
				
			} else {
//				float damage = ANTI_FIGHTER_DAMAGE;
//				if (MoteControlScript.isHighFrequency(projectile.getSource())) {
//					damage = ANTI_FIGHTER_DAMAGE_HF;
//				}
				float damage = MoteControlScript.getAntiFighterDamage(projectile.getSource());
				Global.getCombatEngine().applyDamage(projectile, ship, point, 
						damage, DamageType.ENERGY, 0f, false, false, projectile.getSource(), true);
			}
		} else if (target instanceof MissileAPI) {
			float damage = MoteControlScript.getAntiFighterDamage(projectile.getSource());
			Global.getCombatEngine().applyDamage(projectile, target, point, 
					damage, DamageType.ENERGY, 0f, false, false, projectile.getSource(), true);
		}
		
		//if (!withEMP) {
			String impactSoundId = MoteControlScript.getImpactSoundId(projectile.getSource());
			Global.getSoundPlayer().playSound(impactSoundId, 1f, 1f, point, new Vector2f());
			//Global.getSoundPlayer().playSound("hit_glancing_energy", 1f, 1f, point, new Vector2f());
		//}
	}
}



