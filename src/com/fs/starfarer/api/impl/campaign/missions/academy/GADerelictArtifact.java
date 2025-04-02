package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GADerelictArtifact extends GABaseMission {

	public static float MISSION_DAYS = 120f;
	public static float PROB_PIRATES = 0.33f;
	public static float PROB_SCAVENGER = 0.5f;
	
	
	public static enum Stage {
		GO_TO_DERELCIT,
		GET_IT_FROM_PIRATES,
		GET_IT_FROM_SCAVENGER,
		RETURN_TO_ACADEMY,
		COMPLETED,
		FAILED,
	}
	
	public static enum Variation {
		BASIC,
		REMNANTS,
		PIRATES,
		SCAVENGER,
	}
	
	protected StarSystemAPI system;
	protected SectorEntityToken entity;
	protected StarSystemAPI pirateSystem;
	protected String widget;
	protected Variation variation;
	protected int piratePayment;
	protected int piratePaymentLow;
	protected int scavPayment;
	protected int scavPaymentHigh;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaDA_ref")) {
			return false;
		}
		
		pickDepartment(GADepartments.INDUSTRIAL, GADepartments.SOCIAL, 
					   GADepartments.MILITARY, GADepartments.SCIENCE, GADepartments.WEIRD);
		widget = pickOne(	"unique superconducting structural element",
							"an advanced field conversion prototype",
							"a transparametric integrator",
							"an extremely rare material sample",
							"an uncommon power system component",
							"a Domain-era forge component",
							"a small-scale momentum decoupling device",
						 	"a fascinating low-power field projector");
		
		requireSystemTags(ReqMode.ANY, Tags.THEME_REMNANT_RESURGENT, Tags.THEME_REMNANT_SUPPRESSED,
									   Tags.THEME_DERELICT, Tags.THEME_MISC, Tags.THEME_RUINS);
		//requireSystemTags(ReqMode.ANY, Tags.THEME_REMNANT_RESURGENT, Tags.THEME_REMNANT_SUPPRESSED);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_REMNANT_SECONDARY); // remove not-very-dangerous remnant systems
		preferSystemUnexplored();
		preferSystemInDirectionOfOtherMissions();

		system = pickSystem();
		if (system == null) return false;
		
		entity = spawnDerelictOfType(null, new LocData(EntityLocationType.HIDDEN, null, system, false));
		if (entity == null) return false;
		
		setStartingStage(Stage.GO_TO_DERELCIT);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		makeImportant(entity, "$gaDA_entity", Stage.GO_TO_DERELCIT);
		makeImportant(getPerson(), "$gaDA_returnHere", Stage.RETURN_TO_ACADEMY);
		
		connectWithGlobalFlag(Stage.GO_TO_DERELCIT, Stage.GET_IT_FROM_PIRATES, "$gaDA_piratesTookIt");
		connectWithGlobalFlag(Stage.GO_TO_DERELCIT, Stage.GET_IT_FROM_SCAVENGER, "$gaDA_scavengerTookIt");
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, "$gaDA_gotWidget");
		connectWithGlobalFlag(Stage.RETURN_TO_ACADEMY, Stage.COMPLETED, "$gaDA_returnedWidget");
		
		if (WITH_TIME_LIMIT) {
			setTimeLimit(Stage.FAILED, MISSION_DAYS, entity.getStarSystem(), Stage.RETURN_TO_ACADEMY);
		}
		//setCreditReward(30000, 40000);
		setCreditReward(CreditReward.AVERAGE);
		setDefaultGARepRewards();
		
		if (system.hasTag(Tags.THEME_REMNANT)) {
			variation = Variation.REMNANTS;
			//setCreditReward(50000, 60000);
			setCreditReward(CreditReward.HIGH);
		} else {
			if (rollProbability(PROB_PIRATES)) {
				variation = Variation.PIRATES;
			} else if (rollProbability(PROB_SCAVENGER)) {
				variation = Variation.SCAVENGER;
			} else {
				variation = Variation.BASIC;
			}
		}
		
		setMapMarkerNameColorBasedOnStar(system);
		
