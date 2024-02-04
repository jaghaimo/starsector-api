package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.util.Misc;

public class AbyssalDrifterGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		float depth = Misc.getAbyssalDepth(pf);
		if (depth < 1f) return null;
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		SensorGhost g = new AbyssalDrifterGhost(manager, pf);
		if (!g.isCreationFailed()) {
			result.add(g);
		}
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getAbyssalDrifterFrequency(manager);
	}
	
	@Override
	public boolean canSpawnWhilePlayerInAbyss() {
		return true;
	}

	@Override
	public boolean canSpawnWhilePlayerOutsideAbyss() {
		return false;
	}
	
	@Override
	public float getTimeoutDaysOnSuccessfulCreate(SensorGhostManager manager) {
		return 0f;
	}
	
	
}
