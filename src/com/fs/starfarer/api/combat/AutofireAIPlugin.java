package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

public interface AutofireAIPlugin {

	/**
	 * Only called when the group is on autofire.
	 * 
	 * Should generally make the decision on what to fire at
	 * and whether to fire here, and then return the result of that decision in shouldFire().
	 *
	 * @param amount seconds since last frame.
	 */
	void advance(float amount);
	
	/**
	 * Only called when the group is on autofire.
	 * 
	 * @return whether the weapon should fire now.
	 */
	boolean shouldFire();
	
	/**
	 * Tells the weapon AI to reconsider whether it should be firing, before it decides it should fire again.
	 * 
	 * Called when a group is toggled on/off.
	 */
	void forceOff();
	
	/**
	 * @return location to aim at, with target leading if applicable. Can be null if the weapon has no target/isn't trying to aim anywhere.
	 */
	Vector2f getTarget();
	/**
	 * @return current target, if it's a ship. null otherwise.
	 */
	ShipAPI getTargetShip();
	
	/**
	 * @return the weapon that this AI is controlling. That means the plugin should hold on to it when it's passed in in ModPlugin.pickWeaponAutofireAI().
	 */
	WeaponAPI getWeapon();

	MissileAPI getTargetMissile();
}
