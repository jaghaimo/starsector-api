package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class NoGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		result.add(new NoGhost(manager));
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		//return 0f;
		return GhostFrequencies.getNoGhostFrequency(manager);
	}
	

	@Override
	public float getTimeoutDaysOnSuccessfulCreate(SensorGhostManager manager) {
		return 0f;
	}

	@Override
	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return true;
	}
	
	
}
