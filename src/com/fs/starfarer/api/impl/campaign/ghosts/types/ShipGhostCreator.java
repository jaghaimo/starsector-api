package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class ShipGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		SensorGhost g = new ShipGhost(manager, pf);
		if (!g.isCreationFailed()) {
			result.add(g);
		}
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getShipFrequency(manager);
		//return 10000f;
	}
	
	@Override
	public float getTimeoutDaysOnSuccessfulCreate(SensorGhostManager manager) {
		return 30f + manager.getRandom().nextFloat() * 30f;
	}
	
}



