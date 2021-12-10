package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GAOutpost extends GABaseMission {

	public static float MISSION_DAYS = 120f;
	public static float PROB_DESTROYED = 0.5f;
	public static float PROB_ROGUE_AI = 0.6f;
	public static float PROB_DESTROYED_PIRATES = 1f/6f;
	public static float PROB_DESTROYED_PATHERS = 1f/5f; // 4/6 chance for DESTROYED_OTHER
	public static float PROB_HOSTILE_FLEET = 0.5f;
	
	
	//public static float PROB_PATHER_OR_PIRATE = 0.5f;
//	public static int RAID_DIFFICULTY = 100;
//	public static int MARINES_REQUIRED = RAID_DIFFICULTY / 2;
	
	
	public static enum Stage {
		GO_TO_OUTPOST,
		RETURN_TO_ACADEMY,
		COMPLETED,
		FAILED,
	}
	
	public static enum Variation {
		BASIC,
		DESTROYED_PATHERS,
		DESTROYED_PIRATES,
		DESTROYED_OTHER,
		ROGUE_AI,
	}
	
	protected PlanetAPI planet;
	protected Variation variation;
	protected PersonAPI leader;
	protected PersonAPI core;
	protected int terribleEnd;
	protected int bizarreProject;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaOp_ref")) {
			return false;
		}
		
		pickDepartment(GADepartments.INDUSTRIAL, GADepartments.MILITARY, GADepartments.SCIENCE, GADepartments.WEIRD);
		
		if (rollProbability(PROB_DESTROYED)) {
			if (rollProbability(PROB_DESTROYED_PIRATES)) {
				variation = Variation.DESTROYED_PIRATES;
			} else if (rollProbability(PROB_DESTROYED_PATHERS)) {
				variation = Variation.DESTROYED_PATHERS;
			} else {
				variation = Variation.DESTROYED_OTHER;
				terribleEnd = genRandom.nextInt(4);
			}
		} else if (rollProbability(PROB_ROGUE_AI)) {
			variation = Variation.ROGUE_AI;
			bizarreProject = genRandom.nextInt(7);
		} else {
			variation = Variation.BASIC;
		}
		
//		variation = Variation.BASIC;
//		variation = Variation.DESTROYED_OTHER;
//		terribleEnd = genRandom.nextInt(4);
//		variation = Variation.DESTROYED_PATHERS;
//		PROB_HOSTILE_FLEET = 1f;
//		variation = Variation.ROGUE_AI;
//		bizarreProject = genRandom.nextInt(7);
		
		resetSearch();
		
		//genRandom = new Random();
//		requireSystemTags(ReqMode.ANY, Tags.THEME_REMNANT, Tags.THEME_DERELICT, 
//						  Tags.THEME_MISC, Tags.THEME_MISC_SKIP, Tags.THEME_RUINS);
		preferSystemInteresting();
		preferSystemOnFringeOfSector();
		preferSystemUnexplored();
		requirePlanetNotStar();
		requirePlanetUnpopulated();
		requirePlanetNotGasGiant();
		preferPlanetNotFullySurveyed();
		preferPlanetInDirectionOfOtherMissions();

		planet = pickPlanet();
		
		if (planet == null) {
			return false;
		}
		
		setStartingStage(Stage.GO_TO_OUTPOST);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		makeImportant(planet, "$gaOp_targetPlanet", Stage.GO_TO_OUTPOST);
		makeImportant(getPerson(), "$gaOp_returnHere", Stage.RETURN_TO_ACADEMY);
		
		connectWithGlobalFlag(Stage.GO_TO_OUTPOST, Stage.RETURN_TO_ACADEMY, "$gaOp_needToReturn");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaOp_completed");
		
		if (WITH_TIME_LIMIT) {
			setTimeLimit(Stage.FAILED, MISSION_DAYS, planet.getStarSystem(), Stage.RETURN_TO_ACADEMY);
		}
		//setCreditReward(40000, 50000);
		setCreditReward(CreditReward.AVERAGE);
		setDefaultGARepRewards();
		
		leader = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(genRandom);
		leader.setRankId(Ranks.CITIZEN);
		leader.setPostId(Ranks.POST_ACADEMICIAN);
		
		core = new AICoreOfficerPluginImpl().createPerson(Commodities.BETA_CORE, Factions.NEUTRAL, genRandom);
		
		
		if (variation == Variation.DESTROYED_PIRATES && rollProbability(PROB_HOSTILE_FLEET)) {
			beginStageTrigger(Stage.RETURN_TO_ACADEMY);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, planet);
			triggerAutoAdjustFleetStrengthModerate();
			triggerFleetAllowLongPursuit();
			triggerPickLocationAroundEntity(planet, 3000f);
			triggerSpawnFleetAtPickedLocation(null, null);
			triggerOrderFleetPatrol(planet);
			endTrigger();
		} else if (variation == Variation.DESTROYED_PATHERS && rollProbability(PROB_HOSTILE_FLEET)) {
			beginStageTrigger(Stage.RETURN_TO_ACADEMY);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.LUDDIC_PATH, FleetTypes.PATROL_MEDIUM, planet);
			triggerAutoAdjustFleetStrengthModerate();
			triggerFleetAllowLongPursuit();
			triggerFleetPatherNoDefaultTithe();
			triggerPickLocationAroundEntity(planet, 3000f);
			triggerSpawnFleetAtPickedLocation(null, null);
			triggerOrderFleetPatrol(planet);
			endTrigger();
		}
		
		return true;
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								Map<String, MemoryAPI> memoryMap) {
		if (dialog != null && action.equals("showLeader")) {
			showPersonInfo(leader, dialog, false, false);
			return true;
		}
		if (dialog != null && action.equals("showCore")) {
			showPersonInfo(core, dialog, false, false);
			return true;
		}
		if (dialog != null && action.equals("doubleReward")) {
			setCreditReward(getCreditsReward() * 2);
			return true;
		}
		return false;
	}

	protected void updateInteractionDataImpl() {
		set("$gaOp_department", department);
		set("$gaOp_planetId", planet.getId());
		set("$gaOp_planetName", planet.getName());
		set("$gaOp_systemName", planet.getStarSystem().getNameWithNoType());
		set("$gaOp_dist", getDistanceLY(planet));
		set("$gaOp_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaOp_leaderHeOrShe", leader.getHeOrShe());
		set("$gaOp_leaderHisOrHer", leader.getHisOrHer());
		//variation = Variation.BASIC;
		set("$gaOp_variation", variation);
		set("$gaOp_terribleEnd", terribleEnd);
		set("$gaOp_bizarreProject", bizarreProject);
		set("$gaOp_destroyed", variation == Variation.DESTROYED_OTHER || 
							   variation == Variation.DESTROYED_PATHERS ||
							   variation == Variation.DESTROYED_PIRATES);
//		if (variation == Variation.DECIV) {
//			set("$gaOp_marinesReq", MARINES_REQUIRED);
//			set("$gaOp_raidDifficulty", RAID_DIFFICULTY);
//		} else if (variation == Variation.PULSAR) {
//			PlanetAPI pulsar = Misc.getPulsarInSystem(planet.getStarSystem());
//			if (pulsar != null) {
//				set("$gaOp_pulsarName", planet.getStarSystem().getNameWithNoType());
//			}
//		}
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextPre(planet) +
					", and drop off a small team of scientists at an outpost located there.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and talk to " + 
						 getPerson().getNameString() + " to receive your reward.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextShort(planet), tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and talk to " + getPerson().getNameString(), tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Science Team Transport";
	}

}





