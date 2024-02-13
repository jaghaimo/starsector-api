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

public class CBRemnant extends BaseCustomBountyCreator {

	public static float PROB_IN_SYSTEM_WITH_BASE = 0.5f;
	public static float PROB_IN_SYSTEM_WITH_TAP = 0.25f;
	
	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		return super.getFrequency(mission, difficulty) * CBStats.REMNANT_FREQ;
	}

	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Remnant Fleet";
	}
	
	@Override
	public String getIconName() {
		return Global.getSettings().getSpriteName("campaignMissions", "remnant_bounty");
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		//mission.requireSystem(this);
		mission.requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_CORE);
//		mission.requireSystemTags(ReqMode.ANY, Tags.THEME_RUINS, Tags.THEME_MISC, Tags.THEME_REMNANT,
//				  Tags.THEME_DERELICT, Tags.THEME_REMNANT_DESTROYED);
		mission.preferSystemInteresting();
		mission.preferSystemUnexplored();
		mission.requireSystemNotHasPulsar();		
		if (difficulty >= 9 && mission.rollProbability(PROB_IN_SYSTEM_WITH_BASE)) {
			mission.preferSystemTags(ReqMode.ANY, Tags.THEME_REMNANT_MAIN);
			mission.requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_REMNANT_DESTROYED);
		} else if (mission.rollProbability(PROB_IN_SYSTEM_WITH_TAP)) {
			mission.preferSystemTags(ReqMode.ANY, Tags.HAS_CORONAL_TAP);
		} else {
			mission.preferSystemBlackHoleOrNebula();
			mission.preferSystemOnFringeOfSector();
		}
		
		StarSystemAPI system = mission.pickSystem();
		data.system = system;
	
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.VERY_HIGH;
		OfficerQuality oQuality = OfficerQuality.AI_MIXED;
		OfficerNum oNum = OfficerNum.ALL_SHIPS;
		String type = FleetTypes.PATROL_SMALL;
		
		if (difficulty == 7) {
			size = FleetSize.LARGE;
			type = FleetTypes.PATROL_MEDIUM;
			oQuality = OfficerQuality.AI_BETA_OR_GAMMA;
		} else if (difficulty == 8) {
			size = FleetSize.VERY_LARGE;
			type = FleetTypes.PATROL_LARGE;
			oQuality = OfficerQuality.AI_MIXED;
		} else if (difficulty == 9) {
			size = FleetSize.HUGE;
			type = FleetTypes.PATROL_LARGE;
			oQuality = OfficerQuality.AI_ALPHA;
		} else if (difficulty >= 10) {
			size = FleetSize.MAXIMUM;
			type = FleetTypes.PATROL_LARGE;
			oQuality = OfficerQuality.AI_ALPHA;
		}
		
		//setIconName("campaignMissions", "station_bounty");
		
		beginFleet(mission, data);
		if (false) {
			mission.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_2, Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, data.system);
			mission.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			mission.triggerSetFleetFaction(Factions.TRITACHYON);
			mission.triggerMakeHostileAndAggressive();
			mission.triggerMakeLowRepImpact();
			mission.triggerSetFleetDoctrineComp(0, 0, 5);
			mission.triggerFleetMakeFaster(true, 1, true);
		} else {
			mission.triggerCreateFleet(size, quality, Factions.REMNANTS, type, data.system);
			mission.triggerSetFleetDoctrineQuality(5, 3, 5);
			mission.triggerSetFleetOfficers(oNum, oQuality);
			mission.triggerAutoAdjustFleetSize(size, size.next());
			mission.triggerSetRemnantConfigActive();
			mission.triggerSetFleetNoCommanderSkills();
			mission.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
			//mission.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
			mission.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			
			mission.triggerFleetSetAllWeapons();
		}
//		if (difficulty >= 9) {
//			mission.triggerSetFleetDoctrineOther(5, -1);
//		}
		//mission.triggerSetFleetDoctrineRandomize(1f);
		//mission.triggerSetFleetDoctrineRandomize(0f);
		
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		//mission.triggerFleetSetPatrolActionText("patrolling");
		mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.NEUTRINO, Tags.NEUTRINO_HIGH, Tags.STATION,
									    Tags.SALVAGEABLE, Tags.GAS_GIANT);
		
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.REMNANT_MULT, mission);
		
		return data;
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return 7;
	}

}



