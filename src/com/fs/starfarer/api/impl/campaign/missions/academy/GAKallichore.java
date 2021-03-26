package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GAKallichore extends GABaseMission {

	public static enum Stage {
		TALK_TO_ARROYO,
		VISIT_ARROYO,
		TALK_TO_GARGOYLE,
		GET_HACK_HARDWARE,
		INSTALL_HACK,
		RETRIEVE_ARCHIVE,
		RETURN_TO_ACADEMY,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI arroyo;
	protected PersonAPI gargoyle;
	protected PersonAPI loke;
	protected SectorEntityToken relay;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$gaKA_ref", "$gaKA_inProgress")) {
			return false;
		}
		
		setName("The Kallichore Archive");
		
		baird = getImportantPerson(People.BAIRD);
		arroyo = getImportantPerson(People.ARROYO);
		gargoyle = getImportantPerson(People.GARGOYLE);
		loke = getImportantPerson(People.CLONE_LOKE);
		if (baird == null) return false;
		if (arroyo == null) return false;
		if (gargoyle == null) return false;
		if (loke == null) return false;
		
		// if the player destroyed Aztlan Relay before this point, uhh. Need to do something, probably.
		relay = Global.getSector().getEntityById("aztlan_relay");
		if (relay == null) return false;
		
		StarSystemAPI mayasura = gargoyle.getMarket().getStarSystem();
		StarSystemAPI galatia = baird.getMarket().getStarSystem();
		if (mayasura == null || galatia == null) return false;
		
		MarketAPI coatl = Global.getSector().getEconomy().getMarket("coatl");
		if (coatl == null) return false;
		
		setStoryMission();
		
		setStartingStage(Stage.TALK_TO_ARROYO);
		addSuccessStages(Stage.COMPLETED);
		
		makeImportant(arroyo, "$gaKA_contact", Stage.TALK_TO_ARROYO);
		makeImportant(arroyo.getMarket(), "$gaKA_visitChalet", Stage.VISIT_ARROYO);
		makeImportant(gargoyle, "$gaKA_contact", Stage.TALK_TO_GARGOYLE);
		makeImportant(loke.getMarket(), "$gaKA_getHack", Stage.GET_HACK_HARDWARE);
		makeImportant(relay, "$gaKA_installHack", Stage.INSTALL_HACK);
		makeImportant(gargoyle, "$gaKA_contactRetrieve", Stage.RETRIEVE_ARCHIVE);
		makeImportant(baird.getMarket(), "$gaKA_returnHere", Stage.RETURN_TO_ACADEMY);
		
		setStageOnGlobalFlag(Stage.VISIT_ARROYO, "$gaKA_visitArroyo");
		setStageOnGlobalFlag(Stage.TALK_TO_GARGOYLE, "$gaKA_talkToGargoyle");
		setStageOnGlobalFlag(Stage.GET_HACK_HARDWARE, "$gaKA_getHackHardware");
		setStageOnGlobalFlag(Stage.INSTALL_HACK, "$gaKA_installHack");
		setStageOnGlobalFlag(Stage.RETRIEVE_ARCHIVE, "$gaKA_retrieveArchive");
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, "$gaKA_returnToAcademy");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaKA_completed");
		
		
		beginStageTrigger(Stage.TALK_TO_ARROYO);
		triggerUnhideCommListing(arroyo);
		endTrigger();
		beginStageTrigger(Stage.TALK_TO_GARGOYLE);
		triggerUnhideCommListing(gargoyle);
		endTrigger();
		
		beginStageTrigger(Stage.RETURN_TO_ACADEMY);
		triggerHideCommListing(gargoyle);
		endTrigger();
		
		
		beginStageTrigger(Stage.RETURN_TO_ACADEMY);
		SectorEntityToken fleetLocation = gargoyle.getMarket().getPrimaryEntity();
		triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.DEFAULT, 
								Factions.HEGEMONY, FleetTypes.TASK_FORCE, fleetLocation);
		triggerAutoAdjustFleetStrengthModerate();
		triggerMakeAllFleetFlagsPermanent();
		triggerMakeFleetIgnoreOtherFleets();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerSetPatrol();
		triggerPickLocationAtClosestToEntityJumpPoint(mayasura, fleetLocation, 4000f);
		triggerPickLocationTowardsEntity(null, 30f, 5000f); // towards the jump-point we just picked
		triggerSpawnFleetAtPickedLocation(null, null);
		triggerFleetSetPatrolActionText("standing off with Port Tse Station");
		triggerFleetSetPatrolLeashRange(100f);
		triggerOrderFleetPatrol(fleetLocation);
		triggerOrderFleetEBurn(1f);
		triggerIncreaseMarketHostileTimeout(gargoyle.getMarket(), 90f);
		endTrigger();
	
		beginWithinHyperspaceRangeTrigger(gargoyle.getMarket(), 1f, true, Stage.RETURN_TO_ACADEMY);
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_1, 
						Factions.HEGEMONY, FleetTypes.TASK_FORCE, mayasura);
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerMakeHostileAndAggressive();
		//triggerMakeNoRepImpact(); // this happens in dialog instead
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		triggerSetPatrol();
		triggerPickLocationTowardsEntity(galatia.getHyperspaceAnchor(), 90f, getUnits(0.5f));
		triggerSpawnFleetAtPickedLocation("$gaKA_patrol", null);
		triggerOrderFleetInterceptPlayer();
		triggerOrderFleetEBurn(1f);
		triggerFleetMakeImportant(null, Stage.RETURN_TO_ACADEMY);
		endTrigger();

		triggerCreateMediumPatrol(coatl, Factions.HEGEMONY, relay, Stage.INSTALL_HACK, 0f);
		
		beginStageTrigger(Stage.COMPLETED);
		triggerRemoveTags(relay, Tags.STORY_CRITICAL);
		triggerMakeNonStoryCritical(gargoyle.getMarket(), arroyo.getMarket(), loke.getMarket());
		triggerMovePersonToMarket(gargoyle, baird.getMarket(), true);
		triggerUnhideCommListing(gargoyle);
		endTrigger();
		
		
		// post-hack (and probably post-mission) consequences
		float baseDelay = 90f;
		//baseDelay = 0f;
		beginStageTrigger(Stage.RETRIEVE_ARCHIVE);
		triggerSetGlobalMemoryValueAfterDelay(genDelay(baseDelay), "$gaKA_triTachyonVisit", true);
		triggerSetGlobalMemoryValueAfterDelay(genDelay(baseDelay), "$gaKA_hegemonyVisit", true);
		endTrigger();
		
		beginStageTrigger(Stage.COMPLETED);
		//triggerSetGlobalMemoryValueAfterDelay(genDelay(2f), "$gaKA_missionCompleted", true);
		triggerSetGlobalMemoryValue("$gaKA_missionCompleted", true);
		endTrigger();
		
		
