package com.fs.starfarer.api.impl.combat.dweller;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public interface DwellerShipCreator {
	public void initBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id);
	public void initAfterShipCreation(ShipAPI ship, String id);
	public void initAfterShipAddedToCombatEngine(ShipAPI ship, String id);
	public void initInCombat(ShipAPI ship);
}
