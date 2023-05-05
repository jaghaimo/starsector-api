package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class GuideGhostCreator extends BaseSensorGhostCreator {
	
	public static float GUIDE_GHOST_RADIUS_LY = 5f;
	
	
	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(manager.getRandom());
		
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		for (SectorEntityToken curr : hyper.getEntitiesWithTag(Tags.NEUTRINO_HIGH)) {
			if (curr.isPlayerFleet()) continue;
			float distLY = Misc.getDistanceLY(curr.getLocation(), pf.getLocationInHyperspace());
			if (distLY > GUIDE_GHOST_RADIUS_LY) continue;
			picker.add(curr, 1f);
		}
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float distLY = Misc.getDistanceLY(system.getLocation(), pf.getLocationInHyperspace());
			if (distLY > GUIDE_GHOST_RADIUS_LY) continue;
			
			float score = 0f;
			for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.NEUTRINO_HIGH)) {
				if (curr.hasTag(Tags.OBJECTIVE)) continue;
				if (score == 0) score = 1f;
				score += score;
			}
			
			if (score > 0 && system.getHyperspaceAnchor() != null) {
				picker.add(system.getHyperspaceAnchor(), score);
			}
		}
		
		for (SectorEntityToken item : new ArrayList<SectorEntityToken>(picker.getItems())) {
			if (Misc.crossesAnySlipstream(Global.getSector().getHyperspace(), 
										  pf.getLocation(), item.getLocation())) {
				picker.remove(item);
			}
		}
		
		
		SectorEntityToken target = picker.pick();
		if (target == null) return null;
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		GuideGhost g = new GuideGhost(manager, target);
		if (g.isCreationFailed()) return null;
		result.add(g);
		return result;
	}

	
	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getGuideFrequency(manager);
		//return 10000f;
	}
	
}
