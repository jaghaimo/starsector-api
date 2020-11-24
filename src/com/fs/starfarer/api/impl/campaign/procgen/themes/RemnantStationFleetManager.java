package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

public class RemnantStationFleetManager extends SourceBasedFleetManager {

	protected int minPts;
	protected int maxPts;
	protected int totalLost;

	public RemnantStationFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay, 
									  int minPts, int maxPts) {
		super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
		this.minPts = minPts;
		this.maxPts = maxPts;
	}
	
	@Override
	protected CampaignFleetAPI spawnFleet() {
		Random random = new Random();
		
		int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);
		
		int bonus = totalLost * 4;
		if (bonus > maxPts) bonus = maxPts;
		
		combatPoints += bonus;
		
		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 8) type = FleetTypes.PATROL_MEDIUM;
		if (combatPoints > 16) type = FleetTypes.PATROL_LARGE;
		
		combatPoints *= 8f;
		
		FleetParamsV3 params = new FleetParamsV3(
				source.getMarket(),
				source.getLocationInHyperspace(),
				Factions.REMNANTS,
				1f,
				type,
				combatPoints, // combatPts
				0f, // freighterPts 
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
		);
		params.officerNumberBonus = 10;
		params.random = random;
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null) return null;;
		
		LocationAPI location = source.getContainingLocation();
		location.addEntity(fleet);
		
		RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, false);
		
		fleet.setLocation(source.getLocation().x, source.getLocation().y);
		fleet.setFacing(random.nextFloat() * 360f);
		
		fleet.addScript(new RemnantAssignmentAI(fleet, (StarSystemAPI) source.getContainingLocation(), source));
		
		return fleet;
	}

	
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		super.reportFleetDespawnedToListener(fleet, reason, param);
		if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
			totalLost++;
		}
	}

	
}
