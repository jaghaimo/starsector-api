package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ExtractionMission extends HubMissionWithBarEvent {

	public static float PROB_BASE_INSTEAD_OF_MARKET_MIN = 0.25f;
	public static float PROB_BASE_INSTEAD_OF_MARKET_MAX = 0.75f;
	public static float PROB_PIRATE_BASE_WHEN_BASE = 0.5f;
	
	public static float PROB_ADDITIONAL_PATROLS = 0.5f;
	
	public static float MISSION_DAYS = 120f;
	
	public static enum Variation {
		COLONY,
		PIRATE_BASE,
		LUDDIC_PATH_BASE,
	}
	
	public static enum Stage {
		EXTRACT,
		RETURN,
		COMPLETED,
		FAILED,
		FAILED_DECIV,
	}
	
	protected Variation variation;
	protected MarketAPI market;
	protected RaidDangerLevel danger;
	protected int storyCost = 0;
	protected String seedyBarOwner;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;

		if (Factions.PIRATES.equals(createdAt.getFaction().getId())) return false;
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(Ranks.POST_AGENT);
			setGiverImportance(pickImportance());
			setGiverTags(Tags.CONTACT_MILITARY);
			findOrCreateGiver(createdAt, true, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (!setPersonMissionRef(person, "$extr_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		seedyBarOwner = pickOne("Eduardo",
								"Roy", 
								"Hyde",
								"Shang",
								"Rick",
								"Bogdan",
								"William",
								"Marlowe",
								"Benny"
				);
		
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
		
		if (importance.ordinal() >= PersonImportance.HIGH.ordinal()) {
			float pBase = PROB_BASE_INSTEAD_OF_MARKET_MIN + 
					(PROB_BASE_INSTEAD_OF_MARKET_MAX - PROB_BASE_INSTEAD_OF_MARKET_MIN) * 
					Math.max(0, getPerson().getRelToPlayer().getRel());
			if (rollProbability(pBase)) {
				resetSearch();
				requireMarketIsNot(createdAt);
				requireMarketFactionNotPlayer();
				requireMarketMemoryFlag(PirateBaseIntel.MEM_FLAG, true);
				requireMarketNotInHyperspace();
				preferMarketInDirectionOfOtherMissions();
				MarketAPI pirateBase = pickMarket();
				
				resetSearch();
				requireMarketIsNot(createdAt);
				requireMarketMemoryFlag(LuddicPathBaseIntel.MEM_FLAG, true);
				requireMarketNotInHyperspace();
				preferMarketInDirectionOfOtherMissions();
				MarketAPI pathBase = pickMarket();
				
				boolean allowPath = !Factions.LUDDIC_PATH.equals(createdAt.getFaction().getId());
				
				if (rollProbability(PROB_PIRATE_BASE_WHEN_BASE) && pirateBase != null) {
					market = pirateBase;
					variation = Variation.PIRATE_BASE;
					danger = RaidDangerLevel.EXTREME;
				} else if (allowPath) {
					market = pathBase;
					variation = Variation.LUDDIC_PATH_BASE;
					danger = RaidDangerLevel.EXTREME;
				}
			}
		}
		
		if (market == null) {
			resetSearch();
			requireMarketIsNot(createdAt);
			requireMarketFactionNotPlayer();
			requireMarketNotHidden();
			requireMarketNotInHyperspace();
			preferMarketSizeAtLeast(minMarketSize);
			preferMarketSizeAtMost(maxMarketSize);
			preferMarketFactionHostileTo(createdAt.getFactionId());
			preferMarketInDirectionOfOtherMissions();
			market = pickMarket();
			variation = Variation.COLONY;
			danger = RaidDangerLevel.MEDIUM;
		}
		
		if (market == null) return false;
		if (!setMarketMissionRef(market, "$extr_ref")) {
			return false;
		}
		
		int marines = getMarinesRequiredForCustomObjective(market, danger);
		if (!isOkToOfferMissionRequiringMarines(marines)) {
			return false;
		}
		
		makeImportant(market, "$extr_target", Stage.EXTRACT);
		makeImportant(getPerson(), "$extr_returnHere", Stage.RETURN);
		
		setStartingStage(Stage.EXTRACT);
		setSuccessStage(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		connectWithMemoryFlag(Stage.EXTRACT, Stage.RETURN, market, "$extr_needToReturn");
		setStageOnMemoryFlag(Stage.COMPLETED, person, "$extr_completed");
		
		addNoPenaltyFailureStages(Stage.FAILED_DECIV);
		connectWithMarketDecivilized(Stage.EXTRACT, Stage.FAILED_DECIV, market);
		setStageOnMarketDecivilized(Stage.FAILED_DECIV, createdAt);
		
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
//		int sizeModifier = market.getSize() * 10000;
//		if (variation == Variation.PIRATE_BASE || variation == Variation.LUDDIC_PATH_BASE) {
//			sizeModifier = 10 * 10000;;
//		}
//		setCreditReward(10000 + sizeModifier, 30000 + sizeModifier);
		//int size = market.getSize();
		int extraBonus = 0;
		if (variation == Variation.PIRATE_BASE || variation == Variation.LUDDIC_PATH_BASE) {
			//size = 10;
			extraBonus = 75000;
		}
		//setCreditReward(CreditReward.AVERAGE, size);
		
		int bonus = getRewardBonusForMarines(getMarinesRequiredForCustomObjective(market, danger));
		setCreditRewardWithBonus(CreditReward.AVERAGE, bonus + extraBonus);
		
		storyCost = getRoundNumber(getCreditsReward() / 2);
		
		if (rollProbability(PROB_ADDITIONAL_PATROLS)) {
			if (market.getSize() <= 4) {
				triggerCreateMediumPatrolAroundMarket(market, Stage.EXTRACT, 0f);
			} else if (market.getSize() <= 6) {
				triggerCreateLargePatrolAroundMarket(market, Stage.EXTRACT, 0f);
			} else {
				triggerCreateMediumPatrolAroundMarket(market, Stage.EXTRACT, 0f);
				triggerCreateLargePatrolAroundMarket(market, Stage.EXTRACT, 0f);
			}
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$extr_variation", variation);
		set("$extr_barName", seedyBarOwner + "'s");
		set("$extr_barOwner", seedyBarOwner);
		set("$extr_barEvent", isBarEvent());
		set("$extr_manOrWoman", getPerson().getManOrWoman());
		set("$extr_reward", Misc.getWithDGS(getCreditsReward()));
		set("$extr_storyCost", Misc.getWithDGS(storyCost));
		
		set("$extr_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$extr_marketName", market.getName());
		set("$extr_marketOnOrAt", market.getOnOrAt());
		set("$extr_marketFactionArticle", market.getFaction().getPersonNamePrefixAOrAn());
		set("$extr_marketFaction", market.getFaction().getPersonNamePrefix());
		set("$extr_factionColor",  market.getFaction().getBaseUIColor());
		set("$extr_dist", getDistanceLY(market));
		
		set("$extr_danger", danger);
		set("$extr_marines", Misc.getWithDGS(getMarinesRequiredForCustomObjective(market, danger)));
				
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.EXTRACT) {
			info.addPara("Extract agent located " +
					 market.getOnOrAt() + " " + market.getName() + 
					 " in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad);
			if (variation == Variation.PIRATE_BASE || variation == Variation.LUDDIC_PATH_BASE) {
				FactionAPI f = market.getFaction();
				info.addPara("The target location is " + f.getPersonNamePrefixAOrAn() + " %s base.",
							 opad, f.getBaseUIColor(), f.getPersonNamePrefix());
				
//				info.addPara("The operation will require a sizeable marine contingent and will face active " +
//						"resistance. A close assessment of the station's internal defenses is " +
//						"required to determine the forces necessary.", opad);
			} else {
				FactionAPI f = market.getFaction();
				LabelAPI label = info.addPara("The target location is a size %s " +
								"colony controlled by " + f.getDisplayNameWithArticle() + ".",
							 opad, f.getBaseUIColor(),
							 "" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlight("" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(h, f.getBaseUIColor());
				
//				info.addPara("The operation will require a sizeable marine contingent and will face active " +
//						"resistance. A close assessment of the colony's ground defenses is " +
//						"required to determine the forces necessary.", opad);
			}
			addCustomRaidInfo(market, danger, info, opad);
			
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnText(getPerson().getMarket().getName()) + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.EXTRACT) {
				info.addPara("Extract agent from " +
							 market.getName() + 
							 " in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", pad, tc,
							 market.getFaction().getBaseUIColor(), market.getName());
				return true;
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnTextShort(getPerson().getMarket().getName()), tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Combat Extraction";
	}
}




