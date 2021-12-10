package com.fs.starfarer.api.impl.campaign.enc;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerPiracyScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SlipstreamScavengerEPEC extends BaseEPEncounterCreator {
	
	@Override
	public void createEncounter(EncounterManager manager, EncounterPoint point) {
		Random random = manager.getRandom();
		boolean pirate = random.nextBoolean();
		pirate = true; // seems like legit scavengers wouldn't spend too much time waiting in ambush
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		picker.add(FleetTypes.SCAVENGER_SMALL, 5f);
		picker.add(FleetTypes.SCAVENGER_MEDIUM, 15f);
		picker.add(FleetTypes.SCAVENGER_LARGE, 10f);
		String type = picker.pick();
		
		CampaignFleetAPI fleet = RuinsFleetRouteManager.createScavenger(type, point.getLocInHyper(), 
																		null, null, pirate, random);
		
		if (fleet != null) {
			point.where.addEntity(fleet);
			fleet.setLocation(point.loc.x, point.loc.y);
			Vector2f spawnLoc = Misc.getPointWithinRadius(point.loc, 1000f);
			SectorEntityToken e = point.where.createToken(spawnLoc);
			fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, e, 30f * random.nextFloat(), "waiting");
			fleet.addScript(new MissionFleetAutoDespawn(null, fleet));
			fleet.addScript(new ScavengerPiracyScript(fleet));
		}
	}

	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		if (!EncounterManager.EP_TYPE_SLIPSTREAM.equals(point.type)) return 0f;
		
		float f = getProximityFactor(point.getLocInHyper());
		if (f > 0) {
			f = 0.25f + 0.75f * f;
		}
		return 10f * f;
	}
	
	
	public static float getProximityFactor(Vector2f locInHyper) {
		StarSystemAPI ruins = getClosestSystemWithRuins(locInHyper);
		float f = getRuinsProximityFactor(ruins, locInHyper);
		return f;
	}
	
	
}





