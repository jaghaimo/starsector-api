package com.fs.starfarer.api.impl.campaign.enc;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.util.Misc;

public class SlipstreamLuddicPathEPEC extends BaseEPEncounterCreator {
	
	@Override
	public void createEncounter(EncounterManager manager, EncounterPoint point) {
		Random random = manager.getRandom();
		
		LuddicPathBaseIntel intel = getClosestLuddicPathBase(point.getLocInHyper());
		float f = getProximityFactor(point.getLocInHyper());
		
		int difficulty = 0;
		difficulty += (int) Math.round(f * 3f);
		if (intel != null) {
			if (intel.isLarge()) {
				difficulty += 4;
			} else {
				difficulty += 2;
			}
		}
		difficulty += random.nextInt(4);
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = point.getLocInHyper();
		m.createStandardFleet(difficulty, Factions.LUDDIC_PATH, loc);
		m.triggerSetPirateFleet();
		m.triggerMakeLowRepImpact();
		m.triggerFleetAllowLongPursuit();
		if (intel != null && intel.getMarket() != null) {
			m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, intel.getMarket().getId());
		}
		
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
		
		LuddicPathBaseIntel intel = getClosestLuddicPathBase(point.getLocInHyper());
		float f = getProximityFactor(point.getLocInHyper());
		
		if (intel != null && f > 0) {
			f = 0.25f + 0.75f * f;
			if (intel.isLarge()) {
				f *= 5f;
			} else {
				f *= 3f;
			}
		}
		return 10f * f;
	}
	
	public static float getProximityFactor(Vector2f locInHyper) {
		LuddicPathBaseIntel intel = getClosestLuddicPathBase(locInHyper);
		float f = getLuddicPathBaseProximityFactor(intel, locInHyper);
		
		return f;
	}
}





