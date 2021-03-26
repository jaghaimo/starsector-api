package com.fs.starfarer.api.impl.campaign.missions.cb;

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

public class CBPather extends BaseCustomBountyCreator {

	public static float PROB_IN_SYSTEM_WITH_BASE = 0.5f;
	
	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		if (Factions.LUDDIC_PATH.equals(mission.getPerson().getFaction().getId())) return 0f;
		return super.getFrequency(mission, difficulty) * CBStats.PATHER_FREQ;
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Pather";
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
//		if (difficulty >= 4) {
//			mission.requireSystemTags(ReqMode.ANY, Tags.THEME_RUINS, Tags.THEME_MISC, Tags.THEME_REMNANT_SECONDARY,
//					  				  Tags.THEME_DERELICT, Tags.THEME_REMNANT_DESTROYED);
//		} else {
//			mission.requireSystemTags(ReqMode.ANY, Tags.THEME_RUINS, Tags.THEME_MISC,
//	  				  				  Tags.THEME_DERELICT);
//		}
		mission.requireSystemInterestingAndNotUnsafeOrCore();
		mission.requireSystemNotHasPulsar();
		if (difficulty >= 4 && mission.rollProbability(PROB_IN_SYSTEM_WITH_BASE)) {
			mission.preferSystemHasBase(Factions.LUDDIC_PATH);
		}
		StarSystemAPI system = mission.pickSystem();
		data.system = system;
	
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		String factionId = Factions.LUDDIC_PATH;
		
		if (difficulty <= 0) {
			size = FleetSize.TINY;
			quality = FleetQuality.VERY_LOW;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 1) {
			size = FleetSize.VERY_SMALL;
			quality = FleetQuality.VERY_LOW;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 2) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FEWER;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 3) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 4 || difficulty == 5) {
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
		} else {// if (difficulty == 10) {
			size = FleetSize.MAXIMUM;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.HIGHER;
			//oNum = OfficerNum.ALL_SHIPS;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		}
		
		beginFleet(mission, data);
		mission.triggerCreateFleet(size, quality, factionId, type, data.system);
		mission.triggerSetFleetOfficers(oNum, oQuality);
		mission.triggerFleetPatherNoDefaultTithe();
		mission.triggerAutoAdjustFleetSize(size, size.next());
		mission.triggerSetStandardAggroPirateFlags();
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		//mission.triggerOrderFleetPatrol(data.system);
		mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.SALVAGEABLE, Tags.PLANET, Tags.STATION);
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.PATHER_MULT, mission);
		
		return data;
	}
	
}






