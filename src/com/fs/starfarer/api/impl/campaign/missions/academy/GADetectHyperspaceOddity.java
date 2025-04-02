package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.world.NamelessRock;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GADetectHyperspaceOddity extends GABaseMission {

	public static enum Stage {
		VISIT_ELEK,
		GO_TO_ARRAY,
		GO_TO_LOCATION,
		INVESTIGATE_SHIP,
		RETURN_TO_ELEK,
		COMPLETED,
		FAILED,
		ARRAY_THEN_ELEK,
		ARRAY_THEN_ABYSS,
		ABYSS_NO_ELEK,
	}
	
	protected StarSystemAPI arraySystem;
	protected StarSystemAPI oneslaughtSystem;
	protected SectorEntityToken array;
	protected SectorEntityToken oneslaught;
	protected PersonAPI elek;
	protected int rewardAmount;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		
		// if this mission was already accepted by the player, abort
		if (!setGlobalReference("$gaDHO_ref", "$gaDHO_inProgress")) return false;

		elek = getImportantPerson(People.ELEK);
		if (elek == null) return false;

		array = (SectorEntityToken) Global.getSector().getPersistentData().get(NamelessRock.ONESLAUGHT_SENSOR_ARRAY);
		if (array == null) return false;
		
		oneslaughtSystem = Global.getSector().getStarSystem(NamelessRock.NAMELESS_ROCK_LOCATION_ID);
		if (oneslaughtSystem == null) return false;
		
		
		for (SectorEntityToken entity : oneslaughtSystem.getAllEntities()) {
			if(entity.getMemoryWithoutUpdate().contains("$onslaughtMkI")) {
				oneslaught = entity;
				break;
			} 
		}
		if(oneslaught == null) return false;

		arraySystem = array.getStarSystem();
		if(arraySystem == null) return false;
		
		setStartingStage(Stage.VISIT_ELEK);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		setStageOnGlobalFlag(Stage.FAILED, "$gaDHO_declinedTwice");
		
		setStageOnGlobalFlag(Stage.GO_TO_ARRAY, "$gaDHO_gotScanPackage");
		setStageOnGlobalFlag(Stage.GO_TO_LOCATION, "$gaDHO_gotCoordinates");
		setStageOnGlobalFlag(Stage.INVESTIGATE_SHIP, "$gaDHO_foundRock");
		setStageOnGlobalFlag(Stage.RETURN_TO_ELEK, "$gaDHO_foundOneslaught");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaDHO_completed");
		
		setStageOnGlobalFlag(Stage.ARRAY_THEN_ELEK, "$gaDHO_arrayFirstThenElek");
		setStageOnGlobalFlag(Stage.ARRAY_THEN_ABYSS, "$gaDHO_arrayFirstThenAbyss");
		setStageOnGlobalFlag(Stage.ABYSS_NO_ELEK, "$gaDHO_playerMadeElekMad"); 
		
		makeImportant(elek, "$gaDHO_answerInvite", Stage.VISIT_ELEK);
		makeImportant(array, "$gaDHO_object", Stage.GO_TO_ARRAY);
		
		makeImportant(oneslaughtSystem.getEntityById("nameless_rock"), null, Stage.GO_TO_LOCATION);
		makeImportant(oneslaughtSystem.getEntityById("nameless_rock"), null, Stage.ARRAY_THEN_ABYSS);
		makeImportant(oneslaughtSystem.getEntityById("nameless_rock"), null, Stage.ABYSS_NO_ELEK);
		
		//SectorEntityToken node2 = spawnMissionNode(new LocData(oneslaughtSystem.getEntityById("nameless_rock")));
		//makeImportant(node2, null, Stage.GO_TO_ARRAY);
		
		makeImportant(elek, "$gaDHO_elekReturn", Stage.RETURN_TO_ELEK);
		makeImportant(elek, "$gaDHO_elekReturn", Stage.ARRAY_THEN_ELEK);
		
		makeImportant(oneslaught, null, Stage.INVESTIGATE_SHIP);

		setMapMarkerNameColorBasedOnStar(arraySystem);
		setStoryMission();
		
		setStartingStage(Stage.VISIT_ELEK);
		addSuccessStages(Stage.COMPLETED);
		
		// spawn a spicy pirate fleet if player found the array first.
		beginStageTrigger(Stage.VISIT_ELEK);
		triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.HIGHER, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, array.getStarSystem());
		triggerPickLocationAtClosestToPlayerJumpPoint(array.getStarSystem());
		triggerSpawnFleetAtPickedLocation("$gaDHO_arrayFleet", null);
		triggerOrderFleetPatrol(false, array);
		//triggerOrderFleetInterceptPlayer();
		endTrigger();

		
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$gaDHO_missionCompleted", true);
		//triggerMakeNonStoryCritical(kazeron, chicomoztoc, epiphany, siyavong.getMarket(), kanta.getMarket());
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaDHO_stage", getCurrentStage());
		set("$gaDHO_starName", arraySystem.getNameWithNoType());
		set("$gaDHO_systemName", arraySystem.getNameWithLowercaseTypeShort());
		set("$gaDHO_dist", getDistanceLY(array));
		set("$gaDHO_reward", Misc.getWithDGS(rewardAmount));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		//if(Global.getSector().getMemoryWithoutUpdate().contains("$gaDHO_foundArrayFirst")) { 
		
		if (currentStage == Stage.VISIT_ELEK) {
			if(Global.getSector().getMemoryWithoutUpdate().contains("$gaDHO_foundArrayFirst")) { 
				info.addPara("Talk to Academician " + elek.getNameString() + " about the sensor array data.", opad);
				info.addPara("Or go directly to the coordinates held in the array's data.", opad);
				info.addImage(elek.getPortraitSprite(), width, 128, opad);
			}
			else {
				info.addPara("Talk to Academician " + elek.getNameString() + " about the services he requires. "
					+ "He is at the Galatia Academy, which orbits Pontus in the Galatia system.", opad);
				info.addImage(elek.getPortraitSprite(), width, 128, opad);
			}
		}
		else if (currentStage == Stage.GO_TO_ARRAY) { 
			info.addPara("Install the modification package on the Sensor Array located in the " + 
					arraySystem.getNameWithLowercaseTypeShort() + ".", opad);
			info.addImage(Global.getSettings().getSpriteName("illustrations", "sensor_array"), width, opad);
		}
		else if (currentStage == Stage.GO_TO_LOCATION) { 
			info.addPara("Go to the location deep in the Abyss to find what Academician Elek's patron was looking for.", opad);
		}
		else if (currentStage == Stage.ARRAY_THEN_ABYSS) { 
			info.addPara("Go to the location deep in the Abyss to find out what the sensor array detected.", opad);
		}
		else if (currentStage == Stage.ABYSS_NO_ELEK) { 
			info.addPara("Go to the location deep in the Abyss to find out what the sensor array detected.", opad);
		}
		else if (currentStage == Stage.ARRAY_THEN_ELEK) { 
			info.addPara("Talk to Academician " + elek.getNameString() + " about what his sensor array modifications were looking for. "
					+ "He is at the Galatia Academy, which orbits Pontus in the Galatia system.", opad);
				info.addImage(elek.getPortraitSprite(), width, 128, opad);
		}
		else if (currentStage == Stage.INVESTIGATE_SHIP) { 
			info.addPara("Investigate the strange ship orbiting Nameless Rock.", opad);
		}
		else if (currentStage == Stage.RETURN_TO_ELEK) { 
			if(Global.getSector().getMemoryWithoutUpdate().contains("$gaDHO_arrayFirstThenAbyss")) { 
				info.addPara("Talk to Academician " + elek.getNameString() + " about what he thought he was looking for.", opad);
				info.addPara("And maybe some kind of reward.", opad);
			}
			else {
				info.addPara("Talk to Academician " + elek.getNameString() + " about a reward. And about his 'patron'.", opad);
			}
			
			info.addImage(elek.getPortraitSprite(), width, 128, opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
			//
		if (currentStage == Stage.VISIT_ELEK) {
			if(Global.getSector().getMemoryWithoutUpdate().contains("$gaDHO_foundArrayFirst")) {  
				info.addPara("Talk to Academician " + elek.getNameString() + " at the Galatia Academy about the sensor array data.", tc, pad);
				info.addPara("Or go directly to the coordinates extracted from the array's data", tc, pad);
			}
			else {
				info.addPara("Talk to Academician " + elek.getNameString() + " at the Galatia Academy, which orbits Pontus in the Galatia system.", tc, pad);
			}
			return true;
		}
		else if (currentStage == Stage.GO_TO_ARRAY) { 
			info.addPara("Go to the Sensor Array located in the " + 
					arraySystem.getNameWithLowercaseTypeShort() + ".", tc, pad);
			return true;
		}
		else if (currentStage == Stage.GO_TO_LOCATION) { 
			info.addPara("Go to the location deep in the Abyss and discover what the Sensor Array found.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.ARRAY_THEN_ABYSS) { 
			info.addPara("Go to the location deep in the Abyss and discover what the Sensor Array found.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.ABYSS_NO_ELEK) { 
			info.addPara("Go to the location deep in the Abyss and discover what the Sensor Array found.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.ARRAY_THEN_ELEK) { 
			info.addPara("Talk to Academician " + elek.getNameString() + " at the Galatia Academy.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_ELEK) { 
			info.addPara("Talk to Academician " + elek.getNameString() + 
					" at the Galatia Academy.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.GO_TO_LOCATION) { 
			info.addPara("Go to the location deep in the Abyss and discover what the Sensor Array found.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.INVESTIGATE_SHIP) { 
				info.addPara("Investigate the strange ship orbiting Nameless Rock.", tc, pad);
				return true;
		}
		else if (currentStage == Stage.RETURN_TO_ELEK) { 
			 // return to elek *after finding the Oneslaught* to finish the mission.
			 info.addPara("Talk to Academician " + elek.getNameString() + " at the Galatia Academy.", tc, pad);
			 return true;
		}

		return false;
	}
	
	/*
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if ("discoveredArrayOutOfSequence".equals(action)) {

			
			return true;
		}
		return false;
	}*/
	
	@Override
	public String getBaseName() {
		//return "Detect Hyperspace Oddity";
		return "Abyssal Space Oddity";
	}
	
}


