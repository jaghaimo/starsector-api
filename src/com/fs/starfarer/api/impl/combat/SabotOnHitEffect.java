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

public class SabotOnHitEffect implements OnHitEffectPlugin {


	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if ((float) Math.random() < 0.25f && !shieldHit && target instanceof ShipAPI) {
			
			float emp = projectile.getEmpAmount();
			//emp *= 0.5f;
			float dam = 0;
			
			engine.spawnEmpArc(projectile.getSource(), point, target, target,
							   DamageType.ENERGY, 
							   dam,
							   emp, // emp 
							   100000f, // max range 
							   "tachyon_lance_emp_impact",
							   20f, // thickness
//							   new Color(70,100,155,255),
//							   new Color(255,255,255,255)
							   new Color(25,100,155,255),
							   new Color(255,255,255,255)
//								new Color(235,255,215,70),
//								new Color(255,255,255,255)
							   );
			
			//engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
		}
	}
}
