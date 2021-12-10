package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class CanisterFlakPlugin implements OnFireEffectPlugin {

	public CanisterFlakPlugin() {
	}

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float speedMult = 0.25f + 0.75f * (float) Math.random();
		projectile.getVelocity().scale(speedMult);
		
		float angVel = (float) (Math.signum((float) Math.random() - 0.5f) * 
						(0.5f + Math.random()) * 720f);
		projectile.setAngularVelocity(angVel);
		
		if (projectile instanceof MissileAPI) {
			MissileAPI missile = (MissileAPI) projectile;
			float flightTimeMult = 0.25f + 0.75f * (float) Math.random();
			missile.setMaxFlightTime(missile.getMaxFlightTime() * flightTimeMult);
		}
		
		if (weapon != null) {
			float delay = 0.25f + 0.75f * (float) Math.random();
			weapon.setRefireDelay(delay);
		}
		
	}
}




