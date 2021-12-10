package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.util.Misc;

public class RemoraGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		float f = GhostFrequencies.getFringeFactor();
		int numRemora = 1 + manager.getRandom().nextInt(Math.round(f * 4f) + 1);
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		
		float dur = 7f + manager.getRandom().nextFloat() * 5f;

		Vector2f first = null;
		for (int i = 0; i < numRemora; i++) {
			Vector2f loc = null;
			if (first != null) {
				loc = Misc.getPointWithinRadiusUniform(first, 300f, manager.getRandom());
			}
			RemoraGhost remora = new RemoraGhost(manager, pf, dur + manager.getRandom().nextFloat() * 0.5f, loc);
			if (!remora.isCreationFailed()) {
				result.add(remora);
				first = remora.getEntity().getLocation();
			}
		}
		
		return result;
	}

	
	@Override
	public float getFrequency(SensorGhostManager manager) {
		//return 100000f;
		return GhostFrequencies.getRemoraFrequency(manager);
	}
	
	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return true;
	}
	
}
