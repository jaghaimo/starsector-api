package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.util.List;
import java.util.Map;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TransmitterTrapSpecial;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GATransverseJump extends GABaseMission {

	public static enum Stage {
		GO_TO_OUTPOST,
		RETURN_TO_ACADEMY,
		COMPLETED,
		FAILED,
	}
	
	//public static List<SectorEntityToken> system_jumppoints; 
	
	protected StarSystemAPI system;
	protected PlanetAPI planet;
	protected PersonAPI researcher;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// replaced this with "non_repeatable" tag in person_missions.csv
		// if this mission was EVER completed by the player, abort!
		//if (getCreator().getNumCompleted() > 0) return false;
		
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaTJ_ref")) {
			return false;
		}
		
		//genRandom = Misc.random; // for testing purposes to roll new search results each time

		resetSearch();
		// expanded from just THEME_MISC
		// since the various jump point-related requirements narrows down the valid options
		requireSystemTags(ReqMode.ANY, Tags.THEME_MISC, Tags.THEME_MISC_SKIP, Tags.THEME_RUINS);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE);
		requireSystemNotAlreadyUsedForStory();
		requireSystemNotHasPulsar();
		requireSystemHasAtLeastNumJumpPoints(2);
		preferSystemHasAtLeastNumJumpPoints(3);
		preferSystemOnFringeOfSector();
		preferSystemUnexplored();
		//preferSystemInDirectionOfOtherMissions();
		requirePlanetNotStar();
		requirePlanetUnpopulated();
		requirePlanetNotGasGiant();
		preferPlanetNotNearJumpPoint(5000f);
		preferPlanetNotNearJumpPoint(2000f);
		preferPlanetNotFullySurveyed();

		planet = pickPlanet();
		
		if (planet == null) {
			return false;
		}
		
		system = planet.getStarSystem();
		
		setStartingStage(Stage.GO_TO_OUTPOST);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		setStoryMission();
		
		//setMusic(planet, MusicPlayerPluginImpl.MUSIC_ENCOUNTER_MYSTERIOUS_AGGRO, Stage.GO_TO_OUTPOST);
		
		makeImportant(planet, "$gaTJ_targetPlanet", Stage.GO_TO_OUTPOST);
		makeImportant(getPerson(), "$gaTJ_needToReturn", Stage.RETURN_TO_ACADEMY);
		
		connectWithGlobalFlag(Stage.GO_TO_OUTPOST, Stage.RETURN_TO_ACADEMY, "$gaTJ_needToReturn");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaTJ_completed");
		
		
		setCreditReward(CreditReward.HIGH);

		researcher = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(genRandom);
		researcher.setRankId(Ranks.CITIZEN);
		researcher.setPostId(Ranks.POST_ACADEMICIAN);
		
		// Tri-Tach black ops merc patrol(s) spawn
		// ideally spawned to guard all possible system jump-points
		// then, once player grabs Baird's spy, they all move to planet and start search pattern
		
		List<SectorEntityToken> jumpPoints = system.getJumpPoints();
		for (SectorEntityToken point : jumpPoints) {
			addMercFleet(point);
		}
		
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValuePermanent("$asebSayBairdWantsToTalk", true);
		endTrigger();
		
		setSystemWasUsedForStory(Stage.GO_TO_OUTPOST, system);

		return true;
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								Map<String, MemoryAPI> memoryMap) {
		if (dialog != null && action.equals("showResearcher")) {
			showPersonInfo(researcher, dialog, false, false);
			return true;
		}
		if ("triggerMercFleets".equals(action)) {
			for (CampaignFleetAPI fleet : system.getFleets()) {
				if (fleet.getMemoryWithoutUpdate().contains("$gaTJ_merc")) {
					TransmitterTrapSpecial.makeFleetInterceptPlayer(fleet, true, false, true, 1000f);
				}
			}
			
			return true;
		}
		return false;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaTJ_planetId", planet.getId());
		set("$gaTJ_planetName", planet.getName());
		set("$gaTJ_systemName", planet.getStarSystem().getNameWithNoType());
		set("$gaTJ_dist", getDistanceLY(planet));
		set("$gaTJ_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaTJ_researcherName", researcher.getNameString());
		set("$gaTJ_heOrShe", researcher.getHeOrShe());
		set("$gaTJ_HeOrShe", researcher.getHeOrShe().substring(0, 1).toUpperCase() + researcher.getHeOrShe().substring(1));
		set("$gaTJ_hisOrHer", researcher.getHisOrHer());
		set("$gaTJ_HisOrHet", researcher.getHisOrHer().substring(0, 1).toUpperCase() + researcher.getHisOrHer().substring(1));
		set("$gaTJ_himOrHer", researcher.getHimOrHer());
		set("$gaTJ_HimOrHet", researcher.getHimOrHer().substring(0, 1).toUpperCase() + researcher.getHimOrHer().substring(1));

	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextPre(planet) +
					", and contact the 'researcher' at the Tri-Tachyon black research site located there.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			// shared way to get the same text to ensure consistency across different missions
			info.addPara(getReturnText("the Galatia Academy") + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextShort(planet), tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			// shared way to get the same text to ensure consistency across different missions
			info.addPara(getReturnTextShort("the Galatia Academy"), tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Extract 'Researcher'";
	}
	
	protected void addMercFleet(SectorEntityToken patrolPoint) {
		// changed requirePlayerInHyperspace to false so that if ctrl-clicking into the system, the mercs
		// still spawn. Generally only want to requirePlayerInHyperspace for an encounter intended to be in
		// hyperspace, and even then that's situational
		beginWithinHyperspaceRangeTrigger(planet, 3f, false, Stage.GO_TO_OUTPOST);
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.VERY_HIGH, Factions.MERCENARY, FleetTypes.PATROL_LARGE, system);
		triggerSetFleetFaction(Factions.INDEPENDENT);
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		
		// do we want this here? Probably not, this is a one-off mission and so the strength shouldn't scale
		//triggerAutoAdjustFleetStrengthMajor();
		
		triggerMakeHostileAndAggressive();
		triggerMakeNoRepImpact();
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		
		triggerPickLocationAroundEntity(patrolPoint, 1000f);
		triggerSpawnFleetAtPickedLocation("$gaTJ_merc", null);
		triggerOrderFleetPatrol(true, patrolPoint);
		
		//triggerFleetMakeImportant(null, Stage.RETURN_TO_ACADEMY);
		endTrigger();
	}
}





