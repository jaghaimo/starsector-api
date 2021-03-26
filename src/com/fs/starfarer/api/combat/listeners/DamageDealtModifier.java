package com.fs.starfarer.api.combat.listeners;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;

/**
 * When adding to a ship, add to the ship dealing the damage, NOT the one taking the damage.
 * 
 * Should add this to a ship where possible instead of the engine as performance will be better.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public interface DamageDealtModifier {
	
	/**
	 * Passed in as param to modifyDamageDealt/Taken when the damage dealer is an EMP ship system,
	 * which does not create EmpArcEntityAPI's for historical reasons. 
	 */
	public static String EMP_SHIP_SYSTEM_PARAM = "EMP_SHIP_SYSTEM_PARAM";
	
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
	String modifyDamageDealt(Object param, CombatEntityAPI target, 
						DamageAPI damage, Vector2f point, boolean shieldHit);
	
}
