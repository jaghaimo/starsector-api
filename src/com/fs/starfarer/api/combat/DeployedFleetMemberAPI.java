package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

/**
 * Used in combat to relate a deployed ship or fighter wing to the associated FleetMemberAPI.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2013 Fractal Softworks, LLC
 */


public interface DeployedFleetMemberAPI extends AssignmentTargetAPI {

	FleetMemberAPI getMember();

	boolean isFighterWing();

	/**
	 * @return Ship or wing leader.
	 */
	ShipAPI getShip();

	boolean isAlly();

	boolean isStation();

	boolean canBeGivenOrders();

	boolean isStationModule();

	boolean canBeGivenRetreatOrders();

	boolean isDirectRetreat();
	void setDirectRetreat(boolean directRetreat);

}
