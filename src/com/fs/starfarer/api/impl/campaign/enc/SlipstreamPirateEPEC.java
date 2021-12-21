package com.fs.starfarer.api.impl.campaign.enc;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.util.Misc;

public class SlipstreamPirateEPEC extends BaseEPEncounterCreator {
	
	@Override
	public void createEncounter(EncounterManager manager, EncounterPoint point) {
		Random random = manager.getRandom();
		
		PirateBaseIntel intel = getClosestPirateBase(point.getLocInHyper());
		float f = getProximityFactor(point.getLocInHyper()); // not just bases - ruins/core, too
		
		int difficulty = 0;
		difficulty += (int) Math.round(f * 3f);
		if (intel != null) {
			switch (intel.getTier()) {
			case TIER_1_1MODULE: difficulty += 1; break;
			case TIER_2_1MODULE: difficulty += 1; break;
			case TIER_3_2MODULE: difficulty += 2; break;
			case TIER_4_3MODULE: difficulty += 3; break;
			case TIER_5_3MODULE: difficulty += 4; break;
			}
		}
		difficulty += random.nextInt(4);
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = point.getLocInHyper();
		String factionId = Factions.PIRATES;
		if (intel != null && intel.getMarket() != null) factionId = intel.getMarket().getFactionId();
		m.createStandardFleet(difficulty, factionId, loc);
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
		
		PirateBaseIntel intel = getClosestPirateBase(point.getLocInHyper());
		float f = getProximityFactor(point.getLocInHyper()); // not just bases - ruins/core, too
		
		if (intel != null && f > 0) {
			f = 0.25f + 0.75f * f;
			switch (intel.getTier()) {
			case TIER_1_1MODULE: f *= 1f; break;
			case TIER_2_1MODULE: f *= 2f; break;
			case TIER_3_2MODULE: f *= 3f; break;
			case TIER_4_3MODULE: f *= 4f; break;
			case TIER_5_3MODULE: f *= 5f; break;
			}
		}
		return 10f * f;
	}

	
	public static float getProximityFactor(Vector2f locInHyper) {
		PirateBaseIntel intel = getClosestPirateBase(locInHyper);
		float f = getPirateBaseProximityFactor(intel, locInHyper);
		
		float f2 = getCoreProximityFactor(locInHyper);
		f2 *= 0.5f;
		if (f2 > f) f = f2;
		
		StarSystemAPI ruins = getClosestSystemWithRuins(locInHyper);
		float f3 = getRuinsProximityFactor(ruins, locInHyper);
		f3 *= 0.25f;
		if (f3 > f) f = f3;
			
		return f;
	}
}





