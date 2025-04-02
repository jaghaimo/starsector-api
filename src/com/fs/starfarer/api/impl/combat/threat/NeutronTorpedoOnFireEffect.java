package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class NeutronTorpedoOnFireEffect implements OnFireEffectPlugin {
	public static float VEL_MULT = 0.33f;
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		projectile.getVelocity().scale(VEL_MULT);
	}
}
