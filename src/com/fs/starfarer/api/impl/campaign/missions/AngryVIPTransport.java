package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.missions.academy.GACelestialObject.Variation;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AngryVIPTransport extends HubMissionWithBarEvent {

	public static float MISSION_DAYS = 75f;
	public static int MIN_VALUE = 24000;
	public static int MAX_VALUE = 36000;
	
	public static enum Stage {
		TRANSPORT,
		COMPLETED,
		FAILED,
	}
	
	protected MarketAPI sourceMarket;
	//protected StarSystemAPI destination_system;
	protected MarketAPI destination_market;
	protected int quantity;
	protected String destinationId;
	protected Variation variation;
	protected PersonAPI person;
		
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$avipt_ref", "$avipt_inProgress")) {
			return false;
		}
		
		if (createdAt == null) return false;
		
		if (Factions.PIRATES.equals(createdAt.getFaction().getId())) return false; // I guess?
		
		sourceMarket = createdAt;
		
		// whitelist of aristocrat-plausible core worlds.
		String[] planet_whitelist = new String[] {
				"eventide",
				"kazeron",
				"fikenhild", 
				"culann_starforge",
				"eochu_bres",
				"eldfell",
				"salamanca",
				"yesod", 
				"olinadu",
				"station_tse_enterprise",
				"coatl" };
		
		List<String> withoutSource = new ArrayList<String>();
		for (String curr : planet_whitelist) withoutSource.add(curr);
		
		// if the source market isn't one of these, bail out
		if (!withoutSource.remove(sourceMarket.getId())) {
			return false;
		}
		
		destinationId = pickOne(withoutSource);
		if (destinationId == null) {
			System.out.print("AVIPT failed on destinationId");
			return false;
		}
		
		//sourceMarket.getFaction().createRandomPerson();
		//PersonAPI person = sourceMarket.getFaction().createRandomPerson();
		setGiverRank(Ranks.CITIZEN);
		setGiverPost(Ranks.ARISTOCRAT);
		findOrCreateGiver(createdAt, false, false);
		
		
		person = getPerson();
		if (person == null) {
			return false;
		}
		
		//person.setRankId(Ranks.CITIZEN);
		person.setVoice(Voices.ARISTO);
		//person.setPostId(Ranks.POST_ARISTOCRAT);
		
		//if (person == null) return false;
		
		//setPersonOverride(person);
		
		if (!setPersonMissionRef(person, "$avipt_ref")) {
			return false;
		}
		
		setRepFactionChangesNone();
		setIconName("campaignMissions", "shuttle_vip");
		
		destination_market = Global.getSector().getEconomy().getMarket(destinationId);
		
		if (!setMarketMissionRef(destination_market, "$avipt_ref")) {
			return false;
		}
		
		makeImportant(destination_market, "$avipt_target", Stage.TRANSPORT);
		addSuccessStages(Stage.COMPLETED);
		
		setStartingStage(Stage.TRANSPORT);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, destination_market, "$avipt_completed");
		setNoAbandon();
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		
		setCreditReward(MIN_VALUE, MAX_VALUE);
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {

		set("$avipt_barEvent", isBarEvent());
		set("$avipt_reward", Misc.getWithDGS(getCreditsReward()));
		set("$avipt_timelimit", Misc.getWithDGS(MISSION_DAYS));
		set("$avipt_manOrWoman", getPerson().getManOrWoman());
		set("$avipt_heOrShe", getPerson().getHeOrShe());
		set("$avipt_hisOrHer", getPerson().getHisOrHer());
		set("$avipt_himOrHer", getPerson().getHimOrHer());
		set("$avipt_HeOrShe", getPerson().getHeOrShe().substring(0, 1).toUpperCase() + getPerson().getHeOrShe().substring(1));
		set("$avipt_HisOrHer", getPerson().getHisOrHer().substring(0, 1).toUpperCase() + getPerson().getHisOrHer().substring(1));
		set("$avipt_HimOrHer", getPerson().getHimOrHer().substring(0, 1).toUpperCase() + getPerson().getHimOrHer().substring(1));
		
		set("$avipt_personName", getPerson().getNameString());
		set("$avipt_systemName", destination_market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$avipt_marketName", destination_market.getName());
		set("$avipt_sourceName", sourceMarket.getName());
		
		//if (destination_entity.m)
		//set("$lpt_marketOnOrAt", destination_entity.getOnOrAt());
		
		set("$avipt_dist", getDistanceLY(destination_market));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TRANSPORT) {
			info.addPara("Transport an angry, yet wealthy, VIP to " + destination_market.getName() + 
					" in the " +  destination_market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad,
					h, Misc.getWithDGS(quantity));
			
			info.addPara("The VIP has made it clear that " + getPerson().getHeOrShe() + " is to arrive within " + MISSION_DAYS + " days.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TRANSPORT) {
			info.addPara("Transport an angry, yet wealthy, VIP to " + destination_market.getName() + 
					" in the " +  destination_market.getStarSystem().getNameWithLowercaseTypeShort(), pad, tc,
					h, Misc.getWithDGS(quantity));
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Angry VIP Transport";
	}
	
}

