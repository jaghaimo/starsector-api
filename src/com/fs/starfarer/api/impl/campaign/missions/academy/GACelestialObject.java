package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GACelestialObject extends GABaseMission {

	public static float MISSION_DAYS = 120f;
	public static float PROB_HOLOARTS = 0.33f;
	public static float PROB_PATHERS = 0.5f;
	public static float PROB_SLIPUP = 0.33f;
	
	public static float PROB_PULSAR = 0.33f;
	public static float PROB_BLACK_HOLE = 0.5f;
	
	public static float PROB_NOT_ASTEROIDS = 0.5f;
	
	
	public static enum Stage {
		GO_TO_OBJECT,
		COMPLETED,
		FAILED,
	}
	
	public static enum Variation {
		BASIC,
		PULSAR,
		BLACK_HOLE,
	}
	
	protected StarSystemAPI system;
	protected Variation variation;
	protected CampaignTerrainAPI object;
	protected boolean holoarts = false;
	protected boolean slipUp = false;
	protected int patherTithe = 0;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaCO_ref")) {
			return false;
		}
		
		pickDepartmentAllTags(GADepartments.SCIENCE, GADepartments.WEIRD);
		if (rollProbability(PROB_HOLOARTS)) {
			department = "Holoart Studies";
			holoarts = true;
		}

		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE);
		requireTerrainType(ReqMode.ANY, Terrain.ASTEROID_BELT, Terrain.ASTEROID_FIELD, Terrain.DEBRIS_FIELD, 
						   Terrain.MAGNETIC_FIELD, Terrain.NEBULA, Terrain.RING);
		if (rollProbability(PROB_NOT_ASTEROIDS)) {
			preferTerrainType(ReqMode.NOT_ANY, Terrain.ASTEROID_BELT, Terrain.ASTEROID_FIELD);
		}

		if (!holoarts) {
			if (rollProbability(PROB_PULSAR)) {
				preferSystemHasPulsar();
			} else if (rollProbability(PROB_BLACK_HOLE)) {
				preferSystemBlackHole();
			}
		} else {
			preferSystemNotPulsar();
			preferSystemNotBlackHole();
		}
		
		// important to put these last - requirements that come earlier have higher priority
		//preferTerrainHasSpecialName();
		preferSystemUnexplored();
		preferTerrainInDirectionOfOtherMissions();
		
		object = pickTerrain();
		
		if (object == null) {
			return false;
		}
		
		system = object.getStarSystem();
		
		setStartingStage(Stage.GO_TO_OBJECT);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		//makeImportant(object, "$gaCO_object", Stage.GO_TO_OBJECT);
		SectorEntityToken node = spawnMissionNode(new LocData(object));
		makeImportant(node, "$gaCO_object", Stage.GO_TO_OBJECT);
		
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaCO_scanCompleted");
		setStageOnGlobalFlag(Stage.FAILED, "$gaCO_gaveScannerToPathers");
		
		if (WITH_TIME_LIMIT) {
			setTimeLimit(Stage.FAILED, MISSION_DAYS, system);
		}
		//setCreditReward(30000, 40000);
		setCreditReward(CreditReward.AVERAGE);
		setDefaultGARepRewards();
		
		if (system.hasPulsar()) {
			variation = Variation.PULSAR;
		} else if (system.hasBlackHole()) {
			variation = Variation.BLACK_HOLE;
		} else {
			variation = Variation.BASIC;
		}
		
		slipUp = rollProbability(PROB_SLIPUP);

		if (rollProbability(PROB_PATHERS)) {
			patherTithe = genRoundNumber(10000, 15000);
			
			beginWithinHyperspaceRangeTrigger(object, 1f, false, Stage.GO_TO_OBJECT);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.HIGHER, Factions.LUDDIC_PATH, FleetTypes.PATROL_MEDIUM, object);
			triggerFleetPatherNoDefaultTithe();
			triggerAutoAdjustFleetStrengthModerate();
			triggerMakeAllFleetFlagsPermanent();
			triggerSetStandardAggroPirateFlags();
			triggerPickLocationAroundEntity(node, 1000f);
			triggerSpawnFleetAtPickedLocation("$gaCO_patherPermanentFlag", null);
			triggerFleetAllowLongPursuit();
			triggerOrderFleetPatrol(node);
			// so that the tithe value is available if the interaction with the fleet is after the mission ends
			triggerSetFleetMemoryValue("$gaCO_patherTithe", Misc.getWithDGS(patherTithe));
			triggerFleetMakeImportant("$gaCO_patherMissionInProgress", Stage.GO_TO_OBJECT);
			endTrigger();
			
		}
		
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaCO_department", department);
		set("$gaCO_holoarts", holoarts);
		set("$gaCO_slipUp", slipUp);
		set("$gaCO_objectAOrAn", getTerrainNameAOrAn(object));
		set("$gaCO_celestialObjectNameWithTypeShort", getObjectNameWithTypeShort());
		set("$gaCO_celestialObjectNameWithTypeLong", getObjectNameWithTypeLong());
		set("$gaCO_celestialObjectName", getTerrainName(object));
		set("$gaCO_celestialObjectType", getTerrainType(object));
		set("$gaCO_starName", system.getNameWithNoType());
		set("$gaCO_systemName", system.getNameWithLowercaseTypeShort());
		set("$gaCO_dist", getDistanceLY(object));
		set("$gaCO_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaCO_variation", variation);
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OBJECT) {
			String extra = "";
			if (variation == Variation.BLACK_HOLE) extra = " Avoid the black hole located in-system.";
			else if (variation == Variation.PULSAR) extra = " Avoid the beam of the in-system pulsar.";
			info.addPara("Run a scan package on " + getObjectNameWithTypeShort() +
						" located in the " + system.getNameWithLowercaseTypeShort() + "." + extra, opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OBJECT) {
			info.addPara("Scan " + getTerrainNameAOrAn(object)  + " " + getTerrainType(object) +
						 " in the " + system.getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		return false;
	}
	
	protected String getObjectNameWithTypeLong() {
		if (hasSpecialName(object)) {
			return getTerrainName(object) + " - that's " + getTerrainNameAOrAn(object) + " " + getTerrainType(object);
		} else {
			return getTerrainNameAOrAn(object) + " " + getTerrainType(object);
		}
	}
	protected String getObjectNameWithTypeShort() {
		if (hasSpecialName(object)) {
			return getTerrainName(object) + ", " + getTerrainNameAOrAn(object) + " " + getTerrainType(object);
		} else {
			return getTerrainNameAOrAn(object) + " " + getTerrainType(object);
		}
	}
	
	@Override
	public String getBaseName() {
		return "Scan Celestial Object";
	}
	
}