//		//debug trigger to unhide comm entries and set mission stage to retrieving data
//		beginStageTrigger(Stage.TALK_TO_ARROYO);
//		triggerUnhideCommListing(gargoyle);
//		triggerSetGlobalMemoryValue("$gaKA_retrieveArchive", true);
//		endTrigger();
//		// end debug trigger
		
		
		return true;
		
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaKA_stage", getCurrentStage());
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		FactionAPI heg = Global.getSector().getFaction(Factions.HEGEMONY);
		if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("You've recovered the data archive of the Academy's previous Provost, Tomas Kallichore, " + 
					"which contains the results of failed gate activation experiments.", opad);
		} else {
			info.addPara("Recover the data archive of the Academy's previous Provost, Tomas Kallichore, " + 
					"which contains the results of failed gate activation experiments.", opad);
			info.addPara("The archive is " +
					"currently in the ultra-secure data vaults of the Hegemony Ministry of Technology Standards.", opad, 
					heg.getBaseUIColor(), "Hegemony");
		}
		
		if (currentStage == Stage.TALK_TO_ARROYO) {
			info.addPara(getGoTalkToPersonText(arroyo) + ". He has a relationship with Provost Baird that "
					+ "can be leveraged to compel his cooperation.", opad);
		} else if (currentStage == Stage.VISIT_ARROYO) {
			info.addPara("Visit " + arroyo.getNameString() + " in person, at his planetside chalet.", opad);
		} else if (currentStage == Stage.TALK_TO_GARGOYLE) {
			info.addPara(getGoTalkToPersonText(gargoyle) + ", a hacker, to arrange a break in to the data vaults. " +
					"Arroyo is sure a job \"as stupid as you're proposing\" " +
					"will prove irresistible.", opad);
		} else if (currentStage == Stage.GET_HACK_HARDWARE) {
			info.addPara(getGoToMarketText(loke.getMarket()) + " and pick up the hack device from a " +
												"storage unit.", opad);
		} else if (currentStage == Stage.INSTALL_HACK) {
			info.addPara(getGoToSystemTextShort(relay.getStarSystem()) + " and install the hack transmitter " +
					"on " + relay.getName() + ".", opad);
		} else if (currentStage == Stage.RETRIEVE_ARCHIVE) {
			info.addPara(getGoToMarketText(gargoyle.getMarket()) + 
						" and retrieve Kallichore's archive from Gargoyle.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and talk to Provost Baird.", opad);			
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_ARROYO) {
			info.addPara(getGoTalkToPersonText(arroyo), tc, pad);
			return true;
		} else if (currentStage == Stage.VISIT_ARROYO) {
			info.addPara("Visit " + arroyo.getNameString() + " in person, at his planetside chalet", tc, pad);
			return true;
		} else if (currentStage == Stage.TALK_TO_GARGOYLE) {
			info.addPara(getGoTalkToPersonText(gargoyle), tc, pad);
			return true;
		} else if (currentStage == Stage.GET_HACK_HARDWARE) {
			info.addPara(getGoToMarketText(loke.getMarket()) + " and pick up the hack device", tc, pad);
			return true;
		} else if (currentStage == Stage.INSTALL_HACK) {
			info.addPara(getGoToSystemTextShort(relay.getStarSystem()) + " and install hack transmitter " +
					"on " + relay.getName() + "", tc, pad);
			return true;
		} else if (currentStage == Stage.RETRIEVE_ARCHIVE) {
			info.addPara(getGoToMarketText(gargoyle.getMarket()) + 
						" and retrieve Kallichore's archive", tc, pad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and talk to Provost Baird", tc, pad);			
		}
		return false;
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	
}





