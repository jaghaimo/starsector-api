package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class EchoGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		result.add(new EchoGhost(manager, pf));
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getEchoFrequency(manager);
		//return 10000f;
	}
	
	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return true;
	}

	@Override
	public boolean canSpawnWhilePlayerInAbyss() {
		return true;
	}
	
	
}
