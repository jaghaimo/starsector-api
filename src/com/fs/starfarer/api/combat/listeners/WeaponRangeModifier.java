package com.fs.starfarer.api.combat.listeners;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

/**
 * Only applies when added to ships, NOT the combat engine, for performance reasons.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public interface WeaponRangeModifier {
	float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon);
	float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon);
	float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon);
}
