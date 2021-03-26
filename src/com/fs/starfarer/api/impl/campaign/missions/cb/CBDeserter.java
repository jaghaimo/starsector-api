package com.fs.starfarer.api.impl.campaign.missions.cb;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;

public class CBDeserter extends BaseCustomBountyCreator {

	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		if (Factions.PIRATES.equals(mission.getPerson().getFaction().getId())) return 0f;
		return super.getFrequency(mission, difficulty) * CBStats.DESERTER_FREQ;
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Deserter";
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
//		mission.requireSystemTags(ReqMode.ANY, Tags.THEME_RUINS, Tags.THEME_MISC, Tags.THEME_REMNANT_SECONDARY,
//								  Tags.THEME_DERELICT, Tags.THEME_REMNANT_DESTROYED);
		mission.requireSystemInterestingAndNotUnsafeOrCore();
		mission.requireSystemNotHasPulsar();
		StarSystemAPI system = mission.pickSystem();
		data.system = system;
	
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		FactionAPI faction = mission.getPerson().getFaction();
		
		if (difficulty <= 4) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty <= 5) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 6) {
			size = FleetSize.LARGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 7) {
			size = FleetSize.LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 8) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 9) {
			size = FleetSize.HUGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else {
			size = FleetSize.MAXIMUM;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		}
		
		beginFleet(mission, data);
		mission.triggerCreateFleet(size, quality, faction.getId(), type, data.system);
		mission.triggerSetFleetOfficers(oNum, oQuality);
		mission.triggerAutoAdjustFleetSize(size, size.next());
		mission.triggerSetFleetFaction(Factions.PIRATES);
		mission.triggerFleetSetShipPickMode(ShipPickMode.PRIORITY_THEN_ALL);
		
		mission.triggerFleetSetNoFactionInName();
		if (faction.getEntityNamePrefix() == null || faction.getEntityNamePrefix().isEmpty()) {
			mission.triggerFleetSetName("Deserter");
		} else {
			mission.triggerFleetSetName(faction.getEntityNamePrefix() + " Deserter");
		}
		
		mission.triggerSetStandardAggroPirateFlags();
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		//mission.triggerOrderFleetPatrol(data.system);
		mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.SALVAGEABLE, Tags.PLANET);
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.DESERTER_MULT, mission);
		
		return data;
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return 4;
	}

}






