package com.fs.starfarer.api.impl.combat;

import java.util.List;

import com.fs.starfarer.api.combat.ShipAPI;

public interface DroneStrikeStatsAIInfoProvider {
	float getMaxRange(ShipAPI ship);
	boolean dronesUsefulAsPD();
	boolean droneStrikeUsefulVsFighters();
	List<ShipAPI> getDrones(ShipAPI ship);
	int getMaxDrones();
	void setForceNextTarget(ShipAPI forceNextTarget);
	float getMissileSpeed();
}
