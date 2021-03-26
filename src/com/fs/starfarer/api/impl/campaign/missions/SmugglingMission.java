package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SmugglingMission extends HubMissionWithBarEvent {

	public static float MISSION_DAYS = 60f;
	public static float MIN_VALUE = 10000f;
	public static float MAX_VALUE = 100000f;
	
	public static enum Stage {
		SMUGGLE,
		COMPLETED,
		FAILED,
	}
	
	protected MarketAPI market;
	protected CommodityOnMarketAPI com;
	protected int quantity;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		//if (Factions.PIRATES.equals(createdAt.getFaction().getId())) return false;
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(pickOne(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
						 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
			setGiverImportance(pickImportance());
			setGiverFaction(Factions.PIRATES);
			setGiverTags(Tags.CONTACT_UNDERWORLD);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		
		if (!setPersonMissionRef(person, "$smug_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		requireMarketIsNot(createdAt);
		requireMarketFactionNotPlayer();
		requireMarketLocationNot(createdAt.getContainingLocation());
		requireMarketFactionCustom(ReqMode.NOT_ANY, Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE);
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		preferMarketInDirectionOfOtherMissions();
		
		requireCommodityIllegal();
		requireCommodityDemandAtLeast(1);
		
		com = pickCommodity();
		if (com == null) return false;
		
		market = com.getMarket();
		if (market == null) return false;
		
		
		float value = MIN_VALUE + getQuality() * (MAX_VALUE - MIN_VALUE);
		value *= 0.9f + genRandom.nextFloat() * 0.2f;
		
		quantity = getRoundNumber(value / com.getCommodity().getBasePrice());
		if (quantity < 10) quantity = 10;
		
		if (!setMarketMissionRef(market, "$smug_ref")) {
			return false;
		}
		makeImportant(market, "$smug_target", Stage.SMUGGLE);
		
		setStartingStage(Stage.SMUGGLE);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, market, "$smug_completed");
		setNoAbandon();
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		
		//setCreditReward((int)(value * 0.5f), (int)(value * 0.7f));
		setCreditRewardWithBonus(CreditReward.LOW, (int) (value * 0.5f));
		
		triggerCreateMediumPatrolAroundMarket(market, Stage.SMUGGLE, 1f);
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$smug_barEvent", isBarEvent());
		set("$smug_manOrWoman", getPerson().getManOrWoman());
		set("$smug_reward", Misc.getWithDGS(getCreditsReward()));
		set("$smug_commodityId", com.getId());
		set("$smug_commodityName", com.getCommodity().getLowerCaseName());
		set("$smug_quantity", Misc.getWithDGS(quantity));
		set("$smug_playerHasEnough", playerHasEnough(com.getId(), quantity));
		
		set("$smug_personName", getPerson().getNameString());
		set("$smug_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$smug_marketName", market.getName());
		set("$smug_marketOnOrAt", market.getOnOrAt());
		set("$smug_dist", getDistanceLY(market));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.SMUGGLE) {
			info.addPara("Smuggle %s units of " + com.getCommodity().getLowerCaseName() + " to " + market.getName() + 
					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad,
					h, Misc.getWithDGS(quantity));
			
			info.addPara("The authorities are aware the shipment is incoming and patrols are on high alert.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.SMUGGLE) {
			info.addPara("Smuggle %s units of " + com.getCommodity().getLowerCaseName() + " to " +
					market.getName() + " in the " + 
					market.getStarSystem().getNameWithLowercaseTypeShort(), pad, tc,
					h, Misc.getWithDGS(quantity));
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Smuggling " + com.getCommodity().getName();
	}
	
}

