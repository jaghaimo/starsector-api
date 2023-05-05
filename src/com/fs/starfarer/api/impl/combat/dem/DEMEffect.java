package com.fs.starfarer.api.impl.combat.dem;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * 
 */
public class DEMEffect implements OnFireEffectPlugin {
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		if (!(projectile instanceof MissileAPI)) return;
		
		MissileAPI missile = (MissileAPI) projectile;
		
		ShipAPI ship = null;
		if (weapon != null) ship = weapon.getShip();
		if (ship == null) return;
		
		DEMScript script = new DEMScript(missile, ship, weapon);
		Global.getCombatEngine().addPlugin(script);
	}
	
}




