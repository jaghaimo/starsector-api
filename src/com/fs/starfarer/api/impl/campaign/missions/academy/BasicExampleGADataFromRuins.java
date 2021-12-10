package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BasicExampleGADataFromRuins extends GABaseMission {

//	public static class GADataFromRuinsCreator extends BaseHubMissionCreator {
//		@Override
//		public HubMission createHubMission(MissionHub hub) {
//			return new BasicExampleGADataFromRuins();
//		}
//	}
	
	public static float MISSION_DAYS = 120f;
	
	public static enum Stage {
		GO_TO_RUINS,
		GET_IN_COMMS_RANGE,
		COMPLETED,
		FAIL_TIME,
	}
	
	protected PlanetAPI planet;
	protected String target;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaData_ref")) {
			return false;
		}
		
		pickDepartment(GADepartments.INDUSTRIAL, GADepartments.SOCIAL);
		target = pickOne("library", "datavault", "archive", "laboratory");
		
		requireSystemTags(ReqMode.ANY, Tags.THEME_REMNANT_RESURGENT, Tags.THEME_REMNANT_SUPPRESSED,
									   Tags.THEME_DERELICT, Tags.THEME_MISC, Tags.THEME_RUINS);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_REMNANT_SECONDARY);
		//requireSystemInInnerSector();
		requirePlanetUnpopulated();
		requirePlanetWithRuins();
		preferPlanetNotFullySurveyed();
		preferPlanetUnexploredRuins();
		planet = pickPlanet();
		
		if (planet == null) {
			return false;
		}
		
		setStartingStage(Stage.GO_TO_RUINS);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAIL_TIME);
		
		connectWithGlobalFlag(Stage.GO_TO_RUINS, Stage.GET_IN_COMMS_RANGE, "$gaData_gotData");
		connectWithInRangeOfCommRelay(Stage.GET_IN_COMMS_RANGE, Stage.COMPLETED);
		
		makeImportant(planet, "$gaData_targetPlanet", Stage.GO_TO_RUINS);
		
		setTimeLimit(Stage.FAIL_TIME, MISSION_DAYS, planet.getStarSystem(), Stage.GET_IN_COMMS_RANGE);
		//setCreditReward(30000, 60000);
		setCreditReward(CreditReward.AVERAGE);
		
		beginWithinHyperspaceRangeTrigger(planet, 1f, false, Stage.GO_TO_RUINS);
		triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, planet);
		triggerSetStandardAggroPirateFlags();
		//triggerPickLocationAroundEntity(planet, 3000f);
		triggerPickLocationAtInSystemJumpPoint(planet.getStarSystem());
		triggerSpawnFleetAtPickedLocation("$gaData_pirate", null);
		triggerOrderFleetPatrol(planet);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaData_department", department);
		set("$gaData_target", target);
		set("$gaData_planetName", planet.getName());
		set("$gaData_systemName", planet.getStarSystem().getNameWithNoType());
		set("$gaData_dist", getDistanceLY(planet));
		set("$gaData_reward", Misc.getWithDGS(getCreditsReward()));
	}
	
	@Override
	public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_RUINS) {
			info.addPara("Go to " + planet.getName() + " and retrieve the data from the ruins.", opad); 	
		} else if (currentStage == Stage.GET_IN_COMMS_RANGE) {
			info.addPara("Get within range of a functional comm relay to complete the mission and receive " +
					"your reward.", opad);
		} else {
			super.addDescriptionForCurrentStage(info, width, height);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_RUINS) {
			info.addPara("Go to " + planet.getName() + "", tc, pad);
			return true;
		} else if (currentStage == Stage.GET_IN_COMMS_RANGE) {
			info.addPara("Get within comms range to complete the mission", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Ruins Data Recovery";
	}
	
	@Override
	public String getBlurbText() {
		return null; // this should be done via rules.csv
//		return "The " + department + " department has turned up records of a data cache on " +
//				planet.getName() + " in the " + planet.getStarSystem().getNameWithNoType() + " system; " +
//				"they just need someone to go find it and transmit the contents back to the Academy.";
	}

}


