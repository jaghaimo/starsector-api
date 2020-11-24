package com.fs.starfarer.api.impl.campaign.fleets;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.MercType;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * Unused.
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public class PirateFleetManager extends BaseLimitedFleetManager {

	@Override
	protected int getMaxFleets() {
		return (int) Global.getSettings().getFloat("maxPirateFleets");
	}

	@Override
	protected CampaignFleetAPI spawnFleet() {
		StarSystemAPI target = pickTargetSystem();
		if (target == null || true) return null;
		
		WeightedRandomPicker<MercType> picker = new WeightedRandomPicker<MercType>();
		picker.add(MercType.SCOUT, 10f); 
		picker.add(MercType.BOUNTY_HUNTER, 10f); 
		picker.add(MercType.PRIVATEER, 10f); 
		picker.add(MercType.PATROL, 10f); 
		picker.add(MercType.ARMADA, 3f); 
		
		MercType type = picker.pick();
		
		
		float combat = 0f;
		float tanker = 0f;
		float freighter = 0f;
		String fleetType = type.fleetType;
		switch (type) {
		case SCOUT:
			combat = Math.round(1f + (float) Math.random() * 2f);
			break;
		case PRIVATEER:
		case BOUNTY_HUNTER:
			combat = Math.round(3f + (float) Math.random() * 2f);
			break;
		case PATROL:
			combat = Math.round(9f + (float) Math.random() * 3f);
			break;
		case ARMADA:
			combat = Math.round(12f + (float) Math.random() * 8f);
			break;
		}
		
		FleetParamsV3 params = new FleetParamsV3(
				null, 
				target.getLocation(),
				Factions.PIRATES, // quality will always be reduced by non-market-faction penalty, which is what we want 
				null,
				fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
//		if (route != null) {
//			params.timestamp = route.getTimestamp();
//		}
		//params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null || fleet.isEmpty()) return null;
		
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		
		MarketAPI source = Misc.getSourceMarket(fleet);
		if (source == null) return null;
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		boolean spawnAtSource = true;
		if (player != null) {
			float sourceToPlayer = Misc.getDistance(player.getLocation(), source.getLocationInHyperspace());
			float targetToPlayer = Misc.getDistance(player.getLocation(), target.getLocation());
			spawnAtSource = sourceToPlayer < targetToPlayer;
		}
		
		if (spawnAtSource) {
			source.getPrimaryEntity().getContainingLocation().addEntity(fleet);
			fleet.setLocation(source.getPrimaryEntity().getLocation().x, source.getPrimaryEntity().getLocation().y);
			
			fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, source.getPrimaryEntity(), 2f + (float) Math.random() * 2f,
								"orbiting " + source.getName());
		} else {
			Vector2f loc = Misc.pickHyperLocationNotNearPlayer(target.getLocation(), Global.getSettings().getMaxSensorRangeHyper() + 500f);
			Global.getSector().getHyperspace().addEntity(fleet);
			fleet.setLocation(loc.x, loc.y);
		}
		
		
		Vector2f dest = Misc.getPointAtRadius(target.getLocation(), 1500);
		LocationAPI hyper = Global.getSector().getHyperspace();
		SectorEntityToken token = hyper.createToken(dest.x, dest.y);
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, token, 1000,
				"traveling to the " + target.getBaseName() + " star system");

		if ((float) Math.random() > 0.75f) {
			fleet.addAssignment(FleetAssignment.RAID_SYSTEM, target.getHyperspaceAnchor(), 20,
					"raiding around the " + target.getBaseName() + " star system");
		} else {
			fleet.addAssignment(FleetAssignment.RAID_SYSTEM, target.getCenter(), 20,
					"raiding the " + target.getBaseName() + " star system");
		}
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source.getPrimaryEntity(), 1000,
					"returning to " + source.getName());
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source.getPrimaryEntity(), 2f + 2f * (float) Math.random(),
					"offloading ill-gotten goods");
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source.getPrimaryEntity(), 1000);
		
		return fleet;
	}

	
	protected StarSystemAPI pickTargetSystem() {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float mult = Misc.getSpawnChanceMult(system.getLocation());
			
			if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) {
				continue;
			}
			
			// want: large, unstable
			float weight = 0f;
			float bounties = 0;
			for (MarketAPI market : Misc.getMarketsInLocation(system)) {
				if (market.getFactionId().equals(Factions.PIRATES)) continue;
				//weight += market.getSize() * (15f - market.getStabilityValue());
				float w = 11f - market.getStabilityValue() + market.getSize();
				if (w > weight) weight = w;
//				if (market.hasCondition(Conditions.EVENT_SYSTEM_BOUNTY)) {
//					bounties++;
//				}
			}
			
			weight *= (bounties + 1);
			weight *= mult;
			
			picker.add(system, weight);
		}
		return picker.pick();
	}

	
	public static CampaignFleetAPI createPirateFleet(int combatPoints, RouteData route, Vector2f locInHyper) {
		float combat = combatPoints;
		float tanker = 0f;
		float freighter = 0f;
		
		MercType type = MercType.SCOUT;
		if (combat >= 18f) type = MercType.ARMADA;
		if (combat >= 12f) type = MercType.PATROL;
		if (combat >= 6f) {
			if ((float) Math.random() < 0.5f) {
				type = MercType.PRIVATEER;
			} else {
				type = MercType.BOUNTY_HUNTER;
			}
		}
		
		combat *= 5f;
		
		String fleetType = type.fleetType;
		
		FleetParamsV3 params = new FleetParamsV3(
				route != null ? route.getMarket() : null, 
				locInHyper,
				Factions.PIRATES, // quality will always be reduced by non-market-faction penalty, which is what we want 
				route != null ? route.getQualityOverride() : null,
				fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		if (route != null) {
			params.timestamp = route.getTimestamp();
		}
		//params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
		
		MarketAPI source = Misc.getSourceMarket(fleet);
		if (source == null) return null;
		
		return fleet;
	}
}















