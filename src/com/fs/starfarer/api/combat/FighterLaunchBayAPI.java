package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

public interface FighterLaunchBayAPI {
	
	/**
	 * Absolute location the fighter should be heading for landing.
	 * @param fighter
	 * @return
	 */
	Vector2f getLandingLocation(ShipAPI fighter);
	
	
	
	/**
	 * This removes the fighter from the engine, so its AI methods will stop being called.
	 * When the fighter is re-launched, a new AI will be created.
	 * @param fighter
	 */
	void land(ShipAPI fighter);
	
	
	/**
	 * @return The ship that this launch bay is on.
	 */
	ShipAPI getShip();



	int getFastReplacements();
	void setFastReplacements(int fastReplacements);

	FighterWingAPI getWing();
	void makeCurrentIntervalFast();
	int getExtraDeployments();
	void setExtraDeployments(int extraDeployments);

	int getExtraDeploymentLimit();
	void setExtraDeploymentLimit(int extraDeploymentLimit);

	float getExtraDuration();
	void setExtraDuration(float extraDuration);

	int getNumLost();
	void setNumLost(int numLost);
}





