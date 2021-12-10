package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class StormcallerGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;

		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		SensorGhost g = new StormcallerGhost(manager);
		if (g.isCreationFailed()) return null;
		result.add(g);
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getStormcallerFrequency(manager);
		//return 10000f;
	}
	
	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return true;
	}
}
