package com.fs.starfarer.api.combat.listeners;

import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;

/**
 * Can only be added to the combat engine, not to specific ships; won't do anything in the latter case.
 */
public interface FleetMemberDeploymentListener {
	public void reportFleetMemberDeployed(DeployedFleetMemberAPI member);
}
