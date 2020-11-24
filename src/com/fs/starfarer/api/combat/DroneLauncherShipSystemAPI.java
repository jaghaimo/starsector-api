package com.fs.starfarer.api.combat;

import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

public interface DroneLauncherShipSystemAPI {

	public static enum DroneOrders {
		RECALL,
		DEPLOY,
		ATTACK,
	}

	/**
	 * @return contents of the .system file, in JSON form.
	 */
	JSONObject getSpecJson();
	
	/**
	 * @return where this drone should land, if it's being recalled.
	 * @param drone
	 * @return
	 */
	Vector2f getLandingLocation(ShipAPI drone);
	
	
	DroneLauncherShipSystemAPI.DroneOrders getDroneOrders();

	int getIndex(ShipAPI drone);
}
