package com.fs.starfarer.api.impl.campaign.missions.luddic;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class BornanewFilesFalseIdols extends HubMissionWithSearch {

	public static enum Stage {
		PICK_UP_BORNANEW,
		MEET_MENES_YARIBAY,
		ATTEND_A_PARTY,
		TALK_TO_HORUS, 
		TALK_TO_ENGINEER,
		TALK_TO_ULMUS_POND,
		GO_TO_CHALCEDON,
		INVESTIGATE_PATHER_STATION,
		RETURN_TO_HESPERUS_EARLY,
		GO_TO_HESPERUS,
		ARREST_THE_CURATE,
		DELIVER_KEEPFAITH,
		COMPLETED,
	}
	
	protected PersonAPI bornanew;
	protected PersonAPI jaspis;  
	protected PersonAPI ulmus_pond;
	protected PersonAPI sedge;  
	protected PersonAPI menes_yaribay;
	protected PersonAPI horus_yaribay;
	protected PersonAPI cedra_keepfaith;  
	
	protected MarketAPI asher;
	protected MarketAPI chalcedon;
	protected MarketAPI olinadu;
	protected MarketAPI kazeron;
	protected MarketAPI gilead;
	protected MarketAPI hesperus;
	protected MarketAPI tartessus;
	protected MarketAPI bornanewLocation;
	
	protected StarSystemAPI patherBaseSystem;
	
	protected LuddicPathBaseIntel patherStation;
	protected CampaignFleetAPI patherStationFleet;
	
	protected int stationSalvorLoss;
	
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$bffi_ref", "$bffi_inProgress")) {
			return false;
		}
		
		// Places
		chalcedon = Global.getSector().getEconomy().getMarket("chalcedon");
		if (chalcedon == null) return false;
		if (!chalcedon.getFactionId().equals(Factions.LUDDIC_PATH)) return false;
		
		gilead = Global.getSector().getEconomy().getMarket("gilead");
		if (gilead == null) return false;
		if (!gilead.getFactionId().equals(Factions.LUDDIC_CHURCH)) return false;
		
		kazeron = Global.getSector().getEconomy().getMarket("kazeron");
		if (kazeron == null) return false;
		if (!kazeron.getFactionId().equals(Factions.PERSEAN)) return false;
		
		olinadu = Global.getSector().getEconomy().getMarket("olinadu");
		if (olinadu == null) return false;
		if (!olinadu.getFactionId().equals(Factions.PERSEAN)) return false;
		
		hesperus = Global.getSector().getEconomy().getMarket("hesperus");
		if (hesperus == null) return false;
		if (!hesperus.getFactionId().equals(Factions.LUDDIC_CHURCH)) return false;
		
		tartessus = Global.getSector().getEconomy().getMarket("tartessus");
		if (tartessus == null) return false;
		if (!tartessus.getFactionId().equals(Factions.LUDDIC_CHURCH)) return false;
		
		asher = Global.getSector().getEconomy().getMarket("asher");
		if (asher == null) return false;
		if (!asher.getFactionId().equals(Factions.LUDDIC_CHURCH)) return false;
		
		
		// People
		
		bornanew = getImportantPerson(People.BORNANEW);
		if (bornanew == null) return false;

		jaspis = getImportantPerson(People.JASPIS);
		if (bornanew == null) return false;
		
		ulmus_pond = getImportantPerson(People.ULMUS_POND);
		if (ulmus_pond == null) return false;
		
		menes_yaribay = getImportantPerson(People.MENES_YARIBAY);
		if (menes_yaribay == null) return false;
		
		horus_yaribay = getImportantPerson(People.HORUS_YARIBAY);
		if (horus_yaribay == null) return false;
		
		cedra_keepfaith = getImportantPerson(People.CEDRA_KEEPFAITH);
		if (cedra_keepfaith == null) return false;
		
		
		// Find a system to hide Pather base in
		resetSearch();
		requireSystemTags(ReqMode.ANY, Tags.THEME_MISC, Tags.THEME_MISC_SKIP, Tags.THEME_RUINS);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
		requireSystemNotAlreadyUsedForStory();
		requireSystemNotHasPulsar(); // don't want the challenge to get blown up.
		preferSystemOnFringeOfSector();
		preferSystemUnexplored();
		preferSystemInDirectionOfOtherMissions(); 
		patherBaseSystem = pickSystem();
		if (patherBaseSystem == null) return false;

		setStoryMission();
		
		setStartingStage(Stage.PICK_UP_BORNANEW);
		connectWithGlobalFlag(Stage.PICK_UP_BORNANEW, Stage.MEET_MENES_YARIBAY, "$bffi_goMeetMenesYaribay");
		setStageOnGlobalFlag(Stage.ATTEND_A_PARTY, "$bffi_gotPartyInvite");
		setStageOnGlobalFlag(Stage.TALK_TO_HORUS, "$bffi_goTalkToHorus");
		setStageOnGlobalFlag(Stage.TALK_TO_ENGINEER, "$bffi_talkToEngineer");
		setStageOnGlobalFlag(Stage.TALK_TO_ULMUS_POND, "$bffi_talkToUlmusPond");
		setStageOnGlobalFlag(Stage.GO_TO_CHALCEDON, "$bffi_followUlmusPond");
		setStageOnGlobalFlag(Stage.INVESTIGATE_PATHER_STATION, "$bffi_investigatePatherStation");
		
		setStageOnGlobalFlag(Stage.ARREST_THE_CURATE, "$bffi_arrestTheCurate"); // for testing.
		
		setStageOnGlobalFlag(Stage.RETURN_TO_HESPERUS_EARLY, "$bffi_destroyedStationEarly"); // half-failure.
		
		setStageOnGlobalFlag(Stage.GO_TO_HESPERUS, "$bffi_returnBornanewBody"); // definitely a failure
		setStageOnGlobalFlag(Stage.GO_TO_HESPERUS, "$bffi_keepfaithEscapedTartessus"); // half-failure.
		
		setStageOnGlobalFlag(Stage.ARREST_THE_CURATE, "$bffi_learnedAboutKeepfaith");
		setStageOnGlobalFlag(Stage.DELIVER_KEEPFAITH, "$bffi_arrestedKeepfaith");
		
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$bffi_completed");
		addSuccessStages(Stage.COMPLETED);	
		
		makeImportant(bornanew, "$bffi_pickUpBornanew", Stage.PICK_UP_BORNANEW);
		makeImportant(menes_yaribay, "$bffi_meetMenesYaribay", Stage.MEET_MENES_YARIBAY);
		makeImportant(olinadu, "$bffi_attendAParty", Stage.ATTEND_A_PARTY);
		makeImportant(horus_yaribay, "$bffi_talkToHorusAboutMenesParty", Stage.TALK_TO_HORUS);
		
		makeImportant(asher, "$bffi_talkToEngineer", Stage.TALK_TO_ENGINEER);
		
		makeImportant(ulmus_pond, "$bffi_talkToUlmusPond", Stage.TALK_TO_ULMUS_POND);
		makeImportant(chalcedon, "$bffi_followUlmusPond", Stage.GO_TO_CHALCEDON);
		
		makeImportant(tartessus, "$bffi_arrestTheCurate", Stage.ARREST_THE_CURATE);
		makeImportant(hesperus, "$bffi_deliverKeepfaith", Stage.DELIVER_KEEPFAITH);
		
		makeImportant(hesperus, "$bffi_goToHesperus", Stage.GO_TO_HESPERUS);
		makeImportant(hesperus, "$bffi_goToHesperus", Stage.RETURN_TO_HESPERUS_EARLY);

		setName("False Idols");
		setRepFactionChangesNone();
		setRepPersonChangesNone();
	
		// Spawn a PL patrol or two to spice up things around Olinadu pre Menes' party
		beginStageTrigger(Stage.ATTEND_A_PARTY);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.DEFAULT, Factions.PERSEAN, FleetTypes.PATROL_MEDIUM, olinadu.getPlanetEntity());
		triggerPickLocationAroundEntity(olinadu.getPlanetEntity(), 1600f);
		triggerSpawnFleetAtPickedLocation("$gaDHO_arrayFleet", null);
		triggerOrderFleetPatrol(false, olinadu.getPlanetEntity(), olinadu.getStarSystem().getJumpPoints().get(0));
		triggerSetFleetMissionRef("$bffi_ref");
		endTrigger();
		
		// And a couple PL patrols for after the party.
		beginStageTrigger(Stage.TALK_TO_ENGINEER);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.DEFAULT, Factions.PERSEAN, FleetTypes.PATROL_MEDIUM, olinadu.getPlanetEntity());
		triggerFleetAllowLongPursuit();
		triggerPickLocationAroundEntity(olinadu.getPlanetEntity(), 1000f);
		triggerSetFleetMissionRef("$bffi_ref");
		triggerSpawnFleetAtPickedLocation("$bffi_postRaidPatrol", null);
		//triggerOrderFleetPatrol(false, olinadu.getPlanetEntity(), olinadu.getStarSystem().getJumpPoints().get(0), olinadu.getStarSystem().getJumpPoints().get(1));
		//triggerOrderFleetInterceptPlayer(); // Could write a unique interaction, but I'd want it to have consequences... -dgb
		endTrigger();
		
		beginStageTrigger(Stage.TALK_TO_ENGINEER);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.DEFAULT, Factions.PERSEAN, FleetTypes.PATROL_MEDIUM, olinadu.getPlanetEntity());
		triggerSetFleetFaction(Factions.PERSEAN);
		//triggerAutoAdjustFleetStrengthMajor();
		triggerPickLocationTowardsEntity(olinadu.getStarSystem().getHyperspaceAnchor(), 30f, getUnits(1.5f));
		//triggerFleetAllowLongPursuit();
		triggerSetFleetMissionRef("$bffi_ref");
		triggerSpawnFleetAtPickedLocation("$bffi_postRaidPatrol", null);
		triggerOrderFleetPatrol(false, olinadu.getPlanetEntity(), olinadu.getStarSystem().getJumpPoints().get(1));
		//triggerOrderFleetInterceptPlayer(); // Could write a unique interaction, but I'd want it to have consequences... -dgb
		endTrigger();
		
		
		// Spawn a Pather fleet near Chalcedon after Pond tips them off (inadvertantly or not)
		beginWithinHyperspaceRangeTrigger(chalcedon.getPlanetEntity(), 1f, false,Stage.GO_TO_CHALCEDON);
		triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.DEFAULT, Factions.LUDDIC_PATH, FleetTypes.PATROL_MEDIUM, chalcedon.getPlanetEntity());
		triggerSetFleetFaction(Factions.LUDDIC_PATH);
        triggerPickLocationAroundEntity(chalcedon.getPlanetEntity(), 800f);
        triggerOrderFleetPatrol(chalcedon.getPlanetEntity());
        triggerSpawnFleetAtPickedLocation("$bffi_patherGoblins", null);
        triggerSetFleetMissionRef("$bffi_ref");
        
        // if player is hostile to Path, Path fleet is hostile to player.
        if( Global.getSector().getFaction(Factions.LUDDIC_PATH).getRelToPlayer().isAtBest(RepLevel.HOSTILE)){
        	triggerMakeHostileAndAggressive();
        }
        
        triggerFleetSetPatrolLeashRange(1400f);
        triggerMakeFleetGoAwayAfterDefeat();
        endTrigger();
        
		// A *serious* Luddic intercept fleet post-Chalcedon
        // Revenge for killing Sedge and/or the same but stopping player's meddling.
        // Maybe they demand to talk to Bornanew?
		beginWithinHyperspaceRangeTrigger(chalcedon, 3f, true, Stage.INVESTIGATE_PATHER_STATION);
		triggerCreateFleet(FleetSize.LARGER, FleetQuality.HIGHER, Factions.LUDDIC_PATH, FleetTypes.PATROL_LARGE, chalcedon.getLocationInHyperspace());
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerMakeHostileAndAggressive();
		triggerFleetMakeFaster(true, 2, true);
		triggerSetFleetAlwaysPursue();
		triggerPickLocationTowardsEntity(chalcedon.getStarSystem().getHyperspaceAnchor(), 30f, getUnits(1.5f));
		triggerSpawnFleetAtPickedLocation("$bffi_patherIntercept", null);
		triggerOrderFleetInterceptPlayer();
		triggerOrderFleetEBurn(1f);
		triggerSetFleetMissionRef("$bffi_ref");
		triggerFleetMakeImportant(null, Stage.INVESTIGATE_PATHER_STATION);
		endTrigger();
		
		beginStageTrigger(Stage.COMPLETED);
		triggerMakeNonStoryCritical("asher", "chalcedon", "olinadu", "kazeron", "gilead", "hesperus", "tartessus");
		triggerSetGlobalMemoryValue("$bffi_missionCompleted", true);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
			set("$bffi_stage", getCurrentStage());
			set("$bffi_patherStationSystem", patherBaseSystem.getName());
	}

	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		
		if ("spawnPatherBase".equals(action)) {
			//new LuddicPathBaseIntel(patherBaseSystem,  Factions.LUDDIC_PATH);
			
			patherStation = new LuddicPathBaseIntel(patherBaseSystem, Factions.LUDDIC_PATH);
			
			
			/* Don't need to actually simulate all of this.
			patherStation.getMarket().addIndustry("heavyindustry");
			MarketAPI patherMarket = patherStation.getMarket();
			String itemId = "corrupted_nanoforge";
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(itemId);
			Industry ind = patherMarket.getIndustry("heavyindustry");
			ind.setSpecialItem(new SpecialItemData(itemId, null));
			*/		
			
			MemoryAPI mem = patherStation.getEntity().getMemoryWithoutUpdate();
			mem.set("$bffi_patherStationTarget", true);
			Global.getSector().addScript(patherStation);
			
			CampaignFleetAPI patherStationFleet = Misc.getStationFleet(patherStation.getMarket());
			
			//makeImportant(patherStation, , Stage.INVESTIGATE_PATHER_STATION);
			Misc.makeImportant(patherStation.getEntity(), "$bffi_investigatePatherStation");
			Misc.addDefeatTrigger(patherStationFleet, "BFFIpatherStationDefeated");
			
			/*patherStationFleet.addEventListener(new BaseFleetEventListener() {
				@Override
				public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
					if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
						// TODO something.
						//fleet.getMemoryWithoutUpdate().unset("$gaATG_hegScanFleet");
						//encounterGateHegemony.getMemoryWithoutUpdate().unset("$GAATGhegemonyScanFleet");
					}
				}
			});*/
			return true;
		}
		else if ("cleanUpPatherBase".equals(action)) {
		
			Misc.makeUnimportant(patherStation.getEntity(), "$bffi_investigatePatherStation"); 
			// Is this implicit?
			return true;
		}
		else if ("patherBaseLosses".equals(action))
		{
			// ui_cargo_special_tech_drop
			Global.getSoundPlayer().playSound("explosion_from_damage", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			return true;
		}
		else if ("shootEm".equals(action))
		{
			Global.getSoundPlayer().playSound("storyevent_diktat_execution", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			return true;
		}
		else if ("ulmusPondMaskOff".equals(action)) { // unused, actually.
			ulmus_pond.setFaction("luddic_path"); // Whaaaaa????
			return true;
		}
		else if ("setCourseChalcedon".equals(action)) {
			Global.getSector().layInCourseFor(chalcedon.getPlanetEntity());
			return true;
		}
		/*else if ("transponderOff".equals(action)) {
			Global.getSector().getPlayerFleet().getAbility(Abilities.TRANSPONDER).deactivate();
			return true;
		}
		else if ("transponderOn".equals(action)) {
			Global.getSector().getPlayerFleet().getAbility(Abilities.TRANSPONDER).activate();
			return true;
		}*/
		else if ("doCleanup".equals(action))
		{
			Global.getSector().getMemoryWithoutUpdate().unset("$bffi_jethroCalledOutPond");
			//Global.getSector().getMemoryWithoutUpdate().unset("$bffi_intendToTalkHorus");		
			// Jeez, anything else?
			return true;
		}
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}
	
//		if ("THEDUEL".equals(action)) {
//			TextPanelAPI text = dialog.getTextPanel();
//			text.setFontOrbitronUnnecessarilyLarge();
//			Color color = Misc.getBasePlayerColor();
//			color = Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();
//			text.addPara("THE DUEL", color);
//			text.setFontInsignia();
//			text.addImage("misc", "THEDUEL");
//			return true;
//		}
/*

		else if ("didMazalotRaid".equals(action))
		{
			RecentUnrest.get(mazalot).add(10, "Raided Mazalot and caused a Luddic uprising");
			return true;
		}*/
		
	
	/*
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		FactionAPI faction = getFactionForUIColors();
		PersonAPI person = getPerson();
		
		//info.addImage(Global.getSettings().getSpriteName("illustrations", "luddic_shrine"), width, opad);
		
		addDescriptionForCurrentStage(info, width, height);
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
	}
	*/

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		//Color h = Misc.getHighlightColor();
		
		//Color h2 = Misc.getDarkHighlightColor();
		//FactionAPI church = Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
		
		//info.addImage(robed_man.getPortraitSprite(), width, 128, opad);

		if (currentStage == Stage.PICK_UP_BORNANEW) {
			info.addPara("Pick up Jethro Bornanew from Gilead.", opad);
			addStandardMarketDesc("Go to " + gilead.getOnOrAt(), gilead, info, opad);
		}
		else if (currentStage == Stage.MEET_MENES_YARIBAY) {
			info.addPara("Go to Olinadu and contact Menes Yaribay, the planetary administrator.", opad);
			addStandardMarketDesc("Go to " + olinadu.getOnOrAt(), olinadu, info, opad);
		}
		else if (currentStage == Stage.TALK_TO_HORUS) {
			info.addPara("Go to Kazeron and talk to Horus Yaribay about his cousin Menes.", opad);
			addStandardMarketDesc("Go to " + kazeron.getOnOrAt(), kazeron, info, opad);
		}
		else if (currentStage == Stage.ATTEND_A_PARTY) {
			
			if(Global.getSector().getMemoryWithoutUpdate().contains("$bffi_intendToSteal")) {
				info.addPara("Attend Menes Yaribay's 'gathering' on Olinadu and find a way to steal the false relic.", opad);
				addStandardMarketDesc("Go to " + olinadu.getOnOrAt(), olinadu, info, opad);
			}
			else if(Global.getSector().getMemoryWithoutUpdate().contains("$bffi_horusToParty")) {
				info.addPara("Attend Menes Yaribay's 'gathering' on Olinadu with Horus Yaribay, the patriarch of Gens Yaribay.", opad);
				addStandardMarketDesc("Go to " + olinadu.getOnOrAt(), olinadu, info, opad);
			}
			else {
				info.addPara("Attend Menes Yaribay's 'gathering' on Olinadu and acquire the false relic.", opad);
				addStandardMarketDesc("Go to " + olinadu.getOnOrAt(), olinadu, info, opad);
			}
		}
		else if (currentStage == Stage.TALK_TO_ENGINEER) {
			info.addPara("Bring the false relic to Asher so that a nanoforge engineer in the employ of the Church can examine it.", opad);
			addStandardMarketDesc("Go to " + asher.getOnOrAt(), asher, info, opad);
		}
		else if (currentStage == Stage.TALK_TO_ULMUS_POND) {
			info.addPara("Go to Olinadu and confront Ulmus Pond to get a confession.", opad);
			addStandardMarketDesc("Go to " + asher.getOnOrAt(), asher, info, opad);
		}
		else if (currentStage == Stage.GO_TO_CHALCEDON) {
			info.addPara("Go to Chalcedon and use Bornanew's intel to track down where Ulmus Pond is hiding.", opad);
			addStandardMarketDesc("Go to " + chalcedon.getOnOrAt(), chalcedon, info, opad);
		}
		else if (currentStage == Stage.INVESTIGATE_PATHER_STATION) {
			//info.addPara("Investigate the Pather Station in the " + patherStation.getSystem().getName() + " system and try to find out where their scan of the relic came from.", opad);
			//addStandardMarketDesc("Go to the Pather station " + patherStation.getMarket().getOnOrAt(), patherStation.getMarket(), info, opad);
			info.addPara(getGoToSystemTextShort(patherStation.getMarket().getStarSystem()) + " and investigate. Find where the glove came from and how it was made. Ensure no more will be created.",opad);
		}
		else if (currentStage == Stage.ARREST_THE_CURATE) {
			info.addPara("Go to the Cathedral of Holy Exodus, on Tartessus, and arrest Subcurate Cedra Keepfaith.", opad);
			addStandardMarketDesc("Go to " + tartessus.getOnOrAt(), tartessus, info, opad);
		}
		else if (currentStage == Stage.DELIVER_KEEPFAITH) {
			info.addPara("Deliver Cedra Keepfaith to the Knights of Ludd on Hesperus to face their justice.", opad);
			addStandardMarketDesc("Go to " + hesperus.getOnOrAt(), hesperus, info, opad);
		}
		else if (currentStage == Stage.GO_TO_HESPERUS) {
			if(Global.getSector().getMemoryWithoutUpdate().contains("$bffi_keepfaithEscapedTartessus")){
				info.addPara("Report back to Excubitor Orbis Gideon Oak on Hesperus with news of Bornanew's quest.", opad);
				addStandardMarketDesc("Go to " + hesperus.getOnOrAt(), hesperus, info, opad);
			}
			else { //Global.getSector().getMemoryWithoutUpdate().contains("$bffi_returnBornanewBody"))
				info.addPara("Return Jethro Bornanew's body to the Knights of Ludd on Hesperus so he may rest with his fellow Knights.", opad);
				addStandardMarketDesc("Go to " + hesperus.getOnOrAt(), hesperus, info, opad);
			}
		}
		else if (currentStage == Stage.RETURN_TO_HESPERUS_EARLY) {
			info.addPara("Report back to Excubitor Orbis Gideon Oak on Hesperus with news of Bornanew's quest.", opad);
			addStandardMarketDesc("Go to " + hesperus.getOnOrAt(), hesperus, info, opad);
		}
		
		/*else if (currentStage == Stage.GO_TO_MAZALOT) {
			info.addPara("A Pather, Sedge, claims that Jethro Bornanew travelled to Mazalot. Find him, or find where he has gone from there.", opad);
			addStandardMarketDesc("Go to " + mazalot.getOnOrAt(), mazalot, info, opad);
		}
		else if (currentStage == Stage.CONTACT_VIRENS) {
			info.addPara("Nile Virens runs the Luddic Path on Mazalot. If anyone knows where Bornanew is, it would be him or his organization. He might be persuaded to help by force or diplomacy.", opad);
			addStandardMarketDesc("Contact Nile Virens " + mazalot.getOnOrAt(), mazalot, info, opad);
			info.addImage(nile_virens.getPortraitSprite(), width, 128, opad);
			info.addImage(nile_virens.getFaction().getCrest(), width, 128, opad);
		}
		else if (currentStage == Stage.CONTACT_BORNANEW) {
			info.addPara("Nile Virens has provided you with the location of Jethro Bornanew, or so he claims. This consists of coordinates for a location on the surface of Mazalot, outside of a Luddic-majority settlement.", opad);
			addStandardMarketDesc("Contact Jethro Bornanew " + mazalot.getOnOrAt(), mazalot, info, opad);
			//info.addImage(bornanew.getPortraitSprite(), width, 128, opad);
		}
		else if (currentStage == Stage.RETURN_TO_GILEAD) {
			info.addPara("Return with Jethro Bornanew to the office of Archcurate Jaspis.", opad);
			addStandardMarketDesc("Go to " + gilead.getOnOrAt(), gilead, info, opad);
		}
		else if (currentStage == Stage.RETURN_TO_GILEAD2) {
			info.addPara("Return to the office of Archcurate Jaspis with news of Jethro Bornanew's 'death'.", opad);
			addStandardMarketDesc("Go to " + gilead.getOnOrAt(), gilead, info, opad);
		}*/
		
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		//Color h = Misc.getHighlightColor();
		
		if (currentStage == Stage.PICK_UP_BORNANEW) {
			info.addPara("Send a shuttle down to pick Novice Bornanew up from Gilead", tc, pad);
			return true;
		}
		else if (currentStage == Stage.MEET_MENES_YARIBAY) {
			info.addPara("Go to Olinadu and talk to the administrator, Menes Yaribay", tc, pad);
			return true;
		}
		else if (currentStage == Stage.TALK_TO_HORUS) {
			info.addPara("Go to Kazeron and talk to Horus Yaribay about Menes", tc, pad);
			return true;
		}
		else if (currentStage == Stage.ATTEND_A_PARTY) {
			if(Global.getSector().getMemoryWithoutUpdate().contains("$bffi_intendToSteal")) {
				info.addPara("Attend the 'gathering' organized by Menes Yaribay on Olinadu", tc, pad);
			}
			else {
				info.addPara("Attend the 'gathering' organized by Menes Yaribay on Olinadu", tc, pad);
			}
			
			return true;
		}
		else if (currentStage == Stage.TALK_TO_ENGINEER) {
			info.addPara("Bring the false relic to Asher to be examined by a nanoforge engineer", tc, pad);
			return true;
		}
		else if (currentStage == Stage.TALK_TO_ULMUS_POND) {
			info.addPara("Go to Olinadu and talk to Ulmus Pond", tc, pad);
			return true;
		}
		else if (currentStage == Stage.GO_TO_CHALCEDON) {
			info.addPara("Go to Chalcedon and find Ulmus Pond", tc, pad);
			return true;
		}
		else if (currentStage == Stage.INVESTIGATE_PATHER_STATION) {
			info.addPara("Go to " + patherStation.getSystem().getName() + " and find the source of the 'relic'.", tc, pad);
			return true;
		}
		else if (currentStage == Stage.ARREST_THE_CURATE) {
			info.addPara("Go to the Cathedral of Holy Exodus on Tartessus and arrest Subcurate Cedra Keepfaith", tc, pad);
			return true;
		}
		else if (currentStage == Stage.DELIVER_KEEPFAITH) {
			info.addPara("Bring Cedra Keeptfaith to Hesperus to face the justice of the Knights of Ludd", tc, pad);
			return true;
		}
		else if (currentStage == Stage.GO_TO_HESPERUS) {
			if(Global.getSector().getMemoryWithoutUpdate().contains("$global.bffi_keepfaithEscapedTartessus"))
			{
				info.addPara("Report back to Excubitor Orbis Gideon Oak on Hesperus", tc, pad);
			}
			else { //Global.getSector().getMemoryWithoutUpdate().contains("$bffi_returnBornanewBody"))
				info.addPara("Return Jethro Bornanew's body to the Knights of Ludd on Hesperus", tc, pad);
			}
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_HESPERUS_EARLY) {
			info.addPara("Report back to Excubitor Orbis Gideon Oak on Hesperus", tc, pad);
			return true;
		}
		
		/*
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DROP_OFF) {
			info.addPara("Deliver " + getWithoutArticle(thing) + " to specified location in the " +  
					system.getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		*/
		return false;
	}

	//@Override
	//public String getBaseName() {
	//	return "False Idols";
	//}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (patherStation != null && currentStage == Stage.INVESTIGATE_PATHER_STATION) {
			return patherStation.getEntity();
		}
		return super.getMapLocation(map);
	}
}





