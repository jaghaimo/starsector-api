package com.fs.starfarer.api.combat.listeners;

/**
 * Only supported when added to a ShipAPI, not to CombatEngineAPI.
 * 
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public interface AdvanceableListener {
	void advance(float amount);
}
