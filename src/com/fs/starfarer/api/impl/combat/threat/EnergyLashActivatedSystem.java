package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.combat.ShipAPI;

public interface EnergyLashActivatedSystem {
	//public void resetAfterShipCreation(ShipAPI ship);
	
	public void hitWithEnergyLash(ShipAPI overseer, ShipAPI ship);
	
	/**
	 * 0 to 1, how useful would the ship find it to have its system be lash-activated right now.
	 * @param other 
	 * @return
	 */
	public float getCurrentUsefulnessLevel(ShipAPI overseer, ShipAPI ship);
}
