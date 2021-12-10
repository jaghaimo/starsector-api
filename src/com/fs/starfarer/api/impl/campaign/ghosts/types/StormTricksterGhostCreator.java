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

public class StormTricksterGhostCreator extends BaseSensorGhostCreator {

	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		Vector2f loc = findDeepHyperspaceArea(pf.getLocation(), 2000f, 3000f, 1500f, manager.getRandom());
		if (loc == null) return null;
		
		SectorEntityToken target = Global.getSector().getHyperspace().createToken(loc);
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		SensorGhost g = new StormTricksterGhost(manager, target);
		if (g.isCreationFailed()) return null;
		result.add(g);
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getStormTricksterFrequency(manager);
		//return 10000f;
	}
	
	
}
