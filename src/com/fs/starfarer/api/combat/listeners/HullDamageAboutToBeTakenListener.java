package com.fs.starfarer.api.combat.listeners;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;

/**
 * Should add this to a ship where possible instead of the engine as performance will be better.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public interface HullDamageAboutToBeTakenListener {
	
	/**
	 * if false is returned, the hull damage to be taken is negated.
	 *  
	 * @param param
	 * @param ship
	 * @param point
	 * @param damageAmount
	 * @return
	 */
	boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount);
	
}
