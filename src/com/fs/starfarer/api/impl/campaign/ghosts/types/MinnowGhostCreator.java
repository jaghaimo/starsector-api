package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ghosts.SharedTrigger;

public class MinnowGhostCreator extends BaseSensorGhostCreator {
	
	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		// not inside slipstreams, but can be close enough so they occasionally dart in
		Vector2f loc = findHyperspaceArea(pf.getLocation(), 1500f, 2000f, 1000f, manager.getRandom(), true, 250f);
		if (loc == null) return null;
		
		SectorEntityToken target = Global.getSector().getHyperspace().createToken(loc); 
		
		int numMinnows = 5 + manager.getRandom().nextInt(11);
		
		float minRange = 100f;
		float maxRange = 500;
		float dur = 5f + manager.getRandom().nextFloat() * 5f;
		
		SharedTrigger trigger = new SharedTrigger();
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		for (int i = 0; i < numMinnows; i++) {
			MinnowGhost m = new MinnowGhost(manager, target, minRange, maxRange,
									dur + manager.getRandom().nextFloat(), trigger);
			result.add(m);
		}
		return result;
	}

	
	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getMinnowFrequency(manager);
		//return 10000f;
	}
	
	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return true;
	}
}
