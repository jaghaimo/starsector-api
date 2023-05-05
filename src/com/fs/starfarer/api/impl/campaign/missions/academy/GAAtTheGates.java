package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.BaseFleetEventListener;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class GAAtTheGates extends GABaseMission implements CurrentLocationChangedListener {

	public static String SHOW_GATE_SCAN_COUNT = "show_gate_scan_count";
	
	public static enum Stage {
		TALK_TO_COUREUSE,
		TALK_TO_YARIBAY,
		TALK_TO_HEGEMON,
		MEET_DAUD,
		DO_SCANS,
		RETURN_WITH_DEAL_AND_SCANS,
		FIRST_JANUS_EXPERIMENT,
		FIRST_JANUS_RESULTS,
		COUREUSE_MISSION,
		TALK_TO_KANTA,
		FINDING_LOKE,
		RETURN_TO_KANTA,
		GO_TO_MAGEC_GATE,
		ZAL_TO_GALATIA,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI coureuse;
	protected PersonAPI gargoyle;
	
	protected PersonAPI horus_yaribay;
	protected PersonAPI siyavong;
	protected MarketAPI kazeron;
	//protected StarSystemAPI magec;
	protected SectorEntityToken magecGate;
	protected SectorEntityToken galatiaGate;
	
	protected PersonAPI daud;
	protected MarketAPI chicomoztoc;
	
	protected PersonAPI kanta;
	protected PersonAPI loke;
	protected PersonAPI cotton;
	protected PersonAPI zal;
	protected PersonAPI kantasDenStationCommander;
	protected MarketAPI kantasDen;
	protected MarketAPI epiphany;
	
	protected SectorEntityToken encounterGateHegemony;
	protected CampaignFleetAPI encounterHegemonyFleet;
	
	protected SectorEntityToken encounterGateLuddic;
	protected CampaignFleetAPI encounterLuddicFleet;
	
	protected SectorEntityToken encounterGateTT;
	protected CampaignFleetAPI encounterTTFleet;
	protected int ttScanCost;
	protected int pirateScanCost;
	protected int coureuseCredits;
	
	protected SectorEntityToken encounterGatePirate;
	protected CampaignFleetAPI encounterPirateFleet;
	
	//protected SectorEntityToken encounterGateAlarm;
	//protected CampaignFleetAPI encounterHegemonyFleet;
	
	protected List<ScanEncounterVariation> scanEncounterVariations;
	
	public static float SYSTEM_NO_GATE_ENCOUNTER_CHANCE = 0.2f;
	public static float FACTION_GATE_ENCOUNTER_CHANCE = 0.5f;

	public static enum ScanEncounterVariation {
		HEGEMONY,
		TRITACHYON,
		LUDDIC,
		PIRATE,
		DERELICT,
		JAMMER,
		SCAVENGER,
		PATHER,
		ALARM,
	}
	
	public static float KANTA_RAID_DIFFICULTY = 1000f;
	public static float COTTON_RAID_DIFFICULTY = 1000f;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$gaATG_ref", "$gaATG_inProgress")) {
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		
		coureuse = getImportantPerson(People.COUREUSE);
		if (coureuse == null) return false;
		
		gargoyle = getImportantPerson(People.GARGOYLE);
		if (gargoyle == null) return false;
		
		horus_yaribay = getImportantPerson(People.HORUS_YARIBAY);
		if (horus_yaribay == null) return false;
		
		siyavong = getImportantPerson(People.SIYAVONG);
		if (siyavong == null) return false;
		
		daud = getImportantPerson(People.DAUD);
		if (daud == null) return false;
		
		kanta = getImportantPerson(People.KANTA);
		if (kanta == null) return false;
		
		loke = getImportantPerson(People.CLONE_LOKE);
		if (loke == null) return false;
		
		cotton = getImportantPerson(People.COTTON);
		if (cotton == null) return false;
		
		zal = getImportantPerson(People.ZAL);
		if (zal == null) return false;
		
		kazeron = Global.getSector().getEconomy().getMarket("kazeron");
		if (kazeron == null) return false;
		
		chicomoztoc = Global.getSector().getEconomy().getMarket("chicomoztoc");
		if (chicomoztoc == null) return false;
		
		kantasDen = Global.getSector().getEconomy().getMarket("kantas_den");
		if (kantasDen == null) return false;
		
		kantasDenStationCommander = getPersonAtMarketPost(kantasDen, Ranks.POST_STATION_COMMANDER);
		if (kantasDenStationCommander == null) return false;
		
		epiphany = Global.getSector().getEconomy().getMarket("epiphany");
		if (epiphany == null) return false;
		
		// find the Magec gate!
		StarSystemAPI magec = kantasDen.getStarSystem();
		for (SectorEntityToken curr : magec.getCustomEntitiesWithTag(Tags.GATE)) {
			//if (GateEntityPlugin.isScanned(curr)) continue;
			//if (GateEntityPlugin.isActive(curr)) continue;
			// plus whatever other checks are needed
			magecGate = curr;
			break;
		}
		if (magecGate == null) return false;
		
		// and Galatia Gate
		StarSystemAPI galatia =  Global.getSector().getStarSystem("galatia");
		for (SectorEntityToken curr : galatia.getCustomEntitiesWithTag(Tags.GATE)) {
			//if (GateEntityPlugin.isScanned(curr)) continue;
			//if (GateEntityPlugin.isActive(curr)) continue;
			// plus whatever other checks are needed
			galatiaGate = curr;
			break;
		}
		if (galatiaGate == null) return false;

		setName("At The Gates");
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		// set up our fleet spawns!
		// Siyavong first
		// Siyavong gets a patrol fleet to intercept player
		//beginWithinHyperspaceRangeTrigger(kazeron, 1f, false, Stage.TALK_TO_HEGEMON);
		//triggerCreateFleet(FleetSize.SMALL, FleetQuality.VERY_LOW, Factions.LUDDIC_PATH, FleetTypes.PATROL_SMALL, probeSystem);
		
		beginStageTrigger(Stage.TALK_TO_HEGEMON);
		triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.HIGHER, Factions.PERSEAN, FleetTypes.PATROL_MEDIUM, kazeron.getStarSystem());
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerMakeNonHostile();
		triggerMakeFleetIgnoreOtherFleetsExceptPlayer(); // don't go chasing others, please.
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		triggerFleetMakeFaster(true, 1, true);
		
		triggerPickLocationAroundEntity(kazeron.getStarSystem().getHyperspaceAnchor(), 250f);
		triggerSpawnFleetAtPickedLocation("$gaATG_siyavongFleet", null);
		//triggerSpawnFleetAtPickedLocation("$gaATG_pirateScanFleet", null);
		
		//triggerPickLocationTowardsEntity(kazeron.getStarSystem().getHyperspaceAnchor(), 90f, getUnits(1f));
		triggerSetFleetMissionRef("$gaATG_ref"); // so they can be made unimportant
		triggerOrderFleetInterceptPlayer();
		triggerOrderFleetEBurn(1f);
		triggerFleetMakeImportant(null, Stage.TALK_TO_HEGEMON);
		endTrigger();

		
		// For the big showdown w/ Kanta
		List<SectorEntityToken> jumpPoints = magecGate.getStarSystem().getJumpPoints();
		for (SectorEntityToken point : jumpPoints) {
			spawnKantaVengeanceFleetPirateScout(point);
			spawnKantaVengeanceFleetPirateScout(point); // 2 scouts per.
			spawnKantaVengeanceFleetPirateArmada(point);
			spawnKantaVengeanceFleetMerc(point);
		}
		spawnKantaVengeanceFleetPirateArmada(kantasDen.getPrimaryEntity());
		
		//scanEncounterVariations = Arrays.asList(ScanEncounterVariation.values()); // NO. Bad list.
		scanEncounterVariations = new ArrayList<ScanEncounterVariation>(Arrays.asList(ScanEncounterVariation.values()));
		
		ttScanCost = genRoundNumber(20000, 30000); // bribe for TT scanning fleet
		pirateScanCost = genRoundNumber(10000, 20000); // bribe for Pirate gate-blockers
		coureuseCredits = genRoundNumber(10000, 20000); // Coureuse's pathetic savings

		setStoryMission();
		
		setStartingStage(Stage.TALK_TO_COUREUSE);
		addSuccessStages(Stage.COMPLETED);
		
		makeImportant(coureuse, null, Stage.TALK_TO_COUREUSE);
		makeImportant(horus_yaribay, null, Stage.TALK_TO_YARIBAY);
		makeImportant(daud, null, Stage.TALK_TO_HEGEMON);
		makeImportant(chicomoztoc, null, Stage.MEET_DAUD);
		makeImportant(baird, null, Stage.RETURN_WITH_DEAL_AND_SCANS);
		makeImportant(baird, null, Stage.FIRST_JANUS_RESULTS);
		makeImportant(coureuse, null, Stage.COUREUSE_MISSION);
		makeImportant(kantasDen, "$gaATG_talkToKanta", Stage.TALK_TO_KANTA);
		makeImportant(epiphany, "$gaATG_findingLoke", Stage.FINDING_LOKE);
		makeImportant(kanta.getMarket(), null, Stage.RETURN_TO_KANTA);
		makeImportant(magecGate, null, Stage.GO_TO_MAGEC_GATE);
		makeImportant(baird.getMarket(), null, Stage.ZAL_TO_GALATIA);
		
		//setStageOnGlobalFlag(Stage.TALK_TO_COUREUSE, "$gaATG_goTalkToCoureuse");
		connectWithGlobalFlag(Stage.TALK_TO_COUREUSE, Stage.TALK_TO_YARIBAY, "$gaATG_goTalkToYaribay");
		connectWithGlobalFlag(Stage.TALK_TO_YARIBAY, Stage.TALK_TO_HEGEMON, "$gaATG_goTalkToDaud");
		connectWithGlobalFlag(Stage.TALK_TO_HEGEMON, Stage.MEET_DAUD, "$gaATG_goToDaudMeeting");
		connectWithGlobalFlag(Stage.MEET_DAUD, Stage.DO_SCANS, "$gaATG_gotDaudDeal");
		connectWithGlobalFlag(Stage.DO_SCANS, Stage.RETURN_WITH_DEAL_AND_SCANS, "$gaATG_scannedSixGates");
		setStageOnGlobalFlag(Stage.FIRST_JANUS_EXPERIMENT, "$gaATG_useJanusPrototype");
		setStageOnGlobalFlag(Stage.FIRST_JANUS_RESULTS, "$gaATG_firstJanusResults");
		setStageOnGlobalFlag(Stage.COUREUSE_MISSION, "$gaATG_getMissionFromCoureuse");
		setStageOnGlobalFlag(Stage.TALK_TO_KANTA, "$gaATG_gotKantaToken");
		setStageOnGlobalFlag(Stage.FINDING_LOKE, "$gaATG_findingLoke");
		setStageOnGlobalFlag(Stage.RETURN_TO_KANTA, "$gaATG_foundLoke");
		setStageOnGlobalFlag(Stage.GO_TO_MAGEC_GATE, "$gaATG_foundZal");
		
		connectWithEnteredLocation(Stage.GO_TO_MAGEC_GATE, Stage.ZAL_TO_GALATIA, galatia);
		//setStageOnGlobalFlag(Stage.ZAL_TO_GALATIA, "$gaATG_zalToGalatia");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaATG_completed");
		
		// so they can decivilize etc but not before this and any other story missions using these are completed
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$gaATG_missionCompleted", true);
		triggerMakeNonStoryCritical(kazeron, chicomoztoc, epiphany, siyavong.getMarket(), kanta.getMarket());
		endTrigger();
		
		return true;
	}
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								Map<String, MemoryAPI> memoryMap) {
		
		// $global.numGatesScanned tracks this too; maybe not ideal to dupe the var, but yolo -dgb
		if ("scannedGateForCoureuse".equals(action)) {
			sendUpdate(SHOW_GATE_SCAN_COUNT, dialog.getTextPanel());
			return true;
		}
		else if ("setGalatiaGateScanned".equals(action))
		{
			galatiaGate.getMemoryWithoutUpdate().set(GateEntityPlugin.GATE_SCANNED, true);
			GateCMD.notifyScanned(galatiaGate);
			return true;
		}
		else if ("clearedHegemonyGate".equals(action)) {
			encounterGateHegemony.getMemoryWithoutUpdate().unset("$GAATGhegemonyScanFleet");
			return true;
		}
		else if ("clearedHegemonyGateSendScans".equals(action)) {
			encounterGateHegemony.getMemoryWithoutUpdate().set("$GAATGhegSendScan", true);
			encounterGateHegemony.getMemoryWithoutUpdate().unset("$GAATGhegemonyScanFleet");
			encounterHegemonyFleet.getMemoryWithoutUpdate().unset("$youDoTheScan");
			encounterHegemonyFleet.getMemoryWithoutUpdate().set("$youDidTheScan", true);
			return true;
		}
		else if ("sentHegemonyScan".equals(action)) {
			encounterHegemonyFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS, true);
			return true;
		}
		else if ("clearedLuddicGate".equals(action)) {
			encounterGateLuddic.getMemoryWithoutUpdate().unset("$GAATGluddicScanGate");
			return true;
		}
		else if ("luddicGateFleetMoveAway".equals(action)) {
			encounterGateLuddic.getMemoryWithoutUpdate().unset("$GAATGluddicScanGate");
			encounterGateLuddic.getMemoryWithoutUpdate().set("$GAATGluddicPostScan", true);
			encounterLuddicFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY, true);
			encounterLuddicFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS, true);
			return true;
		}
		else if ("luddicGateFleetMoveBack".equals(action)) {
			//encounterGateLuddic.getMemoryWithoutUpdate().unset("$GAATGluddicScanGate");
			encounterLuddicFleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY);
			encounterLuddicFleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS);
			encounterLuddicFleet.getMemoryWithoutUpdate().set("$resumedVigil", true);
			return true;
		}
		
		else if ("boughtTTscan".equals(action)) {
			encounterGateTT.getMemoryWithoutUpdate().unset("$GAATGttScanGate");
			encounterGateTT.getMemoryWithoutUpdate().set(GateEntityPlugin.GATE_SCANNED, true);
			GateCMD.notifyScanned(encounterGateTT);
			sendUpdate(SHOW_GATE_SCAN_COUNT, dialog.getTextPanel());
			return true;
		}
		else if ("clearedTTGate".equals(action)) {
			encounterGateTT.getMemoryWithoutUpdate().unset("$GAATGttScanGate");
			return true;
		}
		else if ("clearedPirateGate".equals(action)) {
			encounterGatePirate.getMemoryWithoutUpdate().unset("$GAATGpirateScanGate");
			encounterPirateFleet.getMemoryWithoutUpdate().unset("$gaATG_pirateScanFleet");
			return true;
		}
		else if ("doZalEscape".equals(action)) {
			Global.getSector().layInCourseFor(magecGate);
			galatiaGate.getMemoryWithoutUpdate().set(GateEntityPlugin.GATE_SCANNED, true);
			return true;
		}
		else if ("giveJanusDevice".equals(action)) {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.addSpecial(new SpecialItemData(Items.JANUS, null), 1);
			CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData(Items.JANUS, null), null);
			AddRemoveCommodity.addStackGainText(stack, dialog.getTextPanel());
			return true;
		}
		else if ("endMissionCleanup".equals(action)){
			
			epiphany.getMemoryWithoutUpdate().unset("$askedForKidnapperCotton");
			epiphany.getMemoryWithoutUpdate().unset("$offeredBribeForCotton");
			epiphany.getMemoryWithoutUpdate().unset("$setUpCottonMeeting");
			epiphany.getMemoryWithoutUpdate().unset("$askedBarCotton");
			epiphany.getMemoryWithoutUpdate().unset("$setUpCottonMeeting");
			
			//magecGate.getMemoryWithoutUpdate().unset("$visitedWithZal");
			return true;
		}
		else if ("zalCommHack".equals(action)){
			
			TextPanelAPI text = dialog.getTextPanel();
			text.setFontVictor();
			Color red = Misc.getNegativeHighlightColor();
			text.addParagraph("COMM FEED 0 INTERRUPTED \nCOMM FEED 1 INTERRUPTED", red);
			//text.setFontSmallInsignia();
			text.setFontInsignia();
			return true;
		}
		else if ("zalMinesHack".equals(action))
		{
			Global.getSoundPlayer().playSound("hit_heavy", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			return true;
		}
		else if ("giveJanusDevice".equals(action))
		{
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.addSpecial(new SpecialItemData(Items.JANUS, null), 1);
			CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData(Items.JANUS, null), null);
			AddRemoveCommodity.addStackGainText(stack, dialog.getTextPanel());
			return true;
		}
		//else if ("addYaribayContact".equals(action))
		//{
		//	ContactIntel.addPotentialContact(horus_yaribay, kazeron, dialog.getTextPanel());
		//	//setPersonIsPotentialContactOnSuccess(horus_yaribay, 1f); // probability = 1f, otherwise it's only a chance (default 0.25)
		//	return true;
		//}
		
		return false;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaATG_stage", getCurrentStage());
		set("$gaATG_gatesScanned", GateEntityPlugin.getNumGatesScanned());
		set("$gaATG_ttScanCost", Misc.getWithDGS(ttScanCost));
		set("$gaATG_pirateScanCost", Misc.getWithDGS(pirateScanCost));
		set("$gaATG_coureuseCredits", Misc.getWithDGS(coureuseCredits));
		
		set("$gaATG_kantaRaidDifficulty", KANTA_RAID_DIFFICULTY);
		set("$gaATG_cottonRaidDifficulty", COTTON_RAID_DIFFICULTY);
		set("$kantasDenStationCommander", kantasDenStationCommander.getId()); // kantasDenStationCommander);
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		if ( currentStage == Stage.TALK_TO_COUREUSE || 
				 currentStage == Stage.TALK_TO_YARIBAY ||
				 currentStage == Stage.TALK_TO_HEGEMON || 
				 currentStage == Stage.DO_SCANS) { 
			int scanned = GateEntityPlugin.getNumGatesScanned();
			if (scanned >= 6 && 
					!Global.getSector().getMemoryWithoutUpdate().contains("$gaATG_scannedSixGates")) { // failsafe; apparently possible to not have this be set?
				Global.getSector().getMemoryWithoutUpdate().set("$gaATG_scannedSixGates", true);
				checkStageChangesAndTriggers(null, null);
			}
		}
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		
		
		
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		// Contact the High Hegemon
		if (currentStage == Stage.TALK_TO_COUREUSE || 
				currentStage == Stage.TALK_TO_YARIBAY ) {
			info.addPara("Contact Horus Yaribay to get envoy credentials so you can contact High Hegemon Daud.", opad);
			addStandardMarketDesc("Gens Yaribay is based " + kazeron.getOnOrAt(), kazeron, info, opad);
		} 
		else if (currentStage == Stage.TALK_TO_HEGEMON)
		{
			info.addPara("Set up a private meeting with High Hegemon Baikal Daud and get him to accept the deal with Provost Baird.", opad);
			info.addPara("Optional: provide Daud with encrypted Yaribay comm codes.", opad);
			addStandardMarketDesc("The High Hegemon's office is based " + chicomoztoc.getOnOrAt(), chicomoztoc, info, opad);
		}
		else if (currentStage == Stage.MEET_DAUD)
		{
			info.addPara("Meet with High Hegemon Baikal Daud on a shipyard orbiting Chicomoztoc.", opad);
			info.addPara("Optional: provide Daud with encrypted Yaribay comm codes.", opad);
			addStandardMarketDesc("The High Hegemon's office is based " + chicomoztoc.getOnOrAt(), chicomoztoc, info, opad);
		}
		else if (currentStage == Stage.MEET_DAUD)
		{
			
		}
		else if (currentStage == Stage.RETURN_WITH_DEAL_AND_SCANS)
		{
			info.addPara("You've arranged a deal between High Hegemon Daud and Provost Baird which allows "
					+ "the Galatia Academy to resume overt research on Gate technology.",opad);
		}

		
		// Scan six Gates
		if ( currentStage == Stage.TALK_TO_COUREUSE)
		{
			info.addPara("Get the Gate scanning instrument from Academician Coureuse.", opad);
		}
		else if ( currentStage == Stage.TALK_TO_COUREUSE || 
			 currentStage == Stage.TALK_TO_YARIBAY ||
			 currentStage == Stage.TALK_TO_HEGEMON || 
			 currentStage == Stage.DO_SCANS &&
					 GateEntityPlugin.getNumGatesScanned() < 6 )
		{
			info.addPara("Scan six Gates with the instruments provided by Academician Coureuse.", opad);
			bullet(info);
			info.addPara("%s Gates scanned", opad, Misc.getHighlightColor(), 
					"" + GateEntityPlugin.getNumGatesScanned() + " / 6");
			unindent(info);
		}
		
		// Everything else in the mission arc.
		if (currentStage == Stage.RETURN_WITH_DEAL_AND_SCANS)
		{
			info.addPara("Return to the Galatia Academy with the six completed Gate scans.", opad);
		}
		else if (currentStage == Stage.FIRST_JANUS_EXPERIMENT)
		{
			info.addPara("Use the prototype Janus Device on a Gate in an uninhabited system outside of the "
					+ "Core worlds.", opad);
		}
		else if (currentStage == Stage.FIRST_JANUS_RESULTS)
		{
			info.addPara("Return to the Galatia Academy to report the results of the first Janus Prototype "
					+ "experiment.", opad);
		}
		else if (currentStage == Stage.COUREUSE_MISSION)
		{
			info.addPara("Contact Academician Scylla Coureuse about what she needs you to do.", opad);
		}
		else if (currentStage == Stage.TALK_TO_KANTA)
		{
			info.addPara("Use Gargoyle's token to arrange a meeting with Warlord Kanta in the Magec system - or find some other way to extract Zal.", opad);
		}
		else if (currentStage == Stage.FINDING_LOKE)
		{
			info.addPara("Find and extract the clone of Loke from the Pather kidnappers.", opad);
		}
		else if (currentStage == Stage.RETURN_TO_KANTA)
		{
			info.addPara("Return Clone Loke to Warlord Kanta.", opad);
		}
		else if (currentStage == Stage.GO_TO_MAGEC_GATE)
		{
			info.addPara("Go to the Magec Gate.", opad);
		}
		else if (currentStage == Stage.ZAL_TO_GALATIA)
		{
			info.addPara("Return to the Galatia Academy with Elissa Zal and the working Janus Device.", opad);
		}
	}
	
	@Override
	protected boolean shouldSendUpdateForStage(Object id) {
		if (getCurrentStage() == Stage.DO_SCANS && GateEntityPlugin.getNumGatesScanned() >= 6) {
			return false; // going straight to RETURN_WITH_DEAL_AND_SCANS
		}
		return super.shouldSendUpdateForStage(id);
	}
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {

		Color h = Misc.getHighlightColor();
		
		if (getListInfoParam() == SHOW_GATE_SCAN_COUNT) {
			if (GateEntityPlugin.getNumGatesScanned() >= 6)
			{
				// We don't want to end up writing 7/6 if the player gets excitable about scanning before talking to Daud -dgb
				info.addPara("%s Gates scanned", pad, tc, Misc.getHighlightColor(), "6 / 6");
			}
			else
			{
				info.addPara("%s Gates scanned", pad, tc, Misc.getHighlightColor(), 
						"" + GateEntityPlugin.getNumGatesScanned() + " / 6");
			}
			return true;
			
		}
		
		// Scanning equipment
		// Do this first because the player should talk to Coureuse while still at the GA -dgb
		if (currentStage == Stage.TALK_TO_COUREUSE)
		{
			info.addPara("Get the Gate scanning device from Scylla Coureuse", tc, pad);
			pad = 0; // the padding passed in is just for before the first item
		}
		
		// High Hegemon contact
		if (currentStage == Stage.TALK_TO_COUREUSE || 
				currentStage == Stage.TALK_TO_YARIBAY ) {
			info.addPara("Contact Horus Yaribay on Kazeron to get envoy credentials", tc, pad);
			pad = 0;
		} 
		else if (currentStage == Stage.TALK_TO_HEGEMON)
		{
			info.addPara("Horus Yaribay has provided you with official envoy credentials to set up a private meeting with High "
					+ "Hegemon Baikal Daud", tc, pad);
			//info.addPara("Optional: provide Daud with encrypted Yaribay comm codes.", opad);
			//info.addPara("Use the gens Yaribay envoy credentials to set up a private meeting with High "
			//		+ "Hegemon Baikal Daud. Get him to accept a new arrangement with Provost Baird.", opad);
			pad = 0;
		}
		else if (currentStage == Stage.MEET_DAUD)
		{
			info.addPara("Meet with the High Hegemon in Chicomoztoc orbit and secure Baird's deal", tc, pad);
			pad = 0;
		}
		else if (currentStage == Stage.RETURN_WITH_DEAL_AND_SCANS)
		{
			info.addPara("Return to Provost Baird at the Galatia Academy with news of the deal made with "
					+ "High Hegemon Daud", tc, pad);
			pad = 0;
		}
		
		// ... Then scan six Gates; this appears below Yaribay/Daud stuff as it's the next objective -dgb	
		if (	(	currentStage == Stage.TALK_TO_YARIBAY ||
					currentStage == Stage.TALK_TO_HEGEMON || 
					currentStage == Stage.MEET_DAUD || 
					currentStage == Stage.DO_SCANS)
				&& GateEntityPlugin.getNumGatesScanned() < 6) 
			// Though this last point is implicit in the Stage, perhaps? Maybe not though, 
			// because you can scan all the gates while doing the Hegemon stuff. So keep this. -dgb
		{
			info.addPara("Scan six Gates with the instruments provided by Academician Coureuse", tc, pad);
			pad = 0;
		}

		if (currentStage == Stage.RETURN_WITH_DEAL_AND_SCANS)
		{
			info.addPara("Return to the Galatia Academy with the six completed Gate scans", tc, pad);
		}
		else if (currentStage == Stage.FIRST_JANUS_EXPERIMENT)
		{
			info.addPara("Use the prototype Janus Device on a Gate in an uninhabited system outside of the "
					+ "Core worlds", tc, pad);
		}
		else if (currentStage == Stage.FIRST_JANUS_RESULTS)
		{
			info.addPara("Return to the Galatia Academy to report the results of the first Janus Prototype "
					+ "experiment", tc, pad);
		}
		else if (currentStage == Stage.COUREUSE_MISSION)
		{
			info.addPara("Contact Academician Scylla Coureuse about what she needs you to do", tc, pad);
		}
		else if (currentStage == Stage.TALK_TO_KANTA)
		{
			info.addPara("Use Gargoyle's token to arrange a meeting with Warlord Kanta in the Magec system - or find some other way to extract Zal", tc, pad);
		}
		else if (currentStage == Stage.FINDING_LOKE)
		{
			info.addPara("Find and extract the clone of Loke from the Pather kidnappers", tc, pad);
		}
		else if (currentStage == Stage.RETURN_TO_KANTA)
		{
			info.addPara("Return Clone Loke to Warlord Kanta", tc, pad);
		}
		else if (currentStage == Stage.GO_TO_MAGEC_GATE)
		{
			info.addPara("Go to the Magec Gate", tc, pad);
		}
		else if (currentStage == Stage.ZAL_TO_GALATIA)
		{
			info.addPara("Return to the Galatia Academy with Elissa Zal and the working Janus Device", tc, pad);
		}
		
		return false;
	}
	
	@Override
	public String getBaseName() {
		return "At The Gates";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.acceptImpl(dialog, memoryMap);
		Global.getSector().getListenerManager().addListener(this);
	}
	
	@Override
	protected void notifyEnding() {
		Global.getSector().getListenerManager().removeListener(this);
		super.notifyEnding();
	}
	
	// I guess either this doesn't need to be weighted, or I should integrate the weighted choice
	// into this dang function. I'll do it ... later, maybe. -dgb, TODO
	public ScanEncounterVariation pickGateEvent() {
		WeightedRandomPicker<ScanEncounterVariation> picker = new WeightedRandomPicker<ScanEncounterVariation>(genRandom);
		for (ScanEncounterVariation event : scanEncounterVariations) {
			picker.add(event, 1f);
		}
		/*FactionData data = FACTION_DATA.get(factionId);
		if (data != null) {
			float w = Math.max(1f, data.events.length) / Math.max(1f, picker.getTotal());
			w *= SPECIFIC_FACTION_SUBJECT_EVENT_LIKELIHOOD; // faction-specific is more likely than generic, overall
			for (String event : data.events) {
				picker.add(event, w);
			}
		}*/
		return picker.pick();
	}

	public static String CHECKED_FOR_ENCOUNTER = "$gaATG_checkedForEncounter";
	public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI currLoc) {
		if (getCurrentStage() == null) return;
		
		// if in the wrong stage for gate encounters: return
		if (((Enum)getCurrentStage()).ordinal() >= Stage.RETURN_WITH_DEAL_AND_SCANS.ordinal()) return;
		if (((Enum)getCurrentStage()).ordinal() < Stage.TALK_TO_YARIBAY.ordinal()) return;
		
		if(!(currLoc instanceof StarSystemAPI)) return;
		
		// if no gate encounters remain, return.
		if (scanEncounterVariations.isEmpty()) return;
		
		StarSystemAPI system = (StarSystemAPI) currLoc;
		if (system.getMemoryWithoutUpdate().getBoolean(CHECKED_FOR_ENCOUNTER)) return;
		system.getMemoryWithoutUpdate().set(CHECKED_FOR_ENCOUNTER, true);
		
		// We don't want the encounter fleet to get destroyed by pulsars or Remnants. 
		// Plus, this should make the Gate "interesting" enough already. 
		if (system.hasTag(Tags.THEME_UNSAFE)) return;
		if(Misc.hasPulsar(system)) return;

		// if the system is unsuitable - no gate, or gate is scanned: return
		SectorEntityToken gate = null;
		for (SectorEntityToken curr : system.getCustomEntitiesWithTag(Tags.GATE)) {
			if (GateEntityPlugin.isScanned(curr)) continue;
			if (GateEntityPlugin.isActive(curr)) continue;
			// plus whatever other checks are needed
			gate = curr;
			break;
		}
		
		if (gate == null) return;
		
		// Not every Gate has a cool story, right?
		if (rollProbability(SYSTEM_NO_GATE_ENCOUNTER_CHANCE)) return;
		
		// pick what type of encounter to do and spawn it
		// also add a temporary flag to the system to not spawn *another* encounter here
		
		ScanEncounterVariation chosenEncounter = null;
		
		// testing.
		//chosenEncounter = ScanEncounterVariation.HEGEMONY;
		

		for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
			if (scanEncounterVariations.contains(ScanEncounterVariation.HEGEMONY) && 
				market.getFactionId().equals(Factions.HEGEMONY) &&
				rollProbability(FACTION_GATE_ENCOUNTER_CHANCE))
			{
				chosenEncounter = ScanEncounterVariation.HEGEMONY;
				break;
			}
			else if (scanEncounterVariations.contains(ScanEncounterVariation.TRITACHYON) && 
					market.getFactionId().equals(Factions.TRITACHYON) &&
					rollProbability(FACTION_GATE_ENCOUNTER_CHANCE))
			{
				chosenEncounter = ScanEncounterVariation.TRITACHYON;
				break;
			}
			else if (scanEncounterVariations.contains(ScanEncounterVariation.LUDDIC) && 
					market.getFactionId().equals(Factions.LUDDIC_CHURCH) &&
					rollProbability(FACTION_GATE_ENCOUNTER_CHANCE))
			{
				chosenEncounter = ScanEncounterVariation.LUDDIC;
				break;
			}
			else if (scanEncounterVariations.contains(ScanEncounterVariation.LUDDIC) && 
					market.getFactionId().equals(Factions.LUDDIC_CHURCH) &&
					rollProbability(FACTION_GATE_ENCOUNTER_CHANCE))
			{
				chosenEncounter = ScanEncounterVariation.LUDDIC;
				break;
			}
			else if (scanEncounterVariations.contains(ScanEncounterVariation.PIRATE) && 
					market.getFactionId().equals(Factions.PIRATES) &&
					rollProbability(FACTION_GATE_ENCOUNTER_CHANCE))
			{
				chosenEncounter = ScanEncounterVariation.PIRATE;
				break;
			} 
		}
		
		// Choose randomly from the remaining options if none was picked.
		if( chosenEncounter == null)
		{
				chosenEncounter = pickGateEvent();
		}
		
		if( chosenEncounter == null) return; // Uh oh?
		
		scanEncounterVariations.remove(chosenEncounter);
