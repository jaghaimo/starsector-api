package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.enc.BaseEPEncounterCreator;
import com.fs.starfarer.api.impl.campaign.fleets.AutoDespawnScript;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class EncounterTricksterGhostCreator extends BaseSensorGhostCreator {

	public static enum OtherFleetType {
		PIRATE,
		LUDDIC_PATH,
		MERCENARY,
		NOTHING, // higher chance to spawn no encounter when near a base; creator has higher base probability to compensate
	}
	
	@Override
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return null;
		
		Random random = manager.getRandom();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		LocationAPI hyper = Global.getSector().getCurrentLocation();
		
		Vector2f loc = findHyperspaceArea(pf.getLocation(), 3000, 4000, 1000, random, true, 3000);
		if (loc == null) return null;
		
		CampaignFleetAPI other = createOtherFleet(manager, pf.getLocation());
		if (other == null) {
			// to trigger full timeout, since this is a valid outcome for this creator
			// i.e. it picked "nothing" due to not being near a pirate base etc
//			List<SensorGhost> result = new ArrayList<SensorGhost>();
//			result.add(new NoGhost(manager));
//			return result;
			// actually, don't want the above, it's fine fo this ghost to trigger the short timeout and for
			// another ghost to be picked
			return null;
		}
		
		hyper.addEntity(other);
		other.setLocation(loc.x, loc.y);
		other.addScript(new AutoDespawnScript(other));
		
		boolean guideToTarget = manager.getRandom().nextBoolean();
		//guideToTarget = false;
		List<SensorGhost> result = new ArrayList<SensorGhost>();
		SensorGhost g = new EncounterTricksterGhost(manager, other, guideToTarget);
		if (g.isCreationFailed()) {
			hyper.removeEntity(other);
			return null;
		}
		result.add(g);
		return result;
	}

	@Override
	public float getFrequency(SensorGhostManager manager) {
		return GhostFrequencies.getEncounterTricksterFrequency(manager);
		//return 100000f;
	}
	
	
	public CampaignFleetAPI createOtherFleet(SensorGhostManager manager, Vector2f locInHyper) {
		Random random = manager.getRandom();

		
		PirateBaseIntel intel = BaseEPEncounterCreator.getClosestPirateBase(locInHyper);
		float f1 = BaseEPEncounterCreator.getPirateBaseProximityFactor(intel, locInHyper);
		
		LuddicPathBaseIntel intel2 = BaseEPEncounterCreator.getClosestLuddicPathBase(locInHyper);
		float f2 = BaseEPEncounterCreator.getLuddicPathBaseProximityFactor(intel2, locInHyper);
		
		float result = Math.max(f1, f2);
		
		StarSystemAPI ruins = BaseEPEncounterCreator.getClosestSystemWithRuins(locInHyper);
		float f3 = BaseEPEncounterCreator.getRuinsProximityFactor(ruins, locInHyper);
		f3 *= 0.25f;
		result = Math.max(result, f3);
		
		WeightedRandomPicker<OtherFleetType> picker = new WeightedRandomPicker<OtherFleetType>(random);
		picker.add(OtherFleetType.PIRATE, f1 + 0.1f);
		picker.add(OtherFleetType.LUDDIC_PATH, f2 + 0.05f);
		picker.add(OtherFleetType.MERCENARY, (f1 + f2) * 0.25f + 0.01f);
		picker.add(OtherFleetType.NOTHING, 1f);
		OtherFleetType type = picker.pick();
		if (type == null) return null;
		
		CampaignFleetAPI fleet = null;
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		if (type == OtherFleetType.PIRATE) {
			String factionId = Factions.PIRATES;
			if (intel != null && intel.getMarket() != null) factionId = intel.getMarket().getFactionId();
			int difficulty = 0;
			
			if (intel != null) {
				difficulty += (int) Math.round(f1 * 2f);
				if (intel != null) {
					switch (intel.getTier()) {
					case TIER_1_1MODULE: difficulty += 2; break;
					case TIER_2_1MODULE: difficulty += 2; break;
					case TIER_3_2MODULE: difficulty += 3; break;
					case TIER_4_3MODULE: difficulty += 4; break;
					case TIER_5_3MODULE: difficulty += 5; break;
					}
				}
				difficulty += random.nextInt(4);
			} else {
				difficulty += 4;
				difficulty += random.nextInt(7);
			}
			Vector2f loc = locInHyper;
			m.createStandardFleet(difficulty, factionId, loc);
			m.triggerSetStandardAggroPirateFlags();
			m.triggerFleetAllowLongPursuit();
			if (intel != null && intel.getMarket() != null) {
				m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, intel.getMarket().getId());
			}
			fleet = m.createFleet();
		} else if (type == OtherFleetType.LUDDIC_PATH) {
			int difficulty = 0;
			if (intel2 != null) { 
				difficulty += (int) Math.round(f2 * 2f);
				if (intel2.isLarge()) {
					difficulty += 5;
				} else {
					difficulty += 3;
				}
				difficulty += random.nextInt(4);
			} else {
				difficulty += 4;
				difficulty += random.nextInt(7);
			}
			
			Vector2f loc = locInHyper;
			m.createStandardFleet(difficulty, Factions.LUDDIC_PATH, loc);
			m.triggerSetStandardAggroPirateFlags();
			m.triggerFleetAllowLongPursuit();
			m.triggerFleetPatherNoDefaultTithe();
			if (intel2 != null && intel2.getMarket() != null) {
				m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, intel2.getMarket().getId());
			}
			
			fleet = m.createFleet();
		} else if (type == OtherFleetType.MERCENARY) {
			int difficulty = 3;
			float f = Math.max(f1, f2);
			difficulty += (int) Math.round(f * 3f);
			difficulty += random.nextInt(5);
			
			Vector2f loc = locInHyper;
			m.createQualityFleet(difficulty, Factions.MERCENARY, loc);
			m.triggerFleetAllowLongPursuit();
			m.triggerSetFleetFaction(Factions.INDEPENDENT);
			m.triggerMakeNoRepImpact();
			m.triggerFleetSetAllWeapons();
			
			fleet = m.createFleet();
		} else if (type == OtherFleetType.NOTHING) {
			return null;
		}
		
		if (fleet == null) return null;
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		fleet.addScript(new MissionFleetAutoDespawn(null, fleet));
		
		return fleet;
	}
	
	
}






