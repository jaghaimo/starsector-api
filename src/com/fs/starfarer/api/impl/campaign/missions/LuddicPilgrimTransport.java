package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.academy.GACelestialObject.Variation;
import com.fs.starfarer.api.impl.campaign.missions.askonia.TheUsurpers.Stage;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class LuddicPilgrimTransport extends HubMissionWithBarEvent {

	//graphics/icons/missions/luddic_pilgrim_transport.png
	public static float MISSION_DAYS = 60f;
	public static float MIN_VALUE = 10000f;
	public static float MAX_VALUE = 20000f;
	public static float PROB_RICH = 0.5f;
	
	public static enum Stage {
		TRANSPORT,
		COMPLETED,
		FAILED,
	}
	
	public static enum Variation {
		RICH,
		POOR,
	}
	
	
	protected MarketAPI source_market;
	protected StarSystemAPI destination_system;
	protected SectorEntityToken destination_entity;
	protected int quantity;
	protected String destination_shrine;
	protected Variation variation;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		//if (Factions.PIRATES.equals(createdAt.getFaction().getId())) return false;
		
		source_market = createdAt;
		
		
		
		// let's just whitelist core worlds. Should be based on places that plausibly have a significant/interesting Luddic population
		if (! ( source_market.getId().equals("chicomoztoc") ||
				!source_market.getId().equals("mazalot") ||
				!source_market.getId().equals("ilm") ||
				!source_market.getId().equals("volturn") ||
				!source_market.getId().equals("jangala") ||
				!source_market.getId().equals("epiphany") ||
				!source_market.getId().equals("cibola") ||
				!source_market.getId().equals("madeira") ||
				!source_market.getId().equals("chalcedon") ||
				!source_market.getId().equals("tartessus") ||
				!source_market.getId().equals("hesperus") ||
				!source_market.getId().equals("fikenhild") ||
				!source_market.getId().equals("athulf") ||
				!source_market.getId().equals("suddene")) ) {
			return false;
			}
		
		if (rollProbability(PROB_RICH)) {
			variation = Variation.RICH;
			float value = MIN_VALUE + getQuality() * (MAX_VALUE - MIN_VALUE);
			value *= 0.9f + genRandom.nextFloat() * 0.2f;
			setCreditReward((int)(value * 0.5f), (int)(value * 0.7f));
		} else {
			variation = Variation.POOR;
		}
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(Ranks.POST_PILGRIM);
			//setGiverImportance(pickImportance());
			setGiverImportance(PersonImportance.VERY_LOW); // irrelevant anyway.
			setGiverFaction(Factions.LUDDIC_CHURCH);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		
		if (!setPersonMissionRef(person, "$lpt_ref")) {
			return false;
		}
		
		setRepFactionChangesNone();
		setIconName("campaignMissions", "luddic_pilgrim_transport");
		
		// Maybe handle non-"public" shrines later.
		destination_shrine = pickOne("gilead", "beholder_station");
		
		if (destination_shrine.equals("gilead"))
		{
			destination_system = Global.getSector().getStarSystem("canaan");
			destination_entity = Global.getSector().getEconomy().getMarket("gilead").getPlanetEntity();
			if (destination_entity == null) return false;
			if (!destination_entity.getMarket().getFactionId().equals(Factions.LUDDIC_CHURCH)) return false;
		}
		else if (destination_shrine.equals("beholder_station"))
		{
			destination_system = Global.getSector().getStarSystem("kumari kandam");
			for (SectorEntityToken beholder_station : destination_system.getEntitiesWithTag(Tags.LUDDIC_SHRINE)) {
				destination_entity = beholder_station;
				break;
			}
		}
		
		if (!setEntityMissionRef(destination_entity, "$lpt_ref")) {
			return false;
		}
		
		makeImportant(destination_entity, "$lpt_target", Stage.TRANSPORT);
		
		setStartingStage(Stage.TRANSPORT);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, destination_entity, "$lpt_completed");
	//	setStageOnGlobalFlag(Stage.COMPLETED, "lpt_completed");
		setNoAbandon();
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		
		
		//setCreditRewardWithBonus(CreditReward.VERY_LOW, (int) (value * 0.5f));
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		//set("$lpt_destinationShrine", destination_shrine);
		set("$lpt_barEvent", isBarEvent());
		set("$lpt_reward", Misc.getWithDGS(getCreditsReward()));
		
		set("$lpt_manOrWoman", getPerson().getManOrWoman());
		set("$lpt_heOrShe", getPerson().getHeOrShe());
		set("$lpt_hisOrHer", getPerson().getHisOrHer());
		set("$lpt_hisOrHer", getPerson().getHisOrHer());
		set("$lpt_himOrHer", getPerson().getHimOrHer());
		set("$lpt_HeOrShe", getPerson().getHeOrShe().substring(0, 1).toUpperCase() + getPerson().getHeOrShe().substring(1));
		set("$lpt_HisOrHer", getPerson().getHisOrHer().substring(0, 1).toUpperCase() + getPerson().getHisOrHer().substring(1));
		set("$lpt_HimOrHer", getPerson().getHimOrHer().substring(0, 1).toUpperCase() + getPerson().getHimOrHer().substring(1));
		
		if( variation == variation.POOR)
			set("$lpt_wealth", "poor");
		else set("$lpt_wealth", "rich");	
		
		set("$lpt_personName", getPerson().getNameString());
		set("$lpt_systemName", destination_system.getNameWithLowercaseTypeShort());
		set("$lpt_entityName", destination_entity.getName());
		set("$lpt_sourceName", source_market.getName());
		
		//if (destination_entity.m)
		//set("$lpt_marketOnOrAt", destination_entity.getOnOrAt());
		
		set("$lpt_dist", getDistanceLY(destination_entity));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TRANSPORT) {
			info.addPara("Transport a small part of Luddic pilgrims to " + destination_entity.getName() + 
					" in the " + destination_system.getNameWithLowercaseTypeShort() + ".", opad,
					h, Misc.getWithDGS(quantity));
			
			info.addPara("The pilgrims will expect to arrive at the shrine within " + MISSION_DAYS + " days.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TRANSPORT) {
			info.addPara("Transport a small part of Luddic pilgrims to " + destination_entity.getName() + 
					" in the " + destination_system.getNameWithLowercaseTypeShort(), pad, tc,
					h, Misc.getWithDGS(quantity));
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Luddic Pilgrim Transport";
	}
	
}

