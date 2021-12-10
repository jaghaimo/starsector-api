package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SpySatDeployment extends HubMissionWithBarEvent {

	public static float PROB_BAR_UNDERWORLD = 0.25f;
	public static float PROB_PATROL_AROUND_TARGET = 0.5f;
	
	public static float MISSION_DAYS = 120f;
	
	public static enum Stage {
		DEPLOY,
		COMPLETED,
		FAILED,
	}
	
	protected MarketAPI market;
	protected SectorEntityToken target;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		//if (Factions.PIRATES.equals(createdAt.getFaction().getId())) return false;
		
		if (barEvent) {
			if (rollProbability(PROB_BAR_UNDERWORLD)) {
				setGiverRank(Ranks.CITIZEN);
				setGiverPost(pickOne(Ranks.POST_AGENT, Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
							 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
				setGiverImportance(pickImportance());
				setGiverFaction(Factions.PIRATES);
				setGiverTags(Tags.CONTACT_UNDERWORLD);
			} else {				
				setGiverRank(Ranks.CITIZEN);
				setGiverPost(Ranks.POST_AGENT);
				setGiverImportance(pickImportance());
				if (Factions.PIRATES.equals(createdAt.getFaction().getId())) {
					setGiverTags(Tags.CONTACT_UNDERWORLD);
					setGiverFaction(Factions.PIRATES);
				} else {
					setGiverTags(Tags.CONTACT_MILITARY);
				}
			}
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		
		if (!setPersonMissionRef(person, "$ssat_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		requireMarketIsNot(createdAt);
		requireMarketLocationNot(createdAt.getContainingLocation());
		requireMarketFactionNotPlayer();
		requireMarketFactionNot(person.getFaction().getId());
		requireMarketFactionCustom(ReqMode.NOT_ANY, Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE);
		requireMarketMilitary();
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		preferMarketInDirectionOfOtherMissions();
		market = pickMarket();
		
		if (market == null) return false;
		
		target = spawnMissionNode(
					new LocData(EntityLocationType.ORBITING_PARAM, market.getPrimaryEntity(), market.getStarSystem()));
		if (!setEntityMissionRef(target, "$ssat_ref")) return false;
		
		makeImportant(target, "$ssat_target", Stage.DEPLOY);
		setMapMarkerNameColor(market.getTextColorForFactionOrPlanet());
		
		setStartingStage(Stage.DEPLOY);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, target, "$ssat_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
//		int sizeModifier = market.getSize() * 10000;
//		setCreditReward(10000 + sizeModifier, 30000 + sizeModifier);
		setCreditReward(CreditReward.AVERAGE, market.getSize());
		
		if (rollProbability(PROB_PATROL_AROUND_TARGET)) {
			triggerCreateMediumPatrolAroundMarket(market, Stage.DEPLOY, 1f);
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$ssat_barEvent", isBarEvent());
		set("$ssat_underworld", getPerson().hasTag(Tags.CONTACT_UNDERWORLD));
		set("$ssat_manOrWoman", getPerson().getManOrWoman());
		set("$ssat_reward", Misc.getWithDGS(getCreditsReward()));
		
		set("$ssat_personName", getPerson().getNameString());
		set("$ssat_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$ssat_marketName", market.getName());
		set("$ssat_marketOnOrAt", market.getOnOrAt());
		set("$ssat_dist", getDistanceLY(market));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DEPLOY) {
			info.addPara("Deploy a spysat in orbit of " + market.getName() + 
					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DEPLOY) {
			info.addPara("Deploy spysat near " +
					market.getName() + " in the " + 
					market.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "SpySat Deployment";
	}
	
}

