package com.fs.starfarer.api.impl.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * UNUSED
 */
public class VPDriverEffect implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

	public static float MIN_COOLDOWN = Global.getSettings().getFloat("minRefireDelay");
	public static int MAX_SHOTS = 4;
	
	public VPDriverEffect() {
	}
	
	protected List<DamagingProjectileAPI> shots;
	//protected int hits = 0;
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (shots == null) return;
		
//		if (getWeaponEffect(weapon) == null) {
//			Global.getCombatEngine().getCustomData().put(getWeaponEffectId(weapon), this);
//		}
		Iterator<DamagingProjectileAPI> iter = shots.iterator();
		while (iter.hasNext()) {
			DamagingProjectileAPI shot = iter.next();
			if (shot.isExpired() || shot.isFading()) iter.remove();
		}
		
		if (weapon.getCooldownRemaining() > MIN_COOLDOWN) {
//			float flightTime = weapon.getRange() / weapon.getProjectileSpeed();
//			float fireCycle = (weapon.getCooldown() + weapon.getSpec().getChargeTime()) * getRoFMult(weapon);
//			
//			int maxShots = Math.round(flightTime / fireCycle);
//			if (maxShots < 2) maxShots = 2;
			
			//System.out.println("MAX SHOTS: " + maxShots);
			
			//if (shots.size() < maxShots) {
			if (shots.size() < MAX_SHOTS) {
				weapon.setRemainingCooldownTo(MIN_COOLDOWN);
			}
		}
	}
	
	public static float getRoFMult(WeaponAPI weapon) {
		ShipAPI ship = weapon.getShip();
		if (ship == null) return 1f;
		
		float rofMult = 1f;
		switch (weapon.getSpec().getType()) {
		case BALLISTIC: 
			rofMult = ship.getMutableStats().getBallisticRoFMult().getModifiedValue(); 
			break;
		case MISSILE: 
			rofMult = ship.getMutableStats().getMissileRoFMult().getModifiedValue(); 
			break;
		case ENERGY: 
			rofMult = ship.getMutableStats().getEnergyRoFMult().getModifiedValue();
			break;
		}
		return rofMult;
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		if (shots == null) {
			shots = new ArrayList<DamagingProjectileAPI>();
		}
		shots.add(0, projectile);
	}
	
	
//	public String getWeaponEffectId(WeaponAPI weapon) {
//	String id = weapon.getShip().getId() + "_" + weapon.getSlot().getId() + "_" + weapon.getId();
//	return id;
//}
//public VPDriverEffect getWeaponEffect(WeaponAPI weapon) {
//	String id = getWeaponEffectId(weapon);
//	return (VPDriverEffect) Global.getCombatEngine().getCustomData().get(id);
//}
}




