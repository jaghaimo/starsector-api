package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class JailbreakMission extends HubMissionWithBarEvent {

	public static float MISSION_DAYS = 120f;
	public static float PROB_COMPLICATIONS = 0.5f;
	
	public static enum Stage {
		JAILBREAK,
		RETURN,
		COMPLETED,
		FAILED,
		FAILED_DECIV,
	}
	
	protected MarketAPI market;
	protected RaidDangerLevel danger;
	protected int storyCost = 0;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
//		System.out.println("JAILBREAK: " + genRandom.nextLong());
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(pickOne(Ranks.POST_AGENT, Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
						 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
			setGiverImportance(pickImportance());
			setGiverFaction(Factions.PIRATES);
			setGiverTags(Tags.CONTACT_UNDERWORLD);
			findOrCreateGiver(createdAt, true, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (!setPersonMissionRef(person, "$jabr_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		PersonImportance importance = person.getImportance();
		int minMarketSize = 3;
		int maxMarketSize = 9;
		switch (importance) {
		case VERY_LOW:
			minMarketSize = 3;
			maxMarketSize = 4;
			danger = RaidDangerLevel.MINIMAL;
			break;
		case LOW:
			minMarketSize = 4;
			maxMarketSize = 4;
			danger = RaidDangerLevel.LOW;
			break;
		case MEDIUM:
			minMarketSize = 5;
			maxMarketSize = 5;
			danger = RaidDangerLevel.MEDIUM;
			break;
		case HIGH:
			minMarketSize = 5;
			maxMarketSize = 6;
			danger = RaidDangerLevel.HIGH;
			break;
		case VERY_HIGH:
			minMarketSize = 6;
			maxMarketSize = 8;
			danger = RaidDangerLevel.EXTREME;
			break;
		}
		
		requireMarketIsNot(createdAt);
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		requireMarketFactionNotPlayer();
		preferMarketSizeAtLeast(minMarketSize);
		preferMarketSizeAtMost(maxMarketSize);
		preferMarketInDirectionOfOtherMissions();
		market = pickMarket();
		
		
		if (market == null) return false;
		if (!setMarketMissionRef(market, "$jabr_ref")) {
			return false;
		}
		
		int marines = getMarinesRequiredForCustomObjective(market, danger);
		if (!isOkToOfferMissionRequiringMarines(marines)) {
			return false;
		}
		
		makeImportant(market, "$jabr_target", Stage.JAILBREAK);
		makeImportant(getPerson(), "$jabr_returnHere", Stage.RETURN);
		
		setStartingStage(Stage.JAILBREAK);
		setSuccessStage(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		connectWithMemoryFlag(Stage.JAILBREAK, Stage.RETURN, market, "$jabr_needToReturn");
		setStageOnMemoryFlag(Stage.COMPLETED, person, "$jabr_completed");
		
		addNoPenaltyFailureStages(Stage.FAILED_DECIV);
		connectWithMarketDecivilized(Stage.JAILBREAK, Stage.FAILED_DECIV, market);
		setStageOnMarketDecivilized(Stage.FAILED_DECIV, createdAt);
		
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN);
		
		//int size = market.getSize();
		//setCreditReward(CreditReward.HIGH, size);
		
		int bonus = getRewardBonusForMarines(getMarinesRequiredForCustomObjective(market, danger));
		setCreditRewardWithBonus(CreditReward.AVERAGE, bonus);
		
		storyCost = getRoundNumber(getCreditsReward() / 2);
		
		
		if (rollProbability(PROB_COMPLICATIONS)) {
			triggerComplicationBegin(Stage.RETURN, ComplicationSpawn.APPROACHING_OR_ENTERING,
					createdAt.getStarSystem(), Factions.MERCENARY,
					"the escapee", "they", "the escapee from " + market.getName(),
					0,
					true, ComplicationRepImpact.NONE, null);
			//triggerSetFleetSizeAndQuality(FleetSize.HUGE, FleetQuality.SMOD_3, FleetTypes.PATROL_LARGE);
			//triggerSetFleetDoctrineComp(0, 0, 5);
			triggerComplicationEnd(true);
//			triggerComplicationBegin(Stage.JAILBREAK, ComplicationSpawn.ENTERING_SYSTEM,
//					createdAt.getStarSystem(), Factions.REMNANTS,
//					"the escapee", "they", "the escapee from " + market.getName(),
//					//getRoundNumber(getCreditsReward() * 1.5f),
//					10000,
//					true, ComplicationRepImpact.NONE, null);
//			triggerComplicationEnd();			
		}
		
		return true;
	}


	protected void updateInteractionDataImpl() {
		set("$jabr_barEvent", isBarEvent());
		set("$jabr_manOrWoman", getPerson().getManOrWoman());
		set("$jabr_heOrShe", getPerson().getHeOrShe());
		set("$jabr_hisOrHer", getPerson().getHisOrHer());
		set("$jabr_HisOrHer", Misc.ucFirst(getPerson().getHisOrHer()));
		set("$jabr_reward", Misc.getWithDGS(getCreditsReward()));
		set("$jabr_storyCost", Misc.getWithDGS(storyCost));
		
		set("$jabr_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$jabr_marketName", market.getName());
		set("$jabr_marketOnOrAt", market.getOnOrAt());
		set("$jabr_marketFactionArticle", market.getFaction().getPersonNamePrefixAOrAn());
		set("$jabr_marketFaction", market.getFaction().getPersonNamePrefix());
		set("$jabr_factionColor",  market.getFaction().getBaseUIColor());
		set("$jabr_dist", getDistanceLY(market));
		
		set("$jabr_danger", danger);
		set("$jabr_marines", Misc.getWithDGS(getMarinesRequiredForCustomObjective(market, danger)));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		if (getPerson() == null || getPerson().getMarket() == null) return;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.JAILBREAK) {
			addStandardMarketDesc("Break target out of confinement " + market.getOnOrAt(), market, info, opad);
			addCustomRaidInfo(market, danger, info, opad);
			
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnText(getPerson().getMarket().getName()) + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.JAILBREAK) {
			info.addPara("Conduct jailbreak " + market.getOnOrAt() + " " + market.getName() + 
					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnTextShort(getPerson().getMarket().getName()), tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Jailbreak";// - " + market.getName();
	}
	
//	public String getPostfixForState() {
//		String post = super.getPostfixForState();
//		post = post.replaceFirst(" - ", "");
//		if (!post.isEmpty()) post = " (" + post + ")";
//		return post;
//	}
}




