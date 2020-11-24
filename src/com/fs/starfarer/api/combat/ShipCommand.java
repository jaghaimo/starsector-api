package com.fs.starfarer.api.combat;

public enum ShipCommand {
	TURN_LEFT,
	TURN_RIGHT,
	STRAFE_LEFT,
	STRAFE_RIGHT,
	ACCELERATE,
	ACCELERATE_BACKWARDS,
	DECELERATE,
	SELECT_GROUP,
	
	/**
	 * Use "FIRE" instead.
	 */
	@Deprecated 
	USE_SELECTED_GROUP,
	TOGGLE_AUTOFIRE,
	FIRE,
	VENT_FLUX,
	TOGGLE_SHIELD_OR_PHASE_CLOAK,
	HOLD_FIRE,
	PULL_BACK_FIGHTERS,
	USE_SYSTEM,
}
