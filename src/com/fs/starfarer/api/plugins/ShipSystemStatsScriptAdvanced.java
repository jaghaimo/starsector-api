package com.fs.starfarer.api.plugins;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;


public interface ShipSystemStatsScriptAdvanced extends ShipSystemStatsScript {
	
	boolean isUsable(ShipSystemAPI system, ShipAPI ship);
	
	/**
	 * If null, uses "READY" and "ACTIVE" as appropritate in the ship info widget.
	 * @return
	 */
	String getInfoText(ShipSystemAPI system, ShipAPI ship);
}
