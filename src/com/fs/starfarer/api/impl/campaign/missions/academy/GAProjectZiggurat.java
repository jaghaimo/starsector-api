package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.world.TTBlackSite;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GAProjectZiggurat extends GABaseMission { //implements ShipRecoveryListener {

	public static enum Stage {
		GET_KELISE_LEAD,
		SELL_BLACKMAIL_MATERIAL,
		SOLD_BLACKMAIL_MATERIAL,
		TALK_TO_CALLISTO,
		GO_TO_RELAY_SYSTEM,
		GO_TO_NASCENT_WELL,
		INVESTIGATE_SITE,
		//RECOVER_ZIGGURAT,
		RETURN_TO_ACADEMY,
		COMPLETED,
	}
	
	//public static String RECOVERED_ZIGGURAT = "$gaPZ_recoveredZiggurat";
	public static String SCANNED_ZIGGURAT = "$gaPZ_scannedZiggurat";
	
	protected PersonAPI callisto;
	protected MarketAPI culann;
	protected MarketAPI donn;
	protected PersonAPI baird;
	protected PersonAPI arroyo;
	protected PersonAPI gargoyle;
	protected PersonAPI culannAdmin;
	protected NascentGravityWellAPI well;
	protected StarSystemAPI relaySystem; 
	protected StarSystemAPI alphaSite;
	protected CampaignFleetAPI zigFleet;
	protected SectorEntityToken relay;
	protected PlanetAPI baseRuins;
	
	protected boolean pointAtArroyo = false;
	protected boolean pointAtCulannAdmin = false;
	protected int culannBribe;
	protected int paymentForCommFakes;
	protected int paymentForCommFakesHigh;
	protected int rkTithe;

	private SectorEntityToken hamatsu;
	
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$gaPZ_ref", "$gaPZ_inProgress")) {
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		
		callisto = getImportantPerson(People.IBRAHIM);
		if (callisto == null) return false;
		
		gargoyle = getImportantPerson(People.GARGOYLE);
		if (gargoyle == null) return false;
		
		culann = Global.getSector().getEconomy().getMarket("culann");
		if (culann == null) return false;
		
		donn = Global.getSector().getEconomy().getMarket("donn");
		if (donn == null) return false;
		
		arroyo = getImportantPerson(People.ARROYO);
		if (arroyo == null) return false;
		
		culannAdmin = getPersonAtMarketPost(culann, Ranks.POST_ADMINISTRATOR);
		if (culannAdmin == null) return false;
		
		well = (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(TTBlackSite.NASCENT_WELL_KEY);
		if (well == null || !well.isAlive()) return false;
		
		float dir = Misc.getAngleInDegrees(culann.getLocationInHyperspace(), well.getLocationInHyperspace());
		
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
		preferSystemInDirectionFrom(culann.getLocationInHyperspace(), dir, 30f);
		preferSystemWithinRangeOf(culann.getLocationInHyperspace(), 15f, 30f);
		preferSystemWithinRangeOf(culann.getLocationInHyperspace(), 15f, 40f);
		preferSystemUnexplored();
		preferSystemNotPulsar();
		requirePlanetNotGasGiant();
		requirePlanetNotStar();
		preferPlanetUnsurveyed();
		baseRuins = pickPlanet(true);
		if (baseRuins == null) return false;
		
		relaySystem = baseRuins.getStarSystem();
		if (relaySystem == null) return false;
		
		relay = spawnEntity(Entities.GENERIC_PROBE, new LocData(EntityLocationType.HIDDEN, null, relaySystem));
		if (relay == null) return false;
		relay.setCustomDescriptionId("gaPZ_relay");
		
		
		alphaSite = (StarSystemAPI) well.getTarget().getContainingLocation();
		
		for (CampaignFleetAPI fleet : alphaSite.getFleets()) {
			if (fleet.getMemoryWithoutUpdate().getBoolean("$ziggurat")) {
				zigFleet = fleet;
				break;
			}
		}
		if (zigFleet == null) return false;
		
		requireSystemIs(alphaSite);
		requireEntityMemoryFlags("$hamatsu");
		// hamatsu could be null if player salvaged it after dipping into alpha site then backing out
		hamatsu = pickEntity();

		
		paymentForCommFakes = genRoundNumber(10000, 15000);
		paymentForCommFakesHigh = genRoundNumber(40000, 60000);
		culannBribe = genRoundNumber(20000, 30000);
		rkTithe = genRoundNumber(80000, 120000);
		
		setStartingStage(Stage.GET_KELISE_LEAD);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		connectWithGlobalFlag(Stage.GET_KELISE_LEAD, Stage.SELL_BLACKMAIL_MATERIAL, "$gaPZ_sellBlackmail");
		connectWithGlobalFlag(Stage.SELL_BLACKMAIL_MATERIAL, Stage.SOLD_BLACKMAIL_MATERIAL, "$gaPZ_soldBlackmail");
		connectWithGlobalFlag(Stage.GET_KELISE_LEAD, Stage.TALK_TO_CALLISTO, "$gaPZ_talkToCallisto");
		connectWithGlobalFlag(Stage.SOLD_BLACKMAIL_MATERIAL, Stage.TALK_TO_CALLISTO, "$gaPZ_talkToCallisto");
		connectWithGlobalFlag(Stage.TALK_TO_CALLISTO, Stage.GO_TO_RELAY_SYSTEM, "$gaPZ_goToRelaySystem");
		connectWithGlobalFlag(Stage.GO_TO_RELAY_SYSTEM, Stage.GO_TO_NASCENT_WELL, "$gaPZ_goToWell");
		setStageOnEnteredLocation(Stage.INVESTIGATE_SITE, alphaSite);
		//setStageOnGlobalFlag(Stage.RECOVER_ZIGGURAT, "$gaPZ_recoverZig");
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, SCANNED_ZIGGURAT);
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaPZ_completed");
		
		//makeImportant(baird, null, Stage.TALK_TO_BAIRD);
		//setStageOnMemoryFlag(Stage.COMPLETED, baird.getMarket(), "$gaPZ_completed");
		
		makeImportant(culann, null, Stage.GET_KELISE_LEAD);
		makeImportant(donn, null, Stage.SELL_BLACKMAIL_MATERIAL);
		makeImportant(arroyo, null, Stage.SOLD_BLACKMAIL_MATERIAL);
		makeImportant(callisto, null, Stage.TALK_TO_CALLISTO);
		makeImportant(relay, "$gaPZ_relayImportant", Stage.GO_TO_RELAY_SYSTEM);
		makeImportant(well, null, Stage.GO_TO_NASCENT_WELL);
		makeImportant(zigFleet, "$gaPZ_ziggurat", Stage.INVESTIGATE_SITE);
		makeImportant(baird, "$gaPZ_returnHere", Stage.RETURN_TO_ACADEMY);
		
		setFlag(relay, "$gaPZ_relay", false);
		setFlag(culannAdmin, "$gaPZ_culannAdmin", false, Stage.GET_KELISE_LEAD);
		setFlag(baseRuins, "$gaPZ_baseRuins", false, Stage.GO_TO_RELAY_SYSTEM, Stage.GO_TO_NASCENT_WELL);
		

		// Rogue Luddic Knight encounter as the player nears Arcadia
		beginWithinHyperspaceRangeTrigger(callisto.getMarket(), 3f, true, Stage.TALK_TO_CALLISTO);
		triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.HIGHER, Factions.LUDDIC_CHURCH, FleetTypes.PATROL_LARGE, culann.getLocationInHyperspace());
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerMakeHostileAndAggressive();
		triggerMakeLowRepImpact();
		triggerFleetMakeFaster(true, 2, true);
		triggerSetFleetAlwaysPursue();
		triggerPickLocationTowardsEntity(callisto.getMarket().getStarSystem().getHyperspaceAnchor(), 30f, getUnits(1.5f));
		triggerSpawnFleetAtPickedLocation("$gaPZ_rogueKnight", null);
		triggerOrderFleetInterceptPlayer();
		triggerOrderFleetEBurn(1f);
		triggerFleetMakeImportant(null, Stage.TALK_TO_CALLISTO);
		endTrigger();
		
		// TriTach merc, phase fleet
		beginEnteredLocationTrigger(relaySystem, Stage.GO_TO_RELAY_SYSTEM);
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_2, Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, culann.getLocationInHyperspace());
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerSetFleetFaction(Factions.TRITACHYON);
		triggerMakeHostileAndAggressive();
		triggerMakeLowRepImpact();
		triggerSetFleetDoctrineComp(0, 0, 5);
		triggerFleetMakeFaster(true, 1, true);
		triggerPickLocationAtInSystemJumpPoint(relaySystem);
		triggerSpawnFleetAtPickedLocation("$gaPZ_ttMerc", null);
		triggerOrderFleetInterceptPlayer();
		triggerFleetMakeImportant(null, Stage.GO_TO_RELAY_SYSTEM);
		endTrigger();
		
		
		beginStageTrigger(Stage.COMPLETED);
