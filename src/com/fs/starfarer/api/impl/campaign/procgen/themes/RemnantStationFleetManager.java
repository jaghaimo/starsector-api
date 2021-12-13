package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.enc.EncounterManager;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPoint;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPointProvider;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

public class RemnantStationFleetManager extends SourceBasedFleetManager {

	public class RemnantSystemEPGenerator implements EncounterPointProvider {
		public List<EncounterPoint> generateEncounterPoints(LocationAPI where) {
			if (!where.isHyperspace()) return null;
			if (totalLost > 0 && source != null) {
				String id = "ep_" + source.getId();
				EncounterPoint ep = new EncounterPoint(id, where, source.getLocationInHyperspace(), EncounterManager.EP_TYPE_OUTSIDE_SYSTEM);
				ep.custom = this;
				List<EncounterPoint> result = new ArrayList<EncounterPoint>();
				result.add(ep);
				return result;//source.getContainingLocation().getName()
			}
			return null;
		}
	}
	
	protected int minPts;
	protected int maxPts;
	protected int totalLost;
	protected transient RemnantSystemEPGenerator epGen;

	public RemnantStationFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay, 
									  int minPts, int maxPts) {
		super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
		this.minPts = minPts;
		this.maxPts = maxPts;
	}
	
	protected Object readResolve() {
		return this;
	}
	
	protected transient boolean addedListener = false;
	@Override
	public void advance(float amount) {
		if (!addedListener) {
			//totalLost = 1;
			/* best code ever -dgb
			if (Global.getSector().getPlayerPerson() != null && 
					Global.getSector().getPlayerPerson().getNameString().equals("Dave Salvage") &&
					Global.getSector().getClock().getDay() == 15 &&
							Global.getSector().getClock().getMonth() == 12 && 
							Global.getSector().getClock().getCycle() == 206) {
				totalLost = 0;
			}*/
			// global listener needs to be not this class since SourceBasedFleetManager
			// adds it to all fleets as their event listener
			// and so you'd get reportFleetDespawnedToListener() called multiple times
			// from global listeners, and from fleet ones
			epGen = new RemnantSystemEPGenerator();
			Global.getSector().getListenerManager().addListener(epGen, true);
			addedListener = true;
		}
		super.advance(amount);
	}
	

	@Override
	protected CampaignFleetAPI spawnFleet() {
		if (source == null) return null;
		
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
		//params.officerNumberBonus = 10;
		params.random = random;
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null) return null;;
		
		
		LocationAPI location = source.getContainingLocation();
		location.addEntity(fleet);
		
		RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, false);
		
		fleet.setLocation(source.getLocation().x, source.getLocation().y);
		fleet.setFacing(random.nextFloat() * 360f);
		
		fleet.addScript(new RemnantAssignmentAI(fleet, (StarSystemAPI) source.getContainingLocation(), source));
		fleet.getMemoryWithoutUpdate().set("$sourceId", source.getId());
		
		return fleet;
	}

	
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		super.reportFleetDespawnedToListener(fleet, reason, param);
		if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
			String sid = fleet.getMemoryWithoutUpdate().getString("$sourceId");
			if (sid != null && source != null && sid.equals(source.getId())) {
			//if (sid != null && sid.equals(source.getId())) {
				totalLost++;
			}
		}
	}

	public int getTotalLost() {
		return totalLost;
	}

	
}




