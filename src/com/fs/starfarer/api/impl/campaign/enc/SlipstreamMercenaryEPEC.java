package com.fs.starfarer.api.impl.campaign.enc;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.util.Misc;

public class SlipstreamMercenaryEPEC extends BaseEPEncounterCreator {
	
	@Override
	public void createEncounter(EncounterManager manager, EncounterPoint point) {
		Random random = manager.getRandom();
		
		float f = getProximityFactor(point.getLocInHyper());
		
		int difficulty = 0;
		difficulty += (int) Math.round(f * 5f);
		difficulty += random.nextInt(6);
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = point.getLocInHyper();
		m.createQualityFleet(difficulty, Factions.MERCENARY, loc);
		m.triggerFleetAllowLongPursuit();
		m.triggerSetFleetFaction(Factions.INDEPENDENT);
		m.triggerMakeNoRepImpact();
		m.triggerFleetSetAllWeapons();
		
		CampaignFleetAPI fleet = m.createFleet();
		if (fleet != null) {
			point.where.addEntity(fleet);
			fleet.setLocation(point.loc.x, point.loc.y);
			Vector2f spawnLoc = Misc.getPointWithinRadius(point.loc, 1000f);
			SectorEntityToken e = point.where.createToken(spawnLoc);
			fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, e, 30f * random.nextFloat(), "laying in wait");
			fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
			fleet.addScript(new MissionFleetAutoDespawn(null, fleet));
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
		LuddicPathBaseIntel intel = getClosestLuddicPathBase(locInHyper);
		float f1 = getLuddicPathBaseProximityFactor(intel, locInHyper);
		
		PirateBaseIntel intel2 = getClosestPirateBase(locInHyper);
		float f2 = getPirateBaseProximityFactor(intel2, locInHyper);
		
		float result = Math.max(f1, f2);
		
		StarSystemAPI ruins = getClosestSystemWithRuins(locInHyper);
		float f3 = getRuinsProximityFactor(ruins, locInHyper);
		f3 *= 0.25f;
		result = Math.max(result, f3);
		
		return result;
	}
	
}





