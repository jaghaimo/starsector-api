package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class PilumOnHitEffect implements OnHitEffectPlugin {


	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (!(target instanceof ShipAPI)) return;
		ShipAPI ship = (ShipAPI) target;
		float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
		pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
		
		boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
		//piercedShield = true;
		
		if (!shieldHit || piercedShield) {
			float emp = projectile.getEmpAmount();
			float dam = 0;
			engine.spawnEmpArcPierceShields(
							   projectile.getSource(), point, target, target,
							   DamageType.ENERGY, 
							   dam, // damage
							   emp, // emp 
							   100000f, // max range 
							   "tachyon_lance_emp_impact",
							   20f, // thickness
							   //new Color(25,100,155,255),
							   //new Color(255,255,255,255)
							   new Color(125,125,100,255),
							   new Color(255,255,255,255)
							   );
		}
	}
}
