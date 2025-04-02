package com.fs.starfarer.api.impl.campaign.missions.luddic;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class LuddicKnightErrant extends HubMissionWithSearch {

	public static enum Stage {
		GO_TO_CHALCEDON,
		CONTACT_RECRUITER,
		GO_TO_MAZALOT,
		CONTACT_VIRENS,
		CONTACT_BORNANEW,
		RETURN_TO_GILEAD,
		RETURN_TO_GILEAD2,
		COMPLETED,
	}
	
	protected PersonAPI bornanew;
	protected PersonAPI jaspis;  
	protected PersonAPI nile_virens;  
	
	protected MarketAPI chalcedon;
	protected MarketAPI mazalot;
	protected MarketAPI gilead;
	
	//public static float MISSION_DAYS = 120f;
	
	//protected int payment;
	//protected int paymentHigh;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$lke_ref", "$lke_inProgress")) {
			return false;
		}
		
		chalcedon = Global.getSector().getEconomy().getMarket("chalcedon");
		if (chalcedon == null) return false;
		if (!chalcedon.getFactionId().equals(Factions.LUDDIC_PATH)) return false;
		
		gilead = Global.getSector().getEconomy().getMarket("gilead");
		if (gilead == null) return false;
		if (!gilead.getFactionId().equals(Factions.LUDDIC_CHURCH)) return false;
		
		mazalot = Global.getSector().getEconomy().getMarket("mazalot");
		if (mazalot == null) return false;
		if (!mazalot.getFactionId().equals(Factions.PERSEAN)) return false;
		
		bornanew = getImportantPerson(People.BORNANEW);
		if (bornanew == null) return false;
		
		jaspis = getImportantPerson(People.JASPIS);
		if (bornanew == null) return false;
		
		nile_virens = getImportantPerson(People.VIRENS);
		if (nile_virens == null) return false;
		
		setStoryMission();
		
		setStartingStage(Stage.GO_TO_CHALCEDON);
		makeImportant(chalcedon, "$lke_searchForBornanew", Stage.GO_TO_CHALCEDON);
		
		setStageOnGlobalFlag(Stage.CONTACT_RECRUITER, "$lke_contactRecruiter");
		makeImportant(chalcedon, "$lke_contactRecruiter", Stage.CONTACT_RECRUITER);
		
		setStageOnGlobalFlag(Stage.GO_TO_MAZALOT, "$lke_gotBornanewLead");
		makeImportant(mazalot, "$lke_searchForBornanew2", Stage.GO_TO_MAZALOT);
		
		setStageOnGlobalFlag(Stage.CONTACT_VIRENS, "$lke_contactVirens");
		makeImportant(mazalot, "$lke_contactVirens", Stage.CONTACT_VIRENS);
		
		setStageOnGlobalFlag(Stage.CONTACT_BORNANEW, "$lke_contactBornanew");
		makeImportant(mazalot, "$lke_contactBornanew", Stage.CONTACT_BORNANEW);
		
		setStageOnGlobalFlag(Stage.RETURN_TO_GILEAD, "$lke_foundBornanew"); // he's alive
		makeImportant(gilead, "$lke_returnWithBornanew", Stage.RETURN_TO_GILEAD);
		makeImportant(jaspis, "$lke_returnWithBornanew", Stage.RETURN_TO_GILEAD);
		
		setStageOnGlobalFlag(Stage.RETURN_TO_GILEAD2, "$lke_foundBornanew2"); // he's "dead"
		makeImportant(gilead, "$lke_returnWithBornanewNews", Stage.RETURN_TO_GILEAD2);
		makeImportant(jaspis, "$lke_returnWithBornanewNews", Stage.RETURN_TO_GILEAD2);
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$lke_completed");
		addSuccessStages(Stage.COMPLETED);
		

		setName("Knight Errant");
		setRepFactionChangesNone();
		setRepPersonChangesNone();
	
		beginStageTrigger(Stage.COMPLETED);
		triggerMakeNonStoryCritical("chalcedon", "mazalot", "gilead");
		triggerSetGlobalMemoryValue("$lke_missionCompleted", true);
		endTrigger();
		
		
		// Spawn a Pather fleet near Chalcedon to spice things up.
		//beginStageTrigger(Stage.GO_TO_CHALCEDON);	
		 beginWithinHyperspaceRangeTrigger(chalcedon.getPlanetEntity(), 1f, false,Stage.GO_TO_CHALCEDON);
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.LUDDIC_PATH, FleetTypes.PATROL_LARGE, chalcedon.getPlanetEntity());
		triggerSetFleetFaction(Factions.LUDDIC_PATH);
        triggerPickLocationAroundEntity(chalcedon.getPlanetEntity(), 800f);
        triggerOrderFleetPatrol(chalcedon.getPlanetEntity());
        triggerSpawnFleetAtPickedLocation("$lke_patherGoblins", null);
        triggerSetFleetMissionRef("$lke_ref");
        
        // if player is hostile to Path, Path fleet is hostile to player.
       // if( Global.getSector().getFaction(Factions.LUDDIC_PATH).getRelToPlayer().isAtBest(RepLevel.HOSTILE)  )
        //{
        //	triggerMakeHostileAndAggressive();
        //}
        
        //triggerFleetSetPatrolLeashRange(1000f);
        //triggerMakeFleetGoAwayAfterDefeat();
        endTrigger();

		// Luddic intercept fleet post-Chalcedon
		beginWithinHyperspaceRangeTrigger(chalcedon, 3f, true, Stage.GO_TO_MAZALOT);
		triggerCreateFleet(FleetSize.LARGER, FleetQuality.HIGHER, Factions.LUDDIC_PATH, FleetTypes.PATROL_LARGE, mazalot.getLocationInHyperspace());
		triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		triggerMakeHostileAndAggressive();
		triggerMakeLowRepImpact();
		triggerFleetMakeFaster(true, 2, true);
		triggerSetFleetAlwaysPursue();
		triggerPickLocationTowardsEntity(chalcedon.getStarSystem().getHyperspaceAnchor(), 30f, getUnits(1.5f));
		triggerSpawnFleetAtPickedLocation("$lke_patherIntercept", null);
		triggerOrderFleetInterceptPlayer();
		triggerOrderFleetEBurn(1f);
		triggerSetFleetMissionRef("$lke_ref");
		triggerFleetMakeImportant(null, Stage.GO_TO_MAZALOT);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
			set("$lke_stage", getCurrentStage());
			//set("$anh_robedman", robed_man);
	}

	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
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

		if ("postMissionCleanup".equals(action)) {
			// clean up any unneeded memory things.
			//Global.getSector().getMemoryWithoutUpdate().unset("$lpp_didHesperusFirstShrineAttempt");
			Global.getSector().getMemoryWithoutUpdate().unset("$global.lke_foundBornanew");
			
			mazalot.getMemoryWithoutUpdate().unset("$market.dardanWontTalkLKE");
			
			return true;
		}
		else if ("shootEm".equals(action))
		{
			Global.getSoundPlayer().playSound("storyevent_diktat_execution", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
			return true;
		}
		else if ("resumeMusic".equals(action))
		{
			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
			Global.getSoundPlayer().restartCurrentMusic();
			return true;
		}
		else if ("endMusic".equals(action))
		{
			Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
			Global.getSoundPlayer().pauseMusic();
			//Global.getSoundPlayer().restartCurrentMusic();
			return true;
		}
		else if ("playMusicCombat".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_combat", true);
			return true;
		}
		else if ("playMusicSedge".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_luddite_encounter_hostile", true);
			return true;
		}
		else if ("playMusicVirens".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_luddite_market_hostile", true);
			return true;
		}
		else if ("playMusicJethro".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_luddite_market_friendly", true);
			return true;
		}
		else if ("playMusicDardan".equals(action))
		{
			Global.getSoundPlayer().playCustomMusic(1, 1, "music_persean_league_market_hostile", true);
			return true;
		}
		else if ("didMazalotRaid".equals(action))
		{
			RecentUnrest.get(mazalot).add(10, "Raided Mazalot and caused a Luddic uprising");
			return true;
		}
		else if ("doCleanup".equals(action))
		{

			Global.getSector().getMemoryWithoutUpdate().unset("$lke_gotVirensContactFreebie");
			Global.getSector().getMemoryWithoutUpdate().unset("$lke_didMazBarAgentEncounter");
			chalcedon.getMemoryWithoutUpdate().unset("$lkeBuggedVIPs");
			mazalot.getMemoryWithoutUpdate().unset("$lke_wontTellLied");
			mazalot.getMemoryWithoutUpdate().unset("$lke_askedPMChurch");
			mazalot.getMemoryWithoutUpdate().unset("$lkeSetUpVirensMeeting");
			mazalot.getMemoryWithoutUpdate().unset("$dardanWontTalkLKE");
			
			return true;
		}
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}
	
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

		if (currentStage == Stage.GO_TO_CHALCEDON) {
			info.addPara("Find Bornanew. His last mission was the infiltration of a Pather cell; someone must known something about where he is.", opad);
			addStandardMarketDesc("Go to " + chalcedon.getOnOrAt(), chalcedon, info, opad);
		}
		else if (currentStage == Stage.CONTACT_RECRUITER) {
			info.addPara("You have been told to meet with someone in a spacer bar on the surface of Chalcedon, presumably a recruiter for the Luddic Path.", opad);
			addStandardMarketDesc("Go to " + chalcedon.getOnOrAt(), chalcedon, info, opad);
		}
		else if (currentStage == Stage.GO_TO_MAZALOT) {
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
		}
		
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		//Color h = Misc.getHighlightColor();
		
		if (currentStage == Stage.GO_TO_CHALCEDON) {
			info.addPara("Go to Chalcedon and find Jethro Bornanew", tc, pad);
			return true;
		}
		else if (currentStage == Stage.CONTACT_RECRUITER) {
			info.addPara("Go to the spacer bar on Chalcedon and speak with the Luddic Path recruiter", tc, pad);
			return true;
		}
		else if (currentStage == Stage.GO_TO_MAZALOT) {
			info.addPara("Go to Mazalot and find Jethro Bornanew", tc, pad);
			return true;
		}
		else if (currentStage == Stage.CONTACT_VIRENS) {
			info.addPara("Talk to or raid Nile Virens on Mazalot", tc, pad);
			return true;
		}
		else if (currentStage == Stage.CONTACT_BORNANEW) {
			info.addPara("Go to the alleged location of Jethro Bornanew on Mazalot", tc, pad);
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_GILEAD) {
			info.addPara("Return to Gilead with Bornanew", tc, pad);
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_GILEAD2) {
			info.addPara("Return to Gilead with news of Bornanew's 'death'.", tc, pad);
			return true;
		}

		return false;
		
		/*
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DROP_OFF) {
			info.addPara("Deliver " + getWithoutArticle(thing) + " to specified location in the " +  
					system.getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		*/
	}

	@Override
	public String getBaseName() {
		return "Knight Errant";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}





