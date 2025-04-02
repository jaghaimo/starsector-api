package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * A weapon that can not be manually controlled and launches swarms automatically.
 *
 */
public interface SwarmLaunchingWeapon {
	int getPreferredNumFragmentsToFire(WeaponAPI weapon);
}
