package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class NeuralIntegrator extends NeuralInterface {

	public static float DP_INCREASE_PERCENT = 10f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		super.applyEffectsBeforeShipCreation(hullSize, stats, id);
		
		stats.getSuppliesToRecover().modifyPercent(id, DP_INCREASE_PERCENT);
		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(id, DP_INCREASE_PERCENT);
	}

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




