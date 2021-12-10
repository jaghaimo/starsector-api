package com.fs.starfarer.api.impl.campaign.missions.cb;

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

public class CBMerc extends BaseCustomBountyCreator {

	public static float PROB_SMALL_FLEET = 0.5f;
	public static float PROB_SOME_PHASE_IN_SMALL_FLEET = 0.5f;
	public static float PROB_CARRIER_BASED_LARGE_FLEET = 0.33f;
	public static float PROB_SOME_PHASE_IN_LARGE_FLEET = 0.5f;
	
	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		return super.getFrequency(mission, difficulty) * CBStats.MERC_FREQ;
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Elite Mercenary";
	}
	
	protected StarSystemAPI findSystem(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
//		mission.requireSystemTags(ReqMode.ANY, Tags.THEME_RUINS, Tags.THEME_MISC, Tags.THEME_REMNANT_SECONDARY,
//								  Tags.THEME_DERELICT, Tags.THEME_REMNANT_DESTROYED);
		mission.requireSystemInterestingAndNotUnsafeOrCore();
		mission.requireSystemNotHasPulsar();

		StarSystemAPI system = mission.pickSystem();
		return system;		
	}
	
	protected boolean isAggro() {
		return true;
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		data.system = findSystem(createdAt, mission, difficulty, bountyStage);
		if (data.system == null) return null;
	
//		FleetSize size = FleetSize.MEDIUM;
//		FleetSize sizeWolfpack = FleetSize.MEDIUM;
//		FleetQuality quality = FleetQuality.VERY_HIGH;
//		FleetQuality qualityWolfpack = FleetQuality.SMOD_3;
		
		boolean smallFleet = mission.rollProbability(PROB_SMALL_FLEET);
		boolean smallUsePhase = mission.rollProbability(PROB_SOME_PHASE_IN_SMALL_FLEET);
		boolean largeUsePhase = mission.rollProbability(PROB_SOME_PHASE_IN_LARGE_FLEET);
		boolean largeUseCarriers = mission.rollProbability(PROB_CARRIER_BASED_LARGE_FLEET);
		
//		smallFleet = true;
//		largeUseCarriers = false;
//		largeUsePhase = true;
//		smallUsePhase = true;
		
		beginFleet(mission, data);
		if (smallFleet) {
			data.custom1 = true;
			if (difficulty <= 6) {
				mission.triggerCreateFleet(FleetSize.SMALL, FleetQuality.SMOD_3, 
						   				   Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, data.system);
				mission.triggerSetFleetMaxShipSize(1);
				mission.triggerSetFleetDoctrineOther(1, 4);
			} else if (difficulty <= 7) {
				mission.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.SMOD_3, 
		   				   				   Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, data.system);
				mission.triggerSetFleetMaxShipSize(2);
				mission.triggerSetFleetDoctrineOther(2, 4);
			} else if (difficulty <= 8) {
				mission.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.SMOD_3, 
						Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, data.system);
				mission.triggerSetFleetMaxShipSize(3);
				mission.triggerSetFleetDoctrineOther(3, 4);
			} else if (difficulty <= 9) {
				mission.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_3, 
						Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, data.system);
				mission.triggerSetFleetDoctrineOther(4, 4);
			} else if (difficulty <= 10) {
				mission.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_3, 
						Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, data.system);
				mission.triggerSetFleetDoctrineOther(5, 4);
			}
			
			mission.triggerSetFleetMaxNumShips(12);
			
			if (smallUsePhase) {
				if (difficulty <= 8) {
					mission.triggerSetFleetDoctrineComp(0, 0, 5);
				} else {
					mission.triggerSetFleetDoctrineComp(4, 0, 3);
				}
			} else {
				mission.triggerSetFleetDoctrineComp(5, 0, 0);
			}
			
			mission.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			mission.triggerFleetAddCommanderSkill(Skills.WOLFPACK_TACTICS, 1);
			mission.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
			mission.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
			mission.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
			mission.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.UNUSUALLY_HIGH);
		} else {
			if (difficulty <= 6) {
				mission.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.VERY_HIGH, 
										   Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, data.system);
			} else if (difficulty <= 7) {
				mission.triggerCreateFleet(FleetSize.LARGE, FleetQuality.VERY_HIGH, 
						   				   Factions.MERCENARY, FleetTypes.MERC_PRIVATEER, data.system);
			} else if (difficulty <= 8) {
				mission.triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.VERY_HIGH, 
										   Factions.MERCENARY, FleetTypes.MERC_ARMADA, data.system);
			} else if (difficulty <= 9) {
				mission.triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.SMOD_1, 
										   Factions.MERCENARY, FleetTypes.MERC_ARMADA, data.system);
			} else if (difficulty <= 10) {
				mission.triggerCreateFleet(FleetSize.HUGE, FleetQuality.SMOD_2, 
										   Factions.MERCENARY, FleetTypes.MERC_ARMADA, data.system);
			}
			
			if (largeUseCarriers) {
				mission.triggerSetFleetDoctrineComp(3, 4, 0);
				mission.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
				mission.triggerFleetAddCommanderSkill(Skills.FIGHTER_UPLINK, 1);
			} else {
				if (largeUsePhase) {
					mission.triggerSetFleetDoctrineComp(3, 0, 2);
					mission.triggerFleetAddCommanderSkill(Skills.PHASE_CORPS, 1);
					mission.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
				} else {
					mission.triggerSetFleetDoctrineComp(5, 0, 0);
					mission.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
					mission.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
				}
			}
			
			mission.triggerSetFleetDoctrineOther(3, 3);
			mission.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		}
		
		
		mission.triggerSetFleetNoCommanderSkills();
		mission.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
		mission.triggerSetFleetFaction(Factions.INDEPENDENT);
		if (isAggro()) {
			mission.triggerMakeHostileAndAggressive();
			mission.triggerMakeNoRepImpact();
		}
		mission.triggerFleetAllowLongPursuit();
		mission.triggerDoNotShowFleetDesc();
		mission.triggerFleetSetAllWeapons();
		
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		//mission.triggerFleetSetPatrolActionText("patrolling");
		mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.SALVAGEABLE, Tags.PLANET, Tags.OBJECTIVE);
		
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.MERC_MULT, mission);
		
		return data;
	}
	
	@Override
	public void updateInteractionData(HubMissionWithBarEvent mission, CustomBountyData data) {
		String id = mission.getMissionId();
		if (data.custom1 != null) {
			mission.set("$" + id + "_smallMerc", data.difficulty);
			mission.set("$bcb_smallMerc", data.difficulty);
		} else {
			mission.unset("$" + id + "_smallMerc");
			mission.unset("$bcb_smallMerc");
		}
	}

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return 6;
	}

}






