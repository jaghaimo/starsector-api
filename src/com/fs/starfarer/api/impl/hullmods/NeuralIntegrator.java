package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.util.Misc;

public class NeuralIntegrator extends NeuralInterface {

	public boolean isApplicableToShip(ShipAPI ship) {
		if (!Misc.isAutomated(ship)) {
			return false;
		}
		if (ship.getHullSpec().getHints().contains(ShipTypeHints.NO_NEURAL_LINK)) {
			return false;
		}
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!Misc.isAutomated(ship)) {
			return "Can only be installed on automated ships, install Neural Interface instead";
		}
		if (ship.getHullSpec().getHints().contains(ShipTypeHints.NO_NEURAL_LINK)) {
			return "Can not be installed on this ship";
		}
		return null;
	}
}