/*
		System.out.println("scan chosen is " + chosenEncounter.toString());
		System.out.println("remaining encounters:");
		for (ScanEncounterVariation v : scanEncounterVariations) {
			System.out.println(v.toString());
		}
	*/		

		//		if (system.getId().equals("corvus")) {
		//		// an alternate way to figure out what's appropriate where
		//		hasHegemony = true;
		//	}
		
		// of note: since this happens when the player enters a system - i.e. not during mission creation,
		// but while the actual mission is in progress - it's also ok to do things here directly in code
		// instead of having to use triggers. The above commented out block is using a trigger (+ runTriggers())
		// just because it's an easy way to spawn a fleet.
		
		if(chosenEncounter == ScanEncounterVariation.DERELICT)
		{
			DerelictShipData params = new DerelictShipData(new PerShipData("apogee_Balanced", ShipCondition.BATTERED, 0f), false);

			SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
			ship.setDiscoverable(true);

			ship.setCircularOrbit(gate, (float) Math.random() * 360f, 40f, 20f);

			// Eh, maybe some base chance of recoverable vs. not? -dgb
			ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
			
			ship.getMemoryWithoutUpdate().set("$gateScanDerelict", true);
		}
		else if(chosenEncounter == ScanEncounterVariation.HEGEMONY)
		{
			//gate.addTag("GAATGhegemonyScanFleet");
			
			//if (hasHegemony) { // whatever conditions are appropriate
			beginStageTrigger(getCurrentStage());
			triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.HIGHER, Factions.HEGEMONY, FleetTypes.PATROL_MEDIUM, gate);
			triggerMakeNonHostile(); // should it be hostile?
			//triggerMakeHostileAndAggressive();
			//triggerMakeLowRepImpact();
			//triggerFleetPatherNoDefaultTithe();
			
			triggerPickLocationAroundEntity(gate, 100f);
			triggerSpawnFleetAtPickedLocation("$gaATG_hegScanFleet", null);
			
			triggerFleetSetPatrolActionText("scanning");
			triggerFleetSetTravelActionText("repositioning");
			triggerOrderFleetPatrol(true, gate);
			triggerMakeFleetIgnoreOtherFleets(); // don't go chasing pirates, please.
			triggerMakeFleetGoAwayAfterDefeat();
			triggerFleetSetPatrolLeashRange(100f);
			
			//triggerMakeFleetIgnoredByOtherFleets();

			//triggerPickSetLocation(gate.getContainingLocation(), gate.getLocation()); // fleet should appear AT the gate.
			//triggerPickLocationAtClosestToPlayerJumpPoint(probeSystem);
			//triggerSetEntityToPickedJumpPoint();
			//triggerPickLocationTowardsEntity(gate, 15f, getUnits(1.0f));
			//triggerFleetMakeImportant(null, Stage.FOLLOW_THE_EXPERIMENTS);
			//triggerOrderFleetInterceptPlayer();
			
			triggerSetFleetMissionRef("$gaATG_ref"); // so they can be made unimportant
			triggerFleetAddDefeatTrigger("GAATGhegGateScanFleetDefeated");
			triggerFleetSetName("Special Task Force");
			endTrigger();
			
			// this'll execute the trigger added above immediately
			List<CampaignFleetAPI> fleets = runStageTriggersReturnFleets(getCurrentStage());
			if (fleets.isEmpty())
			{
				return; // something went wrong spawning a fleet, unlikely but technically possible
			}
			final CampaignFleetAPI fleet = fleets.get(0);
			
			fleet.addEventListener(new BaseFleetEventListener() {
				@Override
				public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
					if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
						// Cool! So now the fleet no longer blocks the Gate, and the Gate is no longer blocked. -dgb
						fleet.getMemoryWithoutUpdate().unset("$gaATG_hegScanFleet");
						encounterGateHegemony.getMemoryWithoutUpdate().unset("$GAATGhegemonyScanFleet");
					}
				}
			});
			
			encounterHegemonyFleet = fleets.get(0);
			
			// Let's do this after the potential abort, just to be safe.
			// If anything above fails, then the Gate isn't blocked -dgb.
			encounterGateHegemony = gate;
			setFlag(gate, "$GAATGhegemonyScanFleet", true);
		}
		else if(chosenEncounter == ScanEncounterVariation.JAMMER)
		{
			setFlag(gate, "$GAATGscanJammer", true);
		}
		else if(chosenEncounter == ScanEncounterVariation.ALARM)
		{
			setFlag(gate, "$GAATGscanAlarm", true);
		}
		else if(chosenEncounter == ScanEncounterVariation.LUDDIC)
		{
			beginStageTrigger(getCurrentStage());
			triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.LOWER, Factions.LUDDIC_CHURCH, FleetTypes.TRADE_LINER, gate);
			triggerMakeNonHostile();
			//triggerMakeLowRepImpact(); // Yeah uh, blowing away a civilian fleet would look bad.
			triggerSetFleetSizeFraction(0.5f);
			triggerSetFleetComposition(2f, 1f, 1f, 10f, 1f);
			triggerPickLocationAroundEntity(gate, 50f);
			triggerSpawnFleetAtPickedLocation("$gaATG_luddicScanFleet", null);
			triggerFleetSetTravelActionText("holding vigil");
			triggerFleetSetPatrolActionText("holding vigil");
			triggerOrderFleetPatrol(true, gate);
			triggerMakeFleetIgnoreOtherFleets(); // don't go chasing pirates, please.
			triggerMakeFleetGoAwayAfterDefeat();
			triggerFleetSetPatrolLeashRange(30f); // very close.
			//triggerMakeFleetIgnoredByOtherFleets();
			triggerSetFleetMissionRef("$gaATG_ref"); // so they can be made unimportant
			triggerFleetAddDefeatTrigger("GAATGluddicScanFleetDefeated");
			triggerFleetSetName("Sacred Vigil Flotilla");
			endTrigger();
			
			// this'll execute the trigger added above immediately
			List<CampaignFleetAPI> fleets = runStageTriggersReturnFleets(getCurrentStage());
			if (fleets.isEmpty())
			{
				return; // something went wrong spawning a fleet, unlikely but technically possible
			}
			final CampaignFleetAPI fleet = fleets.get(0);
			encounterLuddicFleet = fleet;
			encounterGateLuddic = gate;
			setFlag(gate, "$GAATGluddicScanGate", true);
			
			fleet.addEventListener(new BaseFleetEventListener() {
				public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
					if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
						fleet.getMemoryWithoutUpdate().unset("$gaATG_luddicScanFleet");
						encounterGateLuddic.getMemoryWithoutUpdate().unset("$GAATGluddicScanGate");
					}
				}
			});
		}
		else if(chosenEncounter == ScanEncounterVariation.PATHER)
		{
			// Just hostile Pathers. Maybe they should share their feelings/motivations with the player.
			beginStageTrigger(getCurrentStage());
			triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.HIGHER, Factions.LUDDIC_PATH, FleetTypes.MERC_PRIVATEER, gate);
			triggerFleetPatherNoDefaultTithe(); // They want to kill you.
			triggerPickLocationAroundEntity(gate, 200f);
			triggerSpawnFleetAtPickedLocation("$gaATG_patherScanFleet", null);
			triggerOrderFleetPatrol(true, gate);
			//triggerFleetSetPatrolActionText("guarding");
			//triggerMakeFleetIgnoreOtherFleets(); // actually yes, chase the player. Or anyone else, who cares.
			triggerMakeFleetGoAwayAfterDefeat();
			triggerFleetSetPatrolLeashRange(60f); // very close.
			triggerSetFleetMissionRef("$gaATG_ref"); // so they can be made unimportant
			//triggerFleetAddDefeatTrigger("GAATGpatherScanFleetDefeated"); // don't need it - they won't block the gate except via hostility.
			triggerFleetSetName("Ambush Fleet");
			endTrigger();
		}
		else if(chosenEncounter == ScanEncounterVariation.PIRATE)
		{
			beginStageTrigger(getCurrentStage());
			triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.VERY_HIGH, Factions.PIRATES, FleetTypes.PATROL_LARGE, gate);
			triggerMakeNonHostile(); // Will go hostile after yelling at you, or if you don't talk to them.
			triggerMakeLowRepImpact(); // Yeah uh, blowing away a civilian fleet would look bad.
			triggerPickLocationAroundEntity(gate, 250f);
			triggerSpawnFleetAtPickedLocation("$gaATG_pirateScanFleet", null);
			triggerFleetSetPatrolActionText("loitering");
			triggerFleetSetTravelActionText("maneuvering");
			triggerOrderFleetPatrol(true, gate);
			
			triggerFleetSetPatrolLeashRange(250f); // fairly close.
			//triggerMakeFleetIgnoredByOtherFleets();
			triggerSetFleetMissionRef("$gaATG_ref"); // so they can be made unimportant
			triggerFleetAddDefeatTrigger("GAATGpirateScanFleetDefeated");
			triggerMakeFleetGoAwayAfterDefeat();
			endTrigger();
			
			// this'll execute the trigger added above immediately
			List<CampaignFleetAPI> fleets = runStageTriggersReturnFleets(getCurrentStage());
			if (fleets.isEmpty())
			{
				return; // something went wrong spawning a fleet, unlikely but technically possible
			}
			final CampaignFleetAPI fleet = fleets.get(0);
			
			fleet.addEventListener(new BaseFleetEventListener() {
				@Override
				public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
					if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
						fleet.getMemoryWithoutUpdate().unset("$gaATG_pirateScanFleet");
						encounterGatePirate.getMemoryWithoutUpdate().unset("$GAATGpirateScanGate");
					}
				}
			});
			encounterGatePirate = gate;
			encounterPirateFleet = fleet;
			setFlag(gate, "$GAATGpirateScanGate", true);
		}
		else if(chosenEncounter == ScanEncounterVariation.SCAVENGER)
		{
			// Just a scavenger suspiciously nearby. Doesn't block the gate.
			beginStageTrigger(getCurrentStage());
			triggerCreateFleet(FleetSize.SMALL, FleetQuality.DEFAULT, Factions.INDEPENDENT, FleetTypes.SCAVENGER_MEDIUM, gate);
			triggerMakeNonHostile();
			triggerMakeLowRepImpact(); 
			triggerPickLocationAroundEntity(gate, 100f);
			triggerSpawnFleetAtPickedLocation("$gaATG_scavScanFleet", null);
			triggerFleetSetPatrolActionText("observing");
			triggerFleetSetTravelActionText("observing");
			triggerOrderFleetPatrol(true, gate);
			triggerMakeFleetIgnoreOtherFleets(); // don't go chasing pirates, please.
			triggerFleetSetPatrolLeashRange(250f);
			triggerMakeFleetIgnoredByOtherFleets(); // would be disappointing if they just got blasted.
			triggerSetFleetMissionRef("$gaATG_ref"); // so they can be made unimportant
			//triggerFleetAddDefeatTrigger("GAATGluddicScanFleetDefeated"); n/a
			triggerFleetSetName("Suspect Scavenger");
			triggerMakeFleetGoAwayAfterDefeat();
			endTrigger();
			
			List<CampaignFleetAPI> fleets = runStageTriggersReturnFleets(getCurrentStage());
			if (fleets.isEmpty()) return;
		}
		else if(chosenEncounter == ScanEncounterVariation.TRITACHYON)
		{
			beginStageTrigger(getCurrentStage());
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.HIGHER, Factions.TRITACHYON, FleetTypes.PATROL_LARGE, gate);
			triggerMakeNonHostile();
			//triggerMakeHostileAndAggressive();
			//triggerMakeLowRepImpact();
			//triggerFleetPatherNoDefaultTithe();
			
			triggerPickLocationAroundEntity(gate, 100f);
			triggerSpawnFleetAtPickedLocation("$gaATG_ttScanFleet", null);
			triggerFleetSetTravelActionText("repositioning");
			triggerFleetSetPatrolActionText("scanning");
			triggerOrderFleetPatrol(true, gate);
			triggerMakeFleetIgnoreOtherFleets(); // don't go chasing pirates, please.
			triggerMakeFleetGoAwayAfterDefeat();
			triggerFleetSetPatrolLeashRange(100f);
			
			//triggerMakeFleetIgnoredByOtherFleets();

			triggerSetFleetMissionRef("$gaATG_ref"); // so they can be made unimportant
			triggerFleetAddDefeatTrigger("GAATGttGateScanFleetDefeated");
			triggerFleetSetName("Special Projects Detachment");
			endTrigger();
			
			// this'll execute the trigger added above immediately
			List<CampaignFleetAPI> fleets = runStageTriggersReturnFleets(getCurrentStage());
			if (fleets.isEmpty())
			{
				return; // something went wrong spawning a fleet, unlikely but technically possible
			}
			final CampaignFleetAPI fleet = fleets.get(0);
			
			fleet.addEventListener(new BaseFleetEventListener() {
				@Override
				public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
					if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
						// Cool! So now the fleet no longer blocks the Gate, and the Gate is no longer blocked. -dgb
						fleet.getMemoryWithoutUpdate().unset("$gaATG_ttScanFleet");
						encounterGateTT.getMemoryWithoutUpdate().unset("$GAATGttScanGate");
					}
				}
			});
			
			//encounterHegemonyFleet = fleets.get(0);

			encounterGateTT = gate;
			setFlag(gate, "$GAATGttScanGate", true);
		}
		else
		{
			// none chosen? I guess don't do anything, huh. How'd you get here, anyway?
			return;
		}
	}
	
	protected void spawnKantaVengeanceFleetPirateArmada(SectorEntityToken spawnPoint) {
		beginWithinHyperspaceRangeTrigger(magecGate, 3f, false, Stage.GO_TO_MAGEC_GATE);
		triggerCreateFleet(FleetSize.HUGE, FleetQuality.HIGHER, Factions.PIRATES, FleetTypes.PATROL_LARGE, spawnPoint);
		triggerSetFleetFaction(Factions.PIRATES);
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerAutoAdjustFleetStrengthMajor();
		triggerMakeHostileAndAggressive();
		triggerMakeNoRepImpact();
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		triggerPickLocationAroundEntity(spawnPoint, 100f);
		triggerSetFleetMissionRef("$gaATG_ref");
		triggerSpawnFleetAtPickedLocation("$gaATG_kantaVengeanceFleet", null);
		triggerOrderFleetInterceptPlayer();
		endTrigger();
	}
	
	protected void spawnKantaVengeanceFleetPirateScout(SectorEntityToken spawnPoint) {
		beginWithinHyperspaceRangeTrigger(magecGate, 3f, false, Stage.GO_TO_MAGEC_GATE);
		triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.HIGHER, Factions.PIRATES, FleetTypes.PATROL_SMALL, spawnPoint);
		triggerSetFleetFaction(Factions.PIRATES);
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.DEFAULT);
		triggerAutoAdjustFleetStrengthMajor();
		triggerMakeHostileAndAggressive();
		triggerMakeNoRepImpact();
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		triggerFleetMakeFaster(true, 0, true);
		triggerPickLocationAroundEntity(spawnPoint, 100f);
		triggerSetFleetMissionRef("$gaATG_ref");
		triggerSpawnFleetAtPickedLocation("$gaATG_kantaVengeanceFleet", null);
		triggerOrderFleetInterceptPlayer();
		endTrigger();
	}
	
	protected void spawnKantaVengeanceFleetMerc(SectorEntityToken spawnPoint) {
		beginWithinHyperspaceRangeTrigger(magecGate, 3f, false, Stage.GO_TO_MAGEC_GATE);
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.VERY_HIGH, Factions.MERCENARY, FleetTypes.MERC_ARMADA, spawnPoint);
		triggerSetFleetFaction(Factions.PIRATES);
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerAutoAdjustFleetStrengthMajor();
		triggerMakeHostileAndAggressive();
		triggerMakeNoRepImpact();
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		triggerFleetMakeFaster(true, 1, true);
		triggerPickLocationAroundEntity(spawnPoint, 100f);
		triggerSetFleetMissionRef("$gaATG_ref");
		triggerSpawnFleetAtPickedLocation("$gaATG_kantaVengeanceFleet", null);
		triggerOrderFleetInterceptPlayer();
		endTrigger();
	}
}





