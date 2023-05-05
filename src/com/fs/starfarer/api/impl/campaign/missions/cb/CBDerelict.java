package com.fs.starfarer.api.impl.campaign.missions.cb;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;

public class CBDerelict extends BaseCustomBountyCreator {

	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		return super.getFrequency(mission, difficulty) * CBStats.DERELICT_FREQ;
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Derelict Fleet";
	}
	
	@Override
	public String getIconName() {
		return Global.getSettings().getSpriteName("campaignMissions", "derelict_bounty");
	}
	

	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		//mission.setIconName("campaignMissions", "derelict_bounty");
		
		//mission.requireSystem(this);
		mission.requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_CORE);
		mission.requireSystemNotHasPulsar();
		mission.preferSystemBlackHoleOrNebula();
		mission.preferSystemOnFringeOfSector();
		
		StarSystemAPI system = mission.pickSystem();
		data.system = system;
	
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		OfficerQuality oQuality = OfficerQuality.AI_GAMMA;
		OfficerNum oNum = OfficerNum.ALL_SHIPS;
		
		if (difficulty <= 5) {
			size = FleetSize.MEDIUM;
		} else if (difficulty == 6) {
			size = FleetSize.LARGE;
		} else if (difficulty == 7) {
			size = FleetSize.LARGE;
		} else if (difficulty == 8) {
			size = FleetSize.VERY_LARGE;
		} else if (difficulty == 9) {
			size = FleetSize.HUGE;
		} else if (difficulty >= 10) {
			size = FleetSize.MAXIMUM;
		}
		
		beginFleet(mission, data);
		mission.triggerCreateFleet(size, quality, Factions.DERELICT, FleetTypes.PATROL_MEDIUM, data.system);
		mission.triggerSetFleetOfficers(oNum, oQuality);
		mission.triggerAutoAdjustFleetSize(size, size.next());
		mission.triggerSetRemnantConfigActive();
		mission.triggerSetFleetFaction(Factions.DERELICT);
		mission.triggerFleetSetName("Derelict Fleet");
		mission.triggerFleetAddTags(Tags.NEUTRINO_HIGH);
		mission.triggerFleetAddCommanderSkill(Skills.DERELICT_CONTINGENT, 1);
		mission.triggerMakeHostileAndAggressive();
		mission.triggerMakeNoRepImpact();
		//mission.triggerSetFleetMemoryValue("$shownFleetDescAlready", true);
		mission.triggerDoNotShowFleetDesc();
		mission.triggerFleetForceAutofitOnAllShips();
		mission.triggerFleetSetAllWeapons();
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		mission.triggerFleetSetPatrolActionText("taking scientific readings");
		mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.NEUTRINO, Tags.NEUTRINO_HIGH, Tags.GAS_GIANT);
		
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.DERELICT_MULT, mission);
		
		return data;
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return 5;
	}

}






