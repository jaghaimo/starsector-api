package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
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
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class GAFindingCoureuse extends GABaseMission {

	public static enum Stage {
		CHOOSE_PATH,
		INVESTIGATE_FIKENHILD,
		FOLLOW_THE_EXPERIMENTS,
		SEARCH_ISIRAH,
		CONFRONT_ARCHON,
		VISIT_COUREUSE,
		RETURN_TO_ACADEMY,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI arroyo;
	protected PersonAPI coureuse;
	protected PersonAPI siyavong;
	protected PersonAPI zal;
	protected PersonAPI laicailleArchon;
	protected PersonAPI kapteynAgent;
	protected SectorEntityToken groombridge;
	protected StarSystemAPI probeSystem;
	protected int bribeCost;
	protected int sellOutPrice;
	protected int kapteynBribeCost;
	protected int kapteynBarBribeCost;
	protected SectorEntityToken scavengerFleet;
	protected SectorEntityToken probeEmpty;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$gaFC_ref", "$gaFC_inProgress")) {
			return false;
		}
		
		setName("Finding Coureuse");
		
		MarketAPI laicaille = Global.getSector().getEconomy().getMarket("laicaille_habitat");
		MarketAPI kapteyn = Global.getSector().getEconomy().getMarket("station_kapteyn");
		MarketAPI fikenhild = Global.getSector().getEconomy().getMarket("fikenhild");
		if (laicaille == null || kapteyn == null || fikenhild == null) return false;
		
		baird = getImportantPerson(People.BAIRD);
		arroyo = getImportantPerson(People.ARROYO);
		coureuse = getImportantPerson(People.COUREUSE);
		siyavong = getImportantPerson(People.SIYAVONG);
		zal = getImportantPerson(People.ZAL);
		laicailleArchon = Global.getSector().getImportantPeople().getPerson(People.LAICAILLE_ARCHON);
		
		// Kind of a lot of effort for a minor character, but ... -dgb
		kapteynAgent = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(genRandom);
		kapteynAgent.setRankId(Ranks.CITIZEN);
		kapteynAgent.setPostId(Ranks.POST_SHADY);
		kapteynAgent.setImportance(PersonImportance.MEDIUM);
		kapteynAgent.addTag(Tags.CONTACT_UNDERWORLD);
		kapteyn.getCommDirectory().addPerson(kapteynAgent);
		kapteyn.addPerson(kapteynAgent);
		
		if (baird == null || arroyo == null || coureuse == null || 
				siyavong == null || laicailleArchon == null || kapteynAgent == null) return false;
		
		groombridge = Global.getSector().getEntityById("groombridge_habitat");
		if (groombridge == null) return false;
		
		
		
		// Find a system to hide some probes in.
		resetSearch();
		requireSystemTags(ReqMode.ANY, Tags.THEME_MISC, Tags.THEME_MISC_SKIP, Tags.THEME_RUINS);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
		requireSystemNotAlreadyUsedForStory();
		requireSystemNotHasPulsar(); // gets really awkward for the scavenger fleet if it does have one
		preferSystemOnFringeOfSector();
		preferSystemUnexplored();
		preferSystemInDirectionOfOtherMissions(); 
		probeSystem = pickSystem();
		if (probeSystem == null) return false;
		
		
		// this *should* work -Alex
		// Set up 3? probes in the system to discover. 
		// I sure hope they don't end up in the same place. -dgb
		SectorEntityToken probe1 = spawnEntity(Entities.GENERIC_PROBE, new LocData(EntityLocationType.HIDDEN, null, probeSystem));
		SectorEntityToken probe2 = spawnEntity(Entities.GENERIC_PROBE, new LocData(EntityLocationType.HIDDEN_NOT_NEAR_STAR, null, probeSystem));
		SectorEntityToken probe3 = spawnEntity(Entities.GENERIC_PROBE, new LocData(EntityLocationType.UNCOMMON, null, probeSystem));
		//SectorEntityToken probe4 = spawnEntity(Entities.GENERIC_PROBE, new LocData(EntityLocationType.HIDDEN, null, probeSystem));
		if (probe1 == null || probe2 == null || probe3 == null) return false;
		
		probe1.setCustomDescriptionId("ga_hyperprobe");
		probe2.setCustomDescriptionId("ga_hyperprobe");
		probe3.setCustomDescriptionId("ga_hyperprobe");
		
		// set a random probe as looted.
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(genRandom);
		picker.add(probe1, 1f);
		picker.add(probe2, 1f);
		picker.add(probe3, 1f);
		
		// set the empty probe aside - set it as unimportant after the scavenger probe is found
		probeEmpty = picker.pick(); 
		probeEmpty.addTag("empty");

		// "probe4" is the interior components of the looted probe, held by the scavenger.
		
		// Add the scavenger
		addProbeScavengerFleet();
		// And the ambush fleet.
		addPatherAmbushFleet();
		// And the triTach merc! Because I forgot to do this for forever.
		spawnTriTachMercFleet();
		
		bribeCost = genRoundNumber(15000, 25000); // bribe scavenger
		sellOutPrice = genRoundNumber(40000, 50000); // payment for selling out Coureuse's loc to TriTach
		kapteynBribeCost = genRoundNumber(30000, 40000); // bribe Kapteyn admin
		kapteynBarBribeCost = genRoundNumber(5000, 8000); // bribe someone at Kapteyn bar
		
		setStoryMission();
		
		setStartingStage(Stage.CHOOSE_PATH);
		addSuccessStages(Stage.COMPLETED);
		
		// doesn't seem necessary since you'll move out of that stage while still talking to Baird
		// but also wouldn't really hurt anything since it'd get unset when you move to the next stage -Alex
		//makeImportant(baird, "gaFC_contact", Stage.CHOOSE_PATH);
		
		makeImportant(fikenhild, "$gaFC_coureuseInvestigation", Stage.INVESTIGATE_FIKENHILD);
		makeImportant(probe1, "$gaFC_probe", Stage.FOLLOW_THE_EXPERIMENTS);
		makeImportant(probe2, "$gaFC_probe", Stage.FOLLOW_THE_EXPERIMENTS);
		makeImportant(probe3, "$gaFC_probe", Stage.FOLLOW_THE_EXPERIMENTS);
		//makeImportant(probe4, "$gaFC_probe", Stage.FOLLOW_THE_EXPERIMENTS);
		
		
		// I'm not sure I want to indicate where the 'clues' in Isirah are. -dgb
		// Update; but I DO want to flag the Isirah system. Maybe I'll just give Laicaille. -dgb
		makeImportant(laicailleArchon, "$gaFC_clue", Stage.SEARCH_ISIRAH);
		//makeImportant(kapteyn.getAdmin(), "$gaFC_clue", Stage.SEARCH_ISIRAH);
		//makeImportant(groombridge, "$gaFC_clue", Stage.SEARCH_ISIRAH);

		makeImportant(laicailleArchon, "$gaFC_confront", Stage.CONFRONT_ARCHON);
		makeImportant(laicaille, "$gaFC_safehouse", Stage.VISIT_COUREUSE);
		makeImportant(baird.getMarket(), "$gaFC_returnHere", Stage.RETURN_TO_ACADEMY);
		
		//setStageOnGlobalFlag(Stage.CHOOSE_PATH, "$gaFC_choosePath");
		setStageOnGlobalFlag(Stage.INVESTIGATE_FIKENHILD, "$gaFC_pickedBranchFikenhild");
		setStageOnGlobalFlag(Stage.FOLLOW_THE_EXPERIMENTS, "$gaFC_pickedBranchProbes");
		setStageOnGlobalFlag(Stage.SEARCH_ISIRAH, "$gaFC_searchIsirah");
		setStageOnGlobalFlag(Stage.CONFRONT_ARCHON, "$gaFC_confrontArchon");
		setStageOnGlobalFlag(Stage.VISIT_COUREUSE, "$gaFC_visitCoureuse");
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, "$gaFC_returnToAcademy");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaFC_completed");
		
		// after she's moved to GA
		beginStageTrigger(Stage.RETURN_TO_ACADEMY);
		triggerUnhideCommListing(coureuse);
		endTrigger();
		
		beginStageTrigger(Stage.RETURN_TO_ACADEMY);
		triggerHideCommListing(coureuse);
		endTrigger();

		float baseDelay = genDelay(14f); // 3f; // 90f; // randomize this a bit via genDelay()
		beginStageTrigger(Stage.COMPLETED);
		triggerRunScriptAfterDelay(genDelay(baseDelay), new GAFCReplaceArchon());
		//triggerSetGlobalMemoryValueAfterDelay(genDelay(2f), "$gaFC_missionCompleted", true);
		triggerSetGlobalMemoryValue("$gaFC_missionCompleted", true);
		triggerMakeNonStoryCritical(coureuse.getMarket(), arroyo.getMarket(), 
									siyavong.getMarket(), zal.getMarket(),
									laicailleArchon.getMarket(), kapteynAgent.getMarket());
		endTrigger();
		
		setSystemWasUsedForStory(Stage.CHOOSE_PATH, probeSystem);
		
		return true;
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								Map<String, MemoryAPI> memoryMap) {

		if ("postFikenhildCleanup".equals(action)) {
			// $global. is not needed here (and in fact will not work) since
			// Global.getSector().getMemoryWithoutUpdate() returns the global memory already -Alex
			Global.getSector().getMemoryWithoutUpdate().unset("$gaFC_triedToSeeCavin");
			Global.getSector().getMemoryWithoutUpdate().unset("$gaFC_beingConspicuous");
			Global.getSector().getMemoryWithoutUpdate().unset("$gaFC_triedToSeeCavin");
			Global.getSector().getMemoryWithoutUpdate().unset("$gaFC_knowSiyavongContact"); 
			Global.getSector().getMemoryWithoutUpdate().unset("$gaFC_knockedAnyway");
			Global.getSector().getMemoryWithoutUpdate().unset("$gaFC_madeSiyavongAngry");
			
			//$global.gaFC_knowElissasName // maybe keep this one around for meeting Zal in the Gates arc
			//$global.gaFC_gotZalContactFromCavin // maybe use when meeting Coureuse
			
			MarketAPI fikenhild = Global.getSector().getEconomy().getMarket("fikenhild");
			if (fikenhild != null) {
				fikenhild.getMemoryWithoutUpdate().unset("$visitedA");
				fikenhild.getMemoryWithoutUpdate().unset("$visitedB");
				fikenhild.getMemoryWithoutUpdate().unset("$visitedC");
			}

			return true;
		}
		if ("dropStolenProbe".equals(action)) {
			//SectorEntityToken probe = system.addCustomEntity(null, 
			//"Probe name or null if it's in custom_entities", 
			//"<entity type id from custom entities>, Factions.NEUTRAL);
			//probe.setLocation(scavenger.getLocation().x, scavenger.getLocation().y); // with some extra offset if needed etc
			
			SectorEntityToken scavenger = getEntityFromGlobal("$gaFC_probeScavenger");
			//LocationAPI dropLocation = scavenger.getContainingLocation();
			SectorEntityToken probe4 = probeSystem.addCustomEntity(null, "Ejected Cargo Pod", Entities.CARGO_POD_SPECIAL, Factions.NEUTRAL);
			probe4.setLocation(scavenger.getLocation().x, scavenger.getLocation().y); // redundant?
			probe4.addTag("gaFC_lootedProbe"); //unused?
			Misc.makeImportant(probe4, getMissionId());
			
			// get rid of the highlight on the empty probe
			// Yes, the player doesn't *know* it's empty, but this saves time and bother.
			//Misc.makeUnimportant(probeEmpty, getMissionId());
			
			// it was getting re-flagged "important" when updateData etc was called since
			// it was still noted down that it should be important during the current stage
			// this method call cleans that out
			makeUnimportant(scavenger);
			makeUnimportant(probeEmpty);
			
			return true;
		}
		if ("foundEmptyProbe".equals(action)) {
			// found empty probe, so now player suspects the scavenger of taking it -dgb
			SectorEntityToken scavenger = getEntityFromGlobal("$gaFC_probeScavenger");
			Misc.makeImportant(scavenger, getMissionId());
			
			return true;
		}
		if (dialog != null && action.equals("showKapteynBarAgent")) {
			showPersonInfo(kapteynAgent, dialog, false, false);
			return true;
		}
		if ("soldOutIsirahLead".equals(action)) {
			// 3 should be good -dgb
			spawnTriTachInvestigators();
			spawnTriTachInvestigators();
			spawnTriTachInvestigators();
			return true;
		}

		return false;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaFC_stage", getCurrentStage());
		set("$gaFC_starName", probeSystem.getNameWithNoType());
		set("$gaFC_siyavong", siyavong);
		set("$gaFC_bribeCost", Misc.getWithDGS(bribeCost));
		set("$gaFC_kapteynBribeCost", Misc.getWithDGS(kapteynBribeCost));
		set("$gaFC_kapteynBarBribeCost", Misc.getWithDGS(kapteynBarBribeCost));
		set("$gaFC_sellOutPrice", Misc.getWithDGS(sellOutPrice));
		//set("$gaFC_laicailleArchon", laicailleArchon);
		
		set("$gaFC_KBAheOrShe", kapteynAgent.getHeOrShe());
		set("$gaFC_KBAHeOrShe", kapteynAgent.getHeOrShe().substring(0, 1).toUpperCase() + kapteynAgent.getHeOrShe().substring(1));
		set("$gaFC_KBAhisOrHer", kapteynAgent.getHisOrHer());
		set("$gaFC_KBAHisOrHet", kapteynAgent.getHisOrHer().substring(0, 1).toUpperCase() + kapteynAgent.getHisOrHer().substring(1));
		set("$gaFC_KBAhimOrHer", kapteynAgent.getHimOrHer());
		set("$gaFC_KBAHimOrHet", kapteynAgent.getHimOrHer().substring(0, 1).toUpperCase() + kapteynAgent.getHimOrHer().substring(1));

	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		//Color h = Misc.getHighlightColor();
		FactionAPI heg = Global.getSector().getFaction(Factions.HEGEMONY);
		
		if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("You've found Academician Scylla Coureuse and brought her out of hiding to return her to"
					+ " work on Baird's secret project at the Galatia Academy.",opad);
		} else {
			info.addPara("Find Scylla Coureuse, a former academician of the Galatia Academy who went into hiding "
					+ "after the Hegemony crackdown on hyperspace experimentation.", opad, heg.getBaseUIColor(), "Hegemony");
		}
		
		if (currentStage == Stage.INVESTIGATE_FIKENHILD) {
			info.addPara("Talk to contacts on Fikenhild associated with Scylla Coureuse to find a lead to her current location -"
					+ " or attract the attention of someone who knows where she is.", opad);
			//info.addPara(getGoTalkToPersonText(arroyo) + ". He has a relationship with Provost Baird that "
			//		+ "can be leveraged to compel his cooperation.", opad);
		} else if (currentStage == Stage.FOLLOW_THE_EXPERIMENTS) {
			info.addPara(getGoToSystemTextShort(probeSystem) + " and search for the experimental packages possibly being used by Coureuse.",opad);
			//info.addPara("Visit " + probe_system.getNameString() + " in person, at his planetside chalet.", opad);
		} else if (currentStage == Stage.SEARCH_ISIRAH) {
			info.addPara("Search the Isirah system for Scylla Coureuse. Talk to people who might be involved in hiding "
					+ "her and search for signs of her research.", opad);
		} else if (currentStage == Stage.CONFRONT_ARCHON) {
			info.addPara("Confront the archon of Laicaille Habitat about hiding Scylla Coureuse on the station.", opad);
		} else if (currentStage == Stage.VISIT_COUREUSE) {
			info.addPara("Visit the safehouse of Scylla Coureuse and convince her to come back to the Galatia Academy.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and talk to Provost Baird.", opad);			
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.CHOOSE_PATH) {
			// this text doesn't seem necessary here - maybe something simpler?
			//info.addPara(getGoTalkToPersonText(baird), tc, pad);
			info.addPara("Find former academician Scylla Coureuse", tc, pad);
			return true;
		} else if (currentStage == Stage.INVESTIGATE_FIKENHILD) {
			
			info.addPara("Talk to associates of Scylla Coureuse on Fikenhild to find a lead to her location"
					+ " or attract attention from someone who knows where she is", tc, pad);
			return true;
		} else if (currentStage == Stage.FOLLOW_THE_EXPERIMENTS) {
			info.addPara(getGoToSystemTextShort(probeSystem) + 
					" and search for the experimental packages possibly being used by Coureuse", tc, pad);
			return true;
		} else if (currentStage == Stage.SEARCH_ISIRAH) {
			info.addPara("Search the Isirah system for Scylla Coureuse, talk to people who might be hiding "
					+ "her, and search for signs of her research.", tc, pad);
			//info.addPara(getGoToMarketText(loke.getMarket()) + " and pick up the hack device", tc, pad);
			return true;
		} else if (currentStage == Stage.CONFRONT_ARCHON) {
			info.addPara("Confront the archon of Laicaille Habitat about hiding Scylla Coureuse on the station", tc, pad);
			//info.addPara(getGoToSystemTextShort(relay.getStarSystem()) + " and install hack transmitter " +
			//		"on " + relay.getName() + "", tc, pad);
			return true;
		} else if (currentStage == Stage.VISIT_COUREUSE) {
			info.addPara("Go to the safehouse of Scylla Coureuse and convince her to return to the Galatia Academy", tc, pad);
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
	
	protected void addProbeScavengerFleet() 
	{
		// Near the star? Okay, hope this works.
		//SectorEntityToken fleetLocation = probeSystem.getStar();
		
		// no reason for the scavenger fleet to exist unless the player is nearby
		beginWithinHyperspaceRangeTrigger(probeSystem, 3f, false, Stage.FOLLOW_THE_EXPERIMENTS);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.LOWER, Factions.SCAVENGERS, FleetTypes.SCAVENGER_MEDIUM, probeSystem);
		triggerSetFleetOfficers(OfficerNum.FEWER, OfficerQuality.LOWER);
		triggerSetFleetFaction(Factions.INDEPENDENT);
		triggerMakeLowRepImpact();
		triggerFleetSetAvoidPlayerSlowly();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
		//triggerPickLocationAtClosestToEntityJumpPoint(probeSystem, fleetLocation);
		triggerPickLocationAtInSystemJumpPoint(probeSystem); // so it's not always the one closest to the star...
		triggerSetEntityToPickedJumpPoint();
		triggerPickLocationAroundEntity(1500);
		triggerSpawnFleetAtPickedLocation("$gaFCProbe_scavengerPermanentFlag", null);
		triggerFleetSetTravelActionText("exploring system");
		triggerFleetSetPatrolActionText("searching for salvage");
		triggerOrderFleetPatrolEntity(false);
		triggerFleetAddDefeatTrigger("gaFCScavengerDefeated");
		triggerSaveGlobalFleetRef("$gaFC_probeScavenger"); 
		// only becomes "important" when player finds empty probe
		//triggerFleetMakeImportant(null, Stage.SEARCH_ISIRAH);
		endTrigger();
	}
	
	
	protected void addPatherAmbushFleet()
	{
		//SectorEntityToken location = probeSystem.getStar();
		beginGlobalFlagTrigger("$gaFC_triggerPatherAmbush", Stage.FOLLOW_THE_EXPERIMENTS);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.VERY_LOW, Factions.LUDDIC_PATH, FleetTypes.PATROL_SMALL, probeSystem);
		//triggerMakeNonHostile(); // should it be hostile?
		triggerMakeHostileAndAggressive();
		triggerMakeLowRepImpact();
		triggerFleetPatherNoDefaultTithe();
		triggerPickLocationAtClosestToPlayerJumpPoint(probeSystem);
		triggerSetEntityToPickedJumpPoint();
		triggerFleetSetPatrolActionText("waiting");
		triggerPickLocationTowardsEntity(null, 15f, getUnits(1.0f)); // towards the jump-point we just picked
		triggerSpawnFleetAtPickedLocation("$gaFC_patherProbeAmbush", null);
		triggerSetFleetMissionRef("$gaFC_ref"); // so they can be made unimportant
		triggerFleetMakeImportant(null, Stage.FOLLOW_THE_EXPERIMENTS);
		triggerOrderFleetInterceptPlayer();
		//triggerFleetAddDefeatTrigger("gaFCPatherAmbushDefeated");
		endTrigger();
	}
	
	protected void spawnTriTachMercFleet()
	{
		// Doesn't matter which path player took previously - just spawn near Isirah to enhance "Fun".
		StarSystemAPI isirah = Global.getSector().getStarSystem("isirah");
		beginWithinHyperspaceRangeTrigger(isirah, 3f, true, Stage.SEARCH_ISIRAH);
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.HIGHER, Factions.MERCENARY, FleetTypes.MERC_PRIVATEER, isirah);
		triggerSetFleetFaction(Factions.INDEPENDENT);
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		//triggerAutoAdjustFleetStrengthMajor();
		//triggerMakeHostileAndAggressive(); //
		triggerMakeNonHostile();
		//triggerMakeNoRepImpact();
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		//triggerMakeLowRepImpact();
		triggerPickLocationTowardsPlayer(isirah.getHyperspaceAnchor(), 90f, getUnits(1.5f));
		triggerSpawnFleetAtPickedLocation("$gaFC_isirahMerc", null);
		triggerOrderFleetInterceptPlayer();
		triggerSetFleetMissionRef("$gaFC_ref"); // so they can be made unimportant
		triggerFleetMakeImportant(null, Stage.SEARCH_ISIRAH);
		// ^ was CONFRONT_ARCHON - but should come a stage sooner to intercept player before reaching Isirah system
		endTrigger();
	}
	
	protected void spawnTriTachInvestigators()
	{
		// if you sold out Coureuse, put a few random Tri-Tachyon fleets in Isirah system
		// they'll hang around 'til the end of the mission arc
		// patrol around Isirah system looking generally suspicious. -dgb
		
		StarSystemAPI isirah = Global.getSector().getStarSystem("isirah");
		// false just so when ctrl-click into isirah they spawn anyway
		beginWithinHyperspaceRangeTrigger(isirah, 3f, false, Stage.SEARCH_ISIRAH);
		triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.HIGHER, Factions.TRITACHYON, FleetTypes.MERC_BOUNTY_HUNTER, isirah);
		triggerSetFleetFaction(Factions.TRITACHYON);
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		
		
		triggerFleetSetTravelActionText("traveling"); // this gets shown when they're nearing the system in hyper
		triggerFleetSetPatrolActionText("searching system");
		triggerPickLocationTowardsPlayer(isirah.getHyperspaceAnchor(), 90f, getUnits(1.5f));
		triggerSpawnFleetAtPickedLocation("$gaFC_isirahTriTach", null);
		//triggerOrderFleetInterceptPlayer();
		//triggerFleetMakeImportant(null, Stage.CONFRONT_ARCHON);
		
		triggerOrderFleetPatrol(isirah, true, Tags.OBJECTIVE, Tags.PLANET); // not STATION, otherwise they murder the pirate station -dgb
		triggerOrderFleetPatrol();
		endTrigger();
	}
}





