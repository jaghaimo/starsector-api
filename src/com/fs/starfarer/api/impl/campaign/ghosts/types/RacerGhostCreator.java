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
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;

public class RacerGhostCreator extends BaseSensorGhostCreator {
	
	
	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		Vector2f loc = pf.getLocation();
		
		//LocationAPI hyper = Global.getSector().getHyperspace();
		float radius = 1000f;
		SlipstreamTerrainPlugin2 plugin = pickNearbySlipstream(radius, manager.getRandom());
		if (plugin == null) return null;
		
		float [] coords = plugin.getLengthAndWidthFractionWithinStream(loc, 0f, false, radius);
		if (coords == null) return null;
		
		SlipstreamSegment start = plugin.getSegmentForDist(coords[0]);
		if (start == null) return null;
		
		List<SlipstreamSegment> segments = plugin.getSegments();
		
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		
		int num = 1 + manager.getRandom().nextInt(5);
		
		float distBehind = 1000f;
		float currDist = 0f;
		for (int i = start.index - 1; i >= 0f && num > 0; i--) {
			SlipstreamSegment curr = segments.get(i);
			currDist += curr.lengthToNext;
			
			float b = plugin.getFaderBrightness(coords[0]);
			if (currDist > distBehind && b > 0) {
				num--;
				
				RacerGhost g = new RacerGhost(manager, curr, plugin);
				if (g.isCreationFailed()) continue;
				result.add(g);
				distBehind += 500f * manager.getRandom().nextFloat();
			}
		}
		return result;
	}

	
	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getRacerFrequency(manager);
		//return 10000f;
	}
	
	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return true;
	}
}
