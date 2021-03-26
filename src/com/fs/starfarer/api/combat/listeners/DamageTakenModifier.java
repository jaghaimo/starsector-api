package com.fs.starfarer.api.combat.listeners;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;

/**
 * When adding to a ship, add to the ship taking the damage, NOT the one dealing it.
 * 
 * Should add this to a ship where possible instead of the engine as performance will be better.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public interface DamageTakenModifier {
	
	/**
	 * Modifications to damage should ONLY be made using damage.getModifier().
	 * 
	 * param can be:
	 * null
	 * DamagingProjectileAPI
	 * BeamAPI
	 * EmpArcEntityAPI
	 * Something custom set by a script
	 * 
	 * @return the id of the stat modification to damage.getModifier(), or null if no modification was made
	 */
	String modifyDamageTaken(Object param, CombatEntityAPI target, 
						DamageAPI damage, Vector2f point, boolean shieldHit);
	
}
