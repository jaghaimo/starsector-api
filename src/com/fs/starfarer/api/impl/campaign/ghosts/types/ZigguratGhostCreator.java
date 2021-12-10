package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class ZigguratGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		ZigguratGhost g = new ZigguratGhost(manager);
		if (!g.isCreationFailed()) {
			result.add(g);
		}
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getZigguratFrequency(manager);
	}

	@Override
	public float getTimeoutDaysOnSuccessfulCreate(SensorGhostManager manager) {
		return 50f + manager.getRandom().nextFloat() * 50f;
	}
	
	
}
