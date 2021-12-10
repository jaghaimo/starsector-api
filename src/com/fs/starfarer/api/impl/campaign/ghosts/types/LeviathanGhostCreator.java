package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class LeviathanGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		
		float r = manager.getRandom().nextFloat();
		
		boolean withCalf = false;
		int numRemora = 0;
		if (r < 0.1f) {
			withCalf = true;
		} else if (r < 0.3f) {
			numRemora = 1 + manager.getRandom().nextInt(3);
		}
		
//		calf = false;
//		numRemora = 3;
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		LeviathanGhost g = new LeviathanGhost(manager, 0);
		if (g.isCreationFailed()) return null;
		
		result.add(g);
		
		if (withCalf) {
			LeviathanCalfGhost c = new LeviathanCalfGhost(manager, g.getEntity());
			result.add(c);
		}
		if (numRemora > 0) {
			for (int i = 0; i < numRemora; i++) {
				RemoraGhost remora = new RemoraGhost(manager, g.getEntity(), 1000f); // however long the Leviathan lasts
				if (!remora.isCreationFailed()) {
					result.add(remora);
				}
			}
		}
		
		return result;
	}

	
	@Override
	public float getFrequency(SensorGhostManager manager) {
		//return 100000f;
		return GhostFrequencies.getLeviathanFrequency(manager);
	}
	
	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return true;
	}
}