//		triggerSetGlobalMemoryValueAfterDelay(genDelay(2f), "$gaPZ_missionCompleted", true);
//		triggerSetGlobalMemoryValueAfterDelay(genDelay(2f), SCANNED_ZIGGURAT, true);
		triggerSetGlobalMemoryValue("$gaPZ_missionCompleted", true);
		triggerSetGlobalMemoryValue(SCANNED_ZIGGURAT, true);
		triggerMakeNonStoryCritical(culann, donn, callisto.getMarket(), arroyo.getMarket(), gargoyle.getMarket());
		endTrigger();
		
		return true;
	}
	
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								 Map<String, MemoryAPI> memoryMap) {

		if ("makeArroyoImportant".equals(action)) {
			if (!pointAtArroyo) {
				makeImportant(arroyo, null, Stage.GET_KELISE_LEAD);
				makePrimaryObjective(arroyo);
				makeUnimportant(culann);
				makeUnimportant(culannAdmin);
				pointAtArroyo = true;
			}
			return true;
		} else if ("makeCulannAdminImportant".equals(action)) {
			if (!pointAtCulannAdmin) {
				makeImportant(culannAdmin, null, Stage.GET_KELISE_LEAD);
				makePrimaryObjective(culannAdmin);
				makeUnimportant(arroyo);
				pointAtCulannAdmin = true;
			}
			return true;
		}
		return false;
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		
		Global.getSector().getMemoryWithoutUpdate().unset("$gaPZ_pointedToCulannAdmin");
	}

	protected void updateInteractionDataImpl() {
		set("$gaPZ_stage", getCurrentStage());
		set("$gaPZ_culannBribe", Misc.getWithDGS(culannBribe));
		set("$gaPZ_paymentForCommFakes", Misc.getWithDGS(paymentForCommFakes));
		set("$gaPZ_paymentForCommFakesHigh", Misc.getWithDGS(paymentForCommFakesHigh));
		set("$gaPZ_rkTithe", Misc.getWithDGS(rkTithe));
		set("$gaPZ_relaySystem", relaySystem.getNameWithNoType());
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GET_KELISE_LEAD || 
				currentStage == Stage.SELL_BLACKMAIL_MATERIAL ||
				currentStage == Stage.GO_TO_RELAY_SYSTEM ||
				currentStage == Stage.TALK_TO_CALLISTO ||
				currentStage == Stage.SOLD_BLACKMAIL_MATERIAL) {
			info.addPara("Get a lead on the whereabouts of Kelise Astraia, who is associated with "
					+ "a secret Tri-Tachyon research base called \"Alpha Site\".", opad);
		}
		if (currentStage == Stage.GET_KELISE_LEAD) {
			addStandardMarketDesc("She was formerly employed as a weapons engineer " + culann.getOnOrAt(),
					culann, info, opad);
			if (pointAtArroyo) {
				info.addPara("You've talked to Gargoyle, who advised you to talk to Rayan Arroyo, "
						+ "\"a Tri-Tach goon from top to bottom\", who is likely to have the right connections "
						+ "to help with the investigation.", opad);
				addStandardMarketDesc("Arroyo is located " + arroyo.getMarket().getOnOrAt(), arroyo.getMarket(), info, opad);
			}
			if (pointAtCulannAdmin) {
				info.addPara("You've learned that the administrator of Culann Starforge will have access to "
						+ "personnel records that may shed light on Kelise's whereabouts.", opad);
				addStandardMarketDesc("", culannAdmin.getMarket(), info, opad);
			}
		} else if (currentStage == Stage.SELL_BLACKMAIL_MATERIAL) {
			info.addPara("Rayan Arroyo has agreed to help locate her, in exchange for a favor - selling bad "
					+ "comm fakes to any reasonably highly placed pirate leader, which would serve "
					+ "his ends as part of a disinformation campaign.", opad);
			addStandardMarketDesc("A reasonable place to find such a pirate would be " + 
					donn.getOnOrAt(), donn, info, opad);
		} else if (currentStage == Stage.SOLD_BLACKMAIL_MATERIAL) {
			info.addPara("You've sold the blackmail materials to a pirate leader, in exchange for which Arroyo "
					+ "has agreed to help you find Kelise Astraia.", opad);
			info.addPara(getGoTalkToPersonText(arroyo) + ".", opad);
		} else if (currentStage == Stage.TALK_TO_CALLISTO) {
			info.addPara("You've learned that Kelise chartered - and paid for unknown special modifications to - "
					+ "the ISS Hamatsu, a Venture-class starship, before flying it out of the system. The ship's owner is one " + 
					callisto.getNameString() + ". She may have more information.", opad);
			info.addPara(getGoTalkToPersonText(callisto) + ".", opad);
		} else if (currentStage == Stage.GO_TO_RELAY_SYSTEM) {
			info.addPara("You've learned that the flight plan of the ISS Hamatsu - the ship chartered by Kelise Astraia - "
						+ "led to the " + relaySystem.getNameWithLowercaseTypeShort() + ".", opad);
		} else if (currentStage == Stage.GO_TO_NASCENT_WELL) {
			info.addPara("You've found a hidden relay in the " + relaySystem.getNameWithLowercaseTypeShort() + ", "
					+ "that was likely used for communications with \"Alpha Site\". " 
					+ "Investigate the hyperspace coordinates that the relay was transmitting to.", opad);
			
			if (well.isInCurrentLocation() && Misc.getDistanceToPlayerLY(well) < 0.2f) {
				info.addPara("Use %s to traverse the nascent gravity well located at the coordinates.",
						opad, Misc.getHighlightColor(), "Transverse Jump");
			}
			
		} else if (currentStage == Stage.INVESTIGATE_SITE) {
			info.addPara("Learn what Tri-Tachyon was doing at Alpha Site.", opad);
			info.addPara("Optional: look for the ISS Hamatsu and, if found, return it "
								+ "to " + callisto.getNameString() + ".", opad);
//			addStandardMarketDesc("Optional: look for the ISS Hamatsu and, if found, return it "
//					+ "to " + callisto.getNameString() + " on", callisto.getMarket(), info, opad);
			info.addPara("Optional: locate Kelise Astraia.", opad);
//		} else if (currentStage == Stage.RECOVER_ZIGGURAT) {
//			info.addPara("Recover the Ziggurat-class phase vessel, which was apparently developed in "
//					+ "secret by Tri-Tachyon at Alpha Site.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy with the scan data and report your findings to Provost " + 
					 getPerson().getNameString() + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GET_KELISE_LEAD) {
			info.addPara("Get a lead on the whereabouts of Kelise Astraia", tc, pad);
			return true;
		} else if (currentStage == Stage.SELL_BLACKMAIL_MATERIAL) {
			info.addPara("Sell Arroyo's comm fakes to any highly-placed pirate", tc, pad);
			return true;
		} else if (currentStage == Stage.SOLD_BLACKMAIL_MATERIAL) {
			info.addPara(getGoTalkToPersonText(arroyo), tc, pad);
			return true;
		} else if (currentStage == Stage.TALK_TO_CALLISTO) {
			info.addPara(getGoTalkToPersonText(callisto), tc, pad);
			return true;
		} else if (currentStage == Stage.GO_TO_RELAY_SYSTEM) {
			info.addPara(getGoToSystemTextShort(relaySystem), tc, pad);
			return true;
		} else if (currentStage == Stage.GO_TO_NASCENT_WELL) {
			info.addPara("Investigate hyperspace area the relay was transmitting to", tc, pad);
			return true;
		} else if (currentStage == Stage.INVESTIGATE_SITE) {
			info.addPara("Investigate Alpha Site", tc, pad);
			return true;
//		} else if (currentStage == Stage.RECOVER_ZIGGURAT) {
//			info.addPara("Recover the Ziggurat-class phase vessel", tc, pad);
//			return true;
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and report to Provost Baird", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Project Ziggurat";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	
	@Override
	public void setCurrentStage(Object next, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.setCurrentStage(next, dialog, memoryMap);
		if (next == Stage.RETURN_TO_ACADEMY) {
			// "At The Gates" is offered in response to this variable being set.
			// It can be set either by scanning the Ziggurat without doing this mission, OR
			// by finishing this mission.
			// What we want to avoid is a scenario when this variable remains set before the mission is finished
			// and the player returns to the GA and is potentially offered At The Gates before PZ is completed.
			Global.getSector().getMemoryWithoutUpdate().unset(SCANNED_ZIGGURAT);
		} else if (next == Stage.GO_TO_RELAY_SYSTEM) {
			// just talked to Callisto
			if (hamatsu != null) {
				Misc.makeImportant(hamatsu, getReason());
			}
		}
	}

//	@Override
//	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
//		super.acceptImpl(dialog, memoryMap);
//		Global.getSector().getListenerManager().addListener(this);
//	}
//	
//	public void reportShipsRecovered(List<FleetMemberAPI> ships, InteractionDialogAPI dialog) {
//		if (!(dialog instanceof RuleBasedInteractionDialogPluginImpl)) return;
//		
//		for (FleetMemberAPI member : ships) {
//			if (member.getHullId().equals("ziggurat")) {
//				Global.getSector().getListenerManager().removeListener(this);
//				Global.getSector().getMemoryWithoutUpdate().set(RECOVERED_ZIGGURAT, true, 0);
//				checkStageChangesAndTriggers(dialog, ((RuleBasedInteractionDialogPluginImpl)dialog).getMemoryMap());
//				Global.getSector().getMemoryWithoutUpdate().unset(RECOVERED_ZIGGURAT);
//			}
//		}
//	}

	
}





