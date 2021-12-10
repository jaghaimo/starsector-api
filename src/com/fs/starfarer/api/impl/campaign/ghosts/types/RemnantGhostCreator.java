package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class RemnantGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		SensorGhost g = new RemnantGhost(manager, pf);
		if (!g.isCreationFailed()) {
			result.add(g);
		}
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getRemnantFrequency(manager);
		//return 10000f;
	}
	
	@Override
	public float getTimeoutDaysOnSuccessfulCreate(SensorGhostManager manager) {
		return 100f + manager.getRandom().nextFloat() * 200f;
	}
	
	
}



