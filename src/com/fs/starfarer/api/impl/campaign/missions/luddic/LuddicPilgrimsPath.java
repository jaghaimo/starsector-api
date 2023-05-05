package com.fs.starfarer.api.impl.campaign.missions.luddic;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.LuddicShrineIntel;
import com.fs.starfarer.api.impl.campaign.missions.academy.GACelestialObject.Variation;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class LuddicPilgrimsPath extends HubMissionWithSearch {

	public static enum Stage {
		VISIT_SHRINES,
		RETURN_TO_GILEAD,
		COMPLETED,
	}
	
	public static String VISITED_SHRINE_JANGALA = "$lpp_visitedShrineJangala";
	public static String VISITED_SHRINE_KILLA = "$lpp_visitedShrineKilla";
	public static String VISITED_SHRINE_VOLTURN = "$lpp_visitedShrineVolturn";
	public static String VISITED_SHRINE_CHICOMOZTOC = "$lpp_visitedShrineChicomoztoc";
	public static String VISITED_SHRINE_GILEAD = "$lpp_visitedShrineGilead";
	public static String VISITED_SHRINE_BEHOLDER = "$lpp_visitedShrineBeholder";
	
	protected int xpRewardBase = 10000;
	protected int xpRewardIncrease = 2000;
	protected int num_rewards_given = 0;
	
	//protected PersonAPI robed_man;
	//protected PersonAPI some_kid; 
	//protected PersonAPI robot; 
	
	protected MarketAPI volturn;
	protected MarketAPI gilead;
	protected MarketAPI jangala;
	protected MarketAPI hesperus;
	protected SectorEntityToken killa;
	protected SectorEntityToken beholder_station;
	//public static float MISSION_DAYS = 120f;
	
	//protected int payment;
	//protected int paymentHigh;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$lpp_ref", "$lpp_inProgress")) {
			return false;
		}
		
		volturn = Global.getSector().getEconomy().getMarket("volturn");
		if (volturn == null) return false;
		//if (!volturn.getFactionId().equals("sindrian_diktat")) return false;
		
		
		gilead = Global.getSector().getEconomy().getMarket("gilead");
		if (gilead == null) return false;
		
		jangala = Global.getSector().getEconomy().getMarket("jangala");
		if (jangala == null) return false;

		hesperus = Global.getSector().getEconomy().getMarket("hesperus");
		if (hesperus == null) return false;
		
		// Find Killa!
		StarSystemAPI yma =  Global.getSector().getStarSystem("yma");
		for (SectorEntityToken curr : yma.getEntitiesWithTag(Tags.LUDDIC_SHRINE)) {
			killa = curr;
			break;
		}
		if (killa == null) return false;
		
		// Find Beholder Station!
		StarSystemAPI kumarikandam =  Global.getSector().getStarSystem("kumari kandam");
		for (SectorEntityToken curr : kumarikandam.getEntitiesWithTag(Tags.LUDDIC_SHRINE)) {
			beholder_station = curr;
			break;
		}
		if (beholder_station == null) return false;
		
		setStartingStage(Stage.VISIT_SHRINES);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		//addTag(Tags.INTEL_SHRINES);
		
		// yes, these exact numbers.
		//payment = 10000;
		//paymentHigh = 17000; 
		
		
		// TODO - makeImportant all the shrines!
		setStageOnGlobalFlag(Stage.RETURN_TO_GILEAD, "$lpp_visitedAllShrines");
		
		makeImportant(gilead, "$lpp_finishPilgrimage", Stage.RETURN_TO_GILEAD);
		//setStageOnMemoryFlag(Stage.COMPLETED, gilead, "$lpp_completed");
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$lpp_completed");
		
		
		
		setName("The Pilgrim's Path");
		setRepFactionChangesNone();
		setRepPersonChangesNone();
	
		beginStageTrigger(Stage.COMPLETED);
		triggerMakeNonStoryCritical("jangala", "hesperus", "gilead", "volturn");
		triggerSetGlobalMemoryValue("$lpp_missionCompleted", true);
		endTrigger();
		
		
		//makeImportant(coureuse, null, Stage.TALK_TO_COUREUSE);
