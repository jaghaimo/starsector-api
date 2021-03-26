package com.fs.starfarer.api.campaign.listeners;

import java.util.List;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface ShipRecoveryListener {
	void reportShipsRecovered(List<FleetMemberAPI> ships, InteractionDialogAPI dialog);
}
