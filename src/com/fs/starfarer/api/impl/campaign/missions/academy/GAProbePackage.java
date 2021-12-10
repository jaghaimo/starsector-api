package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GAProbePackage extends GABaseMission {

	public static float MISSION_DAYS = 120f;
	public static float PROB_PATHERS = 0.5f;
	public static float PROB_NOT_ASTEROIDS = 0.5f;
	
	public static float PROB_IT_BLEW_UP = 0.33f;
	public static float PROB_MISSING = 0.5f;
	public static float PROB_SCAVENGER = 0.5f;
	
	
	public static enum Stage {
		GO_TO_PROBE,
		FIND_SCAVENGER,
		RETURN_TO_ACADEMY,
		COMPLETED,
		FAILED,
	}
	
	public static enum Variation {
		BASIC,
		SCAVENGER,
		IT_BLEW_UP,
		PACKAGE_MISSING,
	}
	
	protected StarSystemAPI system;
	protected Variation variation;
	protected CampaignTerrainAPI object;
	protected SectorEntityToken probe;
	protected int hazardPay;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaProbe_ref")) {
			return false;
		}

		//genRandom = Misc.random;

		pickDepartmentAllTags(GADepartments.SCIENCE, GADepartments.WEIRD);

		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE);
		requireTerrainType(ReqMode.ANY, Terrain.ASTEROID_BELT, Terrain.ASTEROID_FIELD, 
						   Terrain.MAGNETIC_FIELD, Terrain.NEBULA, Terrain.RING);
		if (rollProbability(PROB_NOT_ASTEROIDS)) {
			preferTerrainType(ReqMode.NOT_ANY, Terrain.ASTEROID_BELT, Terrain.ASTEROID_FIELD);
		}

		// important to put these last - requirements that come earlier have higher priority
		preferSystemNotPulsar();
		preferSystemNotBlackHole();
		preferSystemUnexplored();
		preferTerrainInDirectionOfOtherMissions();

		object = pickTerrain();

		if (object == null) {
			return false;
		}

		system = object.getStarSystem();

		//		PROB_IT_BLEW_UP = 1f;
		//		PROB_PATHERS = 1f;

		if (rollProbability(PROB_IT_BLEW_UP)) {
			variation = Variation.IT_BLEW_UP;
		} else if (rollProbability(PROB_MISSING)) {
			variation = Variation.PACKAGE_MISSING;
		} else if (rollProbability(PROB_SCAVENGER)) {
			variation = Variation.SCAVENGER;
		} else {
			variation = Variation.BASIC;
		}
		//variation = Variation.SCAVENGER;
		//variation = Variation.BASIC;
		//variation = Variation.IT_BLEW_UP;
		//variation = Variation.PACKAGE_MISSING;

		setStartingStage(Stage.GO_TO_PROBE);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);

		SectorEntityToken probe = spawnEntity(Entities.GENERIC_PROBE, new LocData(object));
		if (probe == null) return false;

		probe.setId("gaProbe_probe");
		makeImportant(probe, "$gaProbe_probe", Stage.GO_TO_PROBE);
		makeImportant(getPerson(), "$gaProbe_returnHere", Stage.RETURN_TO_ACADEMY);

		connectWithGlobalFlag(Stage.GO_TO_PROBE, Stage.FIND_SCAVENGER, "$gaProbe_scavengerTookIt");
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, "$gaProbe_canReturn");
		connectWithGlobalFlag(Stage.RETURN_TO_ACADEMY, Stage.COMPLETED, "$gaProbe_finished");

		if (WITH_TIME_LIMIT) {
			setTimeLimit(Stage.FAILED, MISSION_DAYS, system, Stage.RETURN_TO_ACADEMY);
		}
		//setCreditReward(30000, 40000);
		setCreditReward(CreditReward.AVERAGE);
		setDefaultGARepRewards();
		if (variation == Variation.IT_BLEW_UP) {
			hazardPay = getCreditsReward() / 2;
			spawnDebrisField(DEBRIS_SMALL, DEBRIS_DENSE, new LocData(probe, false));
			spawnShipGraveyard(Factions.LUDDIC_PATH, 3, 5, new LocData(probe, false));

			if (rollProbability(PROB_PATHERS)) {
				beginWithinHyperspaceRangeTrigger(object, 1f, false, Stage.GO_TO_PROBE);
				triggerCreateFleet(FleetSize.TINY, FleetQuality.VERY_LOW, Factions.LUDDIC_PATH, FleetTypes.PATROL_SMALL, object);
				triggerSetFleetSizeFraction(0.03f);
				triggerMakeNonHostile();
				triggerMakeLowRepImpact();
				triggerFleetNoJump();
				triggerFleetPatherNoDefaultTithe();
				triggerSetFleetMissionRef("$gaProbe_ref");
				triggerFleetSetPatrolActionText("waiting"); // a bit dark, maybe?
				triggerOrderFleetPatrol(probe);
				triggerOrderFleetInterceptPlayer();
				triggerPickLocationAroundEntity(probe, 1000f);
				triggerSpawnFleetAtPickedLocation("$gaProbe_patherPermanentFlag", null);
				triggerFleetMakeImportant("$gaProbe_patherMissionInProgress", Stage.GO_TO_PROBE);
				endTrigger();
			}
		}

		if (variation == Variation.SCAVENGER) {
			beginWithinHyperspaceRangeTrigger(object, 1f, false, Stage.GO_TO_PROBE);
			triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.LOWER, Factions.SCAVENGERS, FleetTypes.SCAVENGER_MEDIUM, system);
			triggerAutoAdjustFleetStrengthMajor();
			triggerSetFleetFaction(Factions.INDEPENDENT);

			triggerMakeLowRepImpact();
			triggerFleetSetAvoidPlayerSlowly();
			triggerMakeFleetIgnoredByOtherFleets();

			triggerPickLocationAtClosestToEntityJumpPoint(system, probe);
			triggerSetEntityToPickedJumpPoint();
			triggerPickLocationAroundEntity(5000);

			// so the flag is there prior to the FIND_SCAVENGER stage
			triggerSpawnFleetAtPickedLocation("$gaProbe_scavenger", null);

			triggerFleetSetTravelActionText("exploring system");
			triggerFleetSetPatrolActionText("preparing to leave system");
			triggerOrderFleetPatrolEntity(false);

			triggerFleetMakeImportant("$gaProbe_scavenger", Stage.FIND_SCAVENGER);
			triggerFleetAddDefeatTrigger("gaProbeScavengerDefeated");
			endTrigger();
		}

		setMapMarkerNameColorBasedOnStar(system);

		return true;
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (action.equals("updateReward")) {
			setCreditReward(getCreditsReward() + hazardPay);
			return true;
		}
		return false;
	}


	protected void updateInteractionDataImpl() {
		set("$gaProbe_department", department);
		if (getCurrentStage() != null) {
			set("$gaProbe_stage", ((Enum)getCurrentStage()).name());
		}
		set("$gaProbe_celestialObjectNameWithType", getObjectNameWithType());
		set("$gaProbe_starName", system.getNameWithNoType());
		set("$gaProbe_systemName", system.getNameWithLowercaseTypeShort());
		set("$gaProbe_dist", getDistanceLY(object));
		set("$gaProbe_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaProbe_hazardPay", Misc.getWithDGS(hazardPay));
		set("$gaProbe_variation", variation);
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_PROBE) {
			info.addPara("Recover an instrument package from a probe in the " +
						system.getNameWithLowercaseTypeShort() + ".", opad);
		} else if (currentStage == Stage.FIND_SCAVENGER) {
			info.addPara("Go to the nearest jump-point and retrieve the instrument package " + 
					" from scavengers before they leave the system.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return the instrument package to the Galatia Academy and talk to " + 
					getPerson().getNameString() + " to receive your reward.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_PROBE) {
			if (system.isCurrentLocation()) {
				info.addPara("Recover the instrument package from the probe", tc, pad);
			} else {
				info.addPara(getGoToSystemTextShort(system), tc, pad);
			}
			return true;
		} else if (currentStage == Stage.FIND_SCAVENGER) {
			info.addPara("Recover the instrument package from scavengers at one of the jump-points",
					tc, pad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and talk to " + getPerson().getNameString(), tc, pad);
			return true;
		}
		return false;
	}
	
	
	protected String getObjectNameWithType() {
		if (hasSpecialName(object)) {
			return getTerrainTypeAOrAn(object) + " " + getTerrainType(object) + " called " + getTerrainName(object);
		} else {
			return getTerrainTypeAOrAn(object) + " " + getTerrainType(object);
		}
	}
	
	@Override
	public String getBaseName() {
		return "Recover Instrument Package";
	}
	
}