//		Global.getSector().getMemoryWithoutUpdate().unset("$gaFC_triedToSeeCavin");
		return true;
	}
	
	protected void updateInteractionDataImpl() {
			set("$lpp_stage", getCurrentStage());
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
		
		if ("giveVisitXP".equals(action))
		{
			num_rewards_given++;
			long xp_to_give = (long)(num_rewards_given * xpRewardIncrease + xpRewardBase);
			Global.getSector().getPlayerStats().addXP(xp_to_give, dialog.getTextPanel());
			return true;
		}
		else if ("giveLastVisitXP".equals(action))
		{
			num_rewards_given++;
			long xp_to_give = (long)(num_rewards_given * xpRewardIncrease + xpRewardBase);
			
			// it's possible the player visited shrines before taking on the mission
			// so add all that XP as a lump sum onto the end
			int visited_shrines = getNumberOfShrinesVisited();
			if (num_rewards_given < visited_shrines)
			{
				for (int i = num_rewards_given; i < visited_shrines; i++)
				{
					xp_to_give += (long)(i * xpRewardIncrease + xpRewardBase);
				}
			}
			
			Global.getSector().getPlayerStats().addXP(xp_to_give, dialog.getTextPanel());
			return true;
		}
		else if ("checkShrinesVisited".equals(action)) {
			
			int visited_shrines = getNumberOfShrinesVisited();
			int numberOfShrinesToComplete = 6;
			System.out.print("checkShrinesVisited = " + Integer.toString(visited_shrines));
			
			if( visited_shrines >= numberOfShrinesToComplete) {
				Global.getSector().getMemoryWithoutUpdate().set("$lpp_visitedAllShrines", true);
			}
			return true;
		}
		else if ("postMissionCleanup".equals(action)) {
			// clean up any unneeded memory things.
			Global.getSector().getMemoryWithoutUpdate().unset("$lpp_didHesperusFirstShrineAttempt");
			Global.getSector().getMemoryWithoutUpdate().unset("$lpp_didHookStart");
			gilead.getMemoryWithoutUpdate().unset("$lpp_finishPilgrimage");
			return true;
		}
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		FactionAPI faction = getFactionForUIColors();
		PersonAPI person = getPerson();
		
		info.addImage(Global.getSettings().getSpriteName("illustrations", "luddic_shrine"), width, opad);
		
		addDescriptionForCurrentStage(info, width, height);
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
	}
	

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		//Color h2 = Misc.getDarkHighlightColor();
		//FactionAPI church = Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
		
		//info.addImage(robed_man.getPortraitSprite(), width, 128, opad);
		/*
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(VISITED_SHRINE_JANGALA)) {
			
		}
		*/
		
		if (currentStage == Stage.VISIT_SHRINES) {
			info.addPara("Visit the six shrines of the Pilgrim's Path. The pilgrimage is traditionally completed by returning to Gilead, in the Canaan system.", opad);
		}
		else if (currentStage == Stage.RETURN_TO_GILEAD) {
			info.addPara("Return to the shrine of Gilead in the Canaan system to complete the Pilgrim's Path.", opad);
		}

		LuddicShrineIntel.addShowShrinesButton(this, width, height, info);
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.VISIT_SHRINES) {
			info.addPara("Visit the six Luddic shrines", tc, pad);
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_GILEAD) {
			info.addPara("Complete the pilgrimage on Gilead, in the Canaan system", tc, pad);
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
		return "Pilgrim's Path";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == LuddicShrineIntel.BUTTON_SHOW_SHRINES) {
			LuddicShrineIntel.toggleShrineList(this, ui);
			return;
		}
		super.buttonPressConfirmed(buttonId, ui);
	}
	
	
	public int getNumberOfShrinesVisited() {
		int count = 0;
		
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(LuddicShrineIntel.class)) {
			LuddicShrineIntel shrine = (LuddicShrineIntel) intel;
			if (LuddicShrineIntel.isVisited(shrine.getEntity())) {
				count++;
			}
		}

		System.out.print(" Shrine count found = " + count);
		return count;
	}
	
	
	
	public static int getTotalShrines() {
		return Global.getSector().getIntelManager().getIntel(LuddicShrineIntel.class).size();
	}
}





