package com.fs.starfarer.api.combat.listeners;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

/**
 * Only applies when added to MutableShipStatsAPI, not to ShipAPI or the combat engine.
 * 
 * If multiple different ones are added to the same ship stats, they might have a hard time coordinating properly.
 * Should be used with care, and ideally only through built-in hullmods.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public interface WeaponOPCostModifier {
	int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost);
}