//		variation = Variation.BASIC;
//		variation = Variation.PIRATES;
//		variation = Variation.SCAVENGER;
		
		if (variation == Variation.PIRATES) {
			requireSystemTags(ReqMode.ANY, Tags.THEME_MISC_SKIP, Tags.THEME_DERELICT, Tags.THEME_MISC, Tags.THEME_RUINS);
			requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE);
			requireSystemNot(system);
			requireSystemWithinRangeOf(entity.getLocationInHyperspace(), 10);
			pirateSystem = pickSystem();
			if (pirateSystem == null) return false;

			piratePayment = genRoundNumber(10000, 15000);
			piratePaymentLow = genRoundNumber(2000, 5000);
			
			beginStageTrigger(Stage.GET_IT_FROM_PIRATES);
			triggerSpawnEntity(Entities.SUPPLY_CACHE, new LocData(EntityLocationType.HIDDEN, null, pirateSystem, false));
			triggerSaveGlobalEntityRef("$gaDA_cache");
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, pirateSystem);
			triggerAutoAdjustFleetStrengthMajor();
			triggerSetStandardHostilePirateFlags();
			triggerMakeFleetIgnoredByOtherFleets();
			triggerPickLocationAtInSystemJumpPoint(pirateSystem);
			triggerSpawnFleetAtPickedLocation();
			triggerOrderFleetPatrolEntity(true);
			triggerFleetMakeImportant("$gaDA_pirate", Stage.GET_IT_FROM_PIRATES);
			triggerFleetAddDefeatTrigger("gaDAFleetWithWidgetDefeated");
			endTrigger();
		} else if (variation == Variation.SCAVENGER) {
			scavPayment = genRoundNumber(10000, 15000);
			scavPaymentHigh = genRoundNumber(20000, 25000);
			
			beginStageTrigger(Stage.GET_IT_FROM_SCAVENGER);
			triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.LOWER, Factions.SCAVENGERS, FleetTypes.SCAVENGER_MEDIUM, system);
			triggerAutoAdjustFleetStrengthModerate();
			triggerSetFleetFaction(Factions.INDEPENDENT);
			
//			triggerFleetSetSingleShipOnly();
//			triggerFleetSetAllWeapons();
//			triggerFleetSetFlagship("radiant_Standard");
//			triggerFleetSetOfficers(OfficerNum.FC_ONLY, OfficerQuality.AI_ALPHA);
			
			triggerMakeLowRepImpact();
			
			triggerPickLocationAtClosestToPlayerJumpPoint(system);
			triggerSetEntityToPickedJumpPoint();
			triggerPickLocationAroundEntity(5000);
			triggerSpawnFleetAtPickedLocation();
			
			triggerFleetSetTravelActionText("exploring system");
			triggerFleetSetPatrolActionText("preparing to leave system");
			triggerOrderFleetPatrolEntity(false);
			
			triggerFleetMakeImportant("$gaDA_scavenger", Stage.GET_IT_FROM_SCAVENGER);
			triggerFleetAddDefeatTrigger("gaDAFleetWithWidgetDefeated");
			endTrigger();
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaDA_department", department);
		set("$gaDA_widget", widget);
		set("$gaDA_widgetNoArticle", getWidgetWithoutArticle());
		set("$gaDA_starName", entity.getStarSystem().getNameWithNoType());
		set("$gaDA_systemName", entity.getStarSystem().getNameWithLowercaseTypeShort());
		set("$gaDA_dist", getDistanceLY(entity));
		set("$gaDA_fuel", getFuel(entity, true));
		set("$gaDA_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaDA_piratePayment", Misc.getWithDGS(piratePayment));
		set("$gaDA_piratePaymentLow", Misc.getWithDGS(piratePaymentLow));
		set("$gaDA_scavPayment", Misc.getWithDGS(scavPayment));
		set("$gaDA_scavPaymentHigh", Misc.getWithDGS(scavPaymentHigh));
		
		set("$gaDA_notRemnants", variation != Variation.REMNANTS);
		set("$gaDA_variation", variation);
	}
	
	protected String getWidgetWithoutArticle() {
		if (widget.startsWith("a ")) {
			return widget.replaceFirst("a ", "");
		}
		if (widget.startsWith("an ")) {
			return widget.replaceFirst("an ", "");
		}
		return widget;
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_DERELCIT) {
			String extra = "";
			if (variation == Variation.REMNANTS) extra = " 'Autonomous weapon systems' may pose a danger.";
			info.addPara("Retrieve " + widget + " from a derelict ship " + getLocated(entity) + "." + extra, opad);
		} else if (currentStage == Stage.GET_IT_FROM_PIRATES) {
			SectorEntityToken cache = getEntityFromGlobal("$gaDA_cache");
			info.addPara("Retrieve the " + getWidgetWithoutArticle() + 
					     " from the pirates as they resupply from a cache " + getLocated(cache) + ".", opad);			
		} else if (currentStage == Stage.GET_IT_FROM_SCAVENGER) {
			info.addPara("Go to the nearest jump-point and retrieve the " + getWidgetWithoutArticle() + 
					" from scavengers before they leave the system.", opad);			
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return the " + getWidgetWithoutArticle() + " to the Galatia Academy and talk to " + 
					getPerson().getNameString() + " to receive your reward.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_DERELCIT) {
			if (system.isCurrentLocation()) {
				info.addPara("Retrieve the " + getWidgetWithoutArticle() + " from a derelict ship", tc, pad);
			} else {
				info.addPara(getGoToSystemTextShort(system), tc, pad);
			}
			return true;
		} else if (currentStage == Stage.GET_IT_FROM_PIRATES) {
			info.addPara("Retrieve the " + getWidgetWithoutArticle() + " from pirates in the " +
						 pirateSystem.getNameWithLowercaseType(), tc, pad);
		} else if (currentStage == Stage.GET_IT_FROM_SCAVENGER) {
			info.addPara("Retrieve the " + getWidgetWithoutArticle() + " from scavengers at the nearest jump-point",
						tc, pad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and talk to " + getPerson().getNameString(), tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Derelict Artifact Recovery";
	}
	
}


