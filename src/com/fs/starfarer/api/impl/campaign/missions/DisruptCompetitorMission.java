package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class DisruptCompetitorMission extends HubMissionWithBarEvent {

	public static float PROB_COMPLICATIONS = 0.25f;
	
	public static float MISSION_DAYS = 120f;
	
	public static RaidDangerLevel RAID_DANGER = RaidDangerLevel.MEDIUM;
	
	public static enum Stage {
		DISRUPT,
		COMPLETED,
		FAILED,
	}
	
	protected MarketAPI market;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			String post = pickOne(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, 
			 		 			  Ranks.POST_MERCHANT, Ranks.POST_INVESTOR,
			 		 			  Ranks.POST_EXECUTIVE, Ranks.POST_SENIOR_EXECUTIVE, 
			 		 			  Ranks.POST_PORTMASTER);
			setGiverPost(post);
			if (post.equals(Ranks.POST_SENIOR_EXECUTIVE)) {
				setGiverImportance(pickHighImportance());
			} else {
				setGiverImportance(pickImportance());
			}
			setGiverTags(Tags.CONTACT_TRADE);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		
		if (!setPersonMissionRef(person, "$dcom_ref")) {
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
			break;
		case LOW:
			minMarketSize = 4;
			maxMarketSize = 4;
			break;
		case MEDIUM:
			minMarketSize = 5;
			maxMarketSize = 5;
			break;
		case HIGH:
			minMarketSize = 5;
			maxMarketSize = 6;
			break;
		case VERY_HIGH:
			minMarketSize = 6;
			maxMarketSize = 8;
			break;
		}
		requireMarketIsNot(createdAt);
		requireMarketNotHidden();
		requireMarketFactionNotPlayer();
		requireMarketNotInHyperspace();
		preferMarketSizeAtLeast(minMarketSize);
		preferMarketSizeAtMost(maxMarketSize);
		market = pickMarket();
		
		if (market == null) return false;
		if (!setMarketMissionRef(market, "$dcom_ref")) {
			return false;
		}
		
		int marines = getMarinesRequiredForCustomObjective(market, RAID_DANGER);
		if (!isOkToOfferMissionRequiringMarines(marines)) {
			return false;
		}
		
		makeImportant(market, "$dcom_target", Stage.DISRUPT);
		
		setStartingStage(Stage.DISRUPT);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, market, "$dcom_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		//int sizeModifier = market.getSize() * 10000;
		//setCreditReward(10000 + sizeModifier, 30000 + sizeModifier);
		//setCreditReward(CreditReward.AVERAGE, market.getSize());
		
		int bonus = getRewardBonusForMarines(getMarinesRequiredForCustomObjective(market, RAID_DANGER));
		setCreditRewardWithBonus(CreditReward.AVERAGE, bonus);
		
		return true;
	}
	
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		if (isSucceeded() && (rollProbability(PROB_COMPLICATIONS))) {
			DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
			//e.setDelay(0f);
			e.setDelayMedium();
			e.setLocationInnerSector(false, Factions.INDEPENDENT);
			//e.setEncounterInHyper();
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_1, Factions.MERCENARY, FleetTypes.PATROL_LARGE, new Vector2f());
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			e.triggerFleetSetFaction(Factions.INDEPENDENT);
			e.triggerSetAdjustStrengthBasedOnQuality(true, getQuality());
			e.triggerSetStandardAggroNonPirateFlags();
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerSetFleetMemoryValue("$dcom_marketName", market.getName());
			e.triggerSetFleetMemoryValue("$dcom_marketOnOrAt", market.getOnOrAt());
			e.triggerSetFleetGenericHailPermanent("DCOMMercHail");
			e.endCreate();
		}
	}



	protected void updateInteractionDataImpl() {
		set("$dcom_barEvent", isBarEvent());
		set("$dcom_manOrWoman", getPerson().getManOrWoman());
		set("$dcom_hisOrHer", getPerson().getHisOrHer());
		set("$dcom_reward", Misc.getWithDGS(getCreditsReward()));
		
		set("$dcom_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$dcom_marketName", market.getName());
		set("$dcom_marketOnOrAt", market.getOnOrAt());
		set("$dcom_dist", getDistanceLY(market));
		set("$dcom_marketFactionArticle", market.getFaction().getPersonNamePrefixAOrAn());
		set("$dcom_marketFaction", market.getFaction().getPersonNamePrefix());
		set("$dcom_factionColor",  market.getFaction().getBaseUIColor());
		set("$dcom_danger", RAID_DANGER);
		
		set("$dcom_marines", Misc.getWithDGS(getMarinesRequiredForCustomObjective(market, RAID_DANGER)));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DISRUPT) {
			info.addPara("Disrupt a competitor of " + getPerson().getNameString() + " by raiding their warehouse " +
					market.getOnOrAt() + " " + market.getName() + 
					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad);
			
			addCustomRaidInfo(market, RAID_DANGER, info, opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DISRUPT) {
			info.addPara("Raid warehouse " +
					market.getOnOrAt() + " " + market.getName() + " in the " + 
					market.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Disrupt Competitor";
	}
	
}

