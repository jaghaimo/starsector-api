package com.fs.starfarer.api.impl.campaign.missions.luddic;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class LostOutpostCrewReturnLuddicFind extends HubMissionWithBarEvent {
	
	//public static float MISSION_DAYS = 180f;
	
	public static String LOCR_LUDDIC_PLANET_KEY = "$locr_luddicPlanet";
	public static String LOCR_LUDDIC = "$locr_luddic";
	public static String LOCRL_LUDDIC_DISCOVERED = "$locr_luddicDiscovered";
	public static String LOCR_LUDDIC_TRANSPORT_KEY = "$locr_luddicTransport";
	
	public static enum Stage {
		SEARCH,
		COMPLETED,
		RETURN,
		FAILED,
	}
	
	protected int rewardAmount;
	protected int rewardAmountHigher;
	
	protected MarketAPI market;
	protected SectorEntityToken target;
	protected SectorEntityToken target_system;
	protected SectorEntityToken luddic_transport; 
	protected PersonAPI person;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference("$locrlf_ref", "$locrlf_inProgress")) {
            return false;
        }
		
		person = getPerson();
		if (person == null) { 
			return false;
		}
		
		// If they're not Luddic, they're not giving this mission!!!
		if (person.getFaction().getId() != "luddic_church" ) return false;
		
		//genRandom = Misc.random;
		
		//System.out.print("Attempting LostOutpostCrewReturnLuddicFind");
		
		// If player has already found the Luddics, no mission.
		//if (Global.getSector().getMemoryWithoutUpdate().contains(LOCRL_LUDDIC_DISCOVERED)) {
		if (Global.getSector().getMemoryWithoutUpdate().is(LOCRL_LUDDIC_DISCOVERED,true)) {
			return false;
		}
				
		// No planet means no mission.
		target = Global.getSector().getMemoryWithoutUpdate().getEntity(LOCR_LUDDIC_PLANET_KEY);
		if (target == null) { 
			//System.out.print("... but no Luddic Outpost exists.");
			return false;
		}
		
		// No Luddic transport also means no mission.
		if (!Global.getSector().getMemoryWithoutUpdate().contains(LOCR_LUDDIC_PLANET_KEY)) {
			return false;
		}
		
		luddic_transport = Global.getSector().getMemoryWithoutUpdate().getEntity(LOCR_LUDDIC_TRANSPORT_KEY);
		if (luddic_transport == null) {
			return false;
		}
		
		
		target_system = target.getStarSystem().getCenter();
		
		//  must be given from a Luddic market
		//if (!Factions.LUDDIC_CHURCH.equals(createdAt.getFaction().getId())) return false;

		if (barEvent) {
			
			String post = null;
			setGiverRank(Ranks.CITIZEN);
			//setGiverPost(Ranks.POST_AGENT);
			setGiverImportance(pickImportance());
			setGiverTags(Tags.CONTACT_TRADE);
			post = pickOne(	Ranks.POST_TRADER,
							Ranks.POST_COMMODITIES_AGENT,
							Ranks.POST_PORTMASTER,
							Ranks.POST_MERCHANT,
							Ranks.POST_GUILDMASTER,
							Ranks.POST_COMMUNE_LEADER,
							Ranks.POST_ADMINISTRATOR);
			
			findOrCreateGiver(createdAt, false, false);
		
			setGiverPost(post);
			if (	post.equals(Ranks.POST_BASE_COMMANDER) ||
					post.equals(Ranks.POST_ADMINISTRATOR)) {
				setGiverImportance(pickHighImportance());
			} else {
				setGiverImportance(pickImportance());
			}
		}
		

		
		if (!setPersonMissionRef(person, "$locrlf_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}

		// Do we need to store the market? Maybe.
		market = createdAt;
		if (market == null) return false;
		market.getMemoryWithoutUpdate().set("$locrlf_market", true);
		
		makeImportant(luddic_transport, "$locrlf_target", Stage.SEARCH);
		makeImportant(target.getStarSystem().getHyperspaceAnchor(), null, Stage.SEARCH);
		makeImportant(person, "$locrlf_return", Stage.RETURN);	
	
		setStartingStage(Stage.SEARCH);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		connectWithGlobalFlag(Stage.SEARCH, Stage.RETURN, "$locrlf_foundHeretics");
		connectWithGlobalFlag(Stage.RETURN, Stage.COMPLETED, "$locrlf_completed");
		
		//setTimeLimit(Stage.FAILED, MISSION_DAYS, null);

		rewardAmount = 24000;
		rewardAmountHigher = 36000;
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$locrlf_barEvent", isBarEvent());
		set("$locrlf_manOrWoman", getPerson().getManOrWoman());
		set("$locrlf_heOrShe", getPerson().getHeOrShe());
		set("$locrlf_himOrHer", getPerson().getHimOrHer());
		set("$locrlf_hisOrHer", getPerson().getHisOrHer());
		set("$locrlf_HeOrShe", getPerson().getHeOrShe().substring(0, 1).toUpperCase() + getPerson().getHeOrShe().substring(1));
		set("$locrlf_HisOrHer", getPerson().getHisOrHer().substring(0, 1).toUpperCase() + getPerson().getHisOrHer().substring(1));
		set("$locrlf_HimOrHer", getPerson().getHimOrHer().substring(0, 1).toUpperCase() + getPerson().getHimOrHer().substring(1));
		
		set("$locrlf_rewardAmount", Misc.getWithDGS(rewardAmount));
		set("$locrlf_rewardAmountHigher", Misc.getWithDGS(rewardAmountHigher));
		
		set("$locrlf_person", getPerson());
		set("$locrlf_personName", getPerson().getNameString());
		set("$locrlf_systemName", target.getStarSystem().getNameWithLowercaseTypeShort());
		set("$locrlf_transportName", luddic_transport.getName());  
		//set("$locrlf_marketName", target.getName());
		//set("$locrlft_marketOnOrAt", market.getOnOrAt());
		set("$locrlf_dist", getDistanceLY(target));

	}
	
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if ("addPersonToMarket".equals(action)) {
			//System.out.print("Adding person to market.");
			
			Misc.moveToMarket(person, market, true);
			if (person.getMarket() == null) return false;
			if (person.getMarket().getCommDirectory() == null) return false;
			if (person.getMarket().getCommDirectory().getEntryForPerson(person.getId()) == null) return false;
			person.getMarket().getCommDirectory().getEntryForPerson(person.getId()).setHidden(false);
			
			return true;
		}
		return false;
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.SEARCH) {
			info.addPara("Search the " + target_system.getStarSystem().getNameWithLowercaseTypeShort()
					+ " for the lost group of Luddic heretics.", opad);
		}
		else if (currentStage == Stage.RETURN) {
			info.addPara("Return to " + person.getNameString() + " to report that you found "
					+ "the lost Luddic heretics.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.SEARCH) {
			info.addPara("Search for the Luddic heretics in the " +
					target_system.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		else if (currentStage == Stage.RETURN) {
			info.addPara("Return with news of the Luddic heretics to " + person.getNameString() + ".", tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "The Stray Flock";
	}
	
}

