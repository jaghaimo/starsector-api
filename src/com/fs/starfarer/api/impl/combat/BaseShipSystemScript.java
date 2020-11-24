package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScriptAdvanced;

public class BaseShipSystemScript implements ShipSystemStatsScriptAdvanced {

	public BaseShipSystemScript() {
		//System.out.println("wefwefe");
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
	}

	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		return null;
	}

	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}

	public float getActiveOverride(ShipAPI ship) {
		return -1;
	}
	public float getInOverride(ShipAPI ship) {
		return -1;
	}
	public float getOutOverride(ShipAPI ship) {
		return -1;
	}

	public float getRegenOverride(ShipAPI ship) {
		return -1;
	}

	public int getUsesOverride(ShipAPI ship) {
		return -1;
	}
}
