package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GARansomResearcher extends GABaseMission {

	public static float RAID_DIFFICULTY = 300f;
	
	public static float MISSION_DAYS = 120f;
	
	public static float PROB_PIRATE_VARIATION = 0.5f;
	
	
	public static enum Stage {
		GO_TO_MARKET,
		RETURN_TO_ACADEMY,
		COMPLETED,
		FAILED,
		FAILED_DECIV,
	}
	
	public static enum Variation {
		PIRATE,
		CHURCH,
	}
	
	protected MarketAPI market;
	protected Variation variation;
	protected PersonAPI contact;
	protected PersonAPI researcher;
	
	protected int costNormal;
	protected int costHigh;
	protected int costVeryLow;

	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaRR_ref")) {
			return false;
		}
		if (getGlobalMemory().contains("$gaRR_playerOwes")) {
			return false; // the player needs to pay back the advance before this mission is offered again
		}
		
		pickAnyDepartment();
		
		if (rollProbability(PROB_PIRATE_VARIATION)) {
			variation = Variation.PIRATE;
			setIconName("campaignMissions", "prisoner_pirate");
		} else {
			variation = Variation.CHURCH;
			setIconName("campaignMissions", "prisoner_luddic");
		}
		
		resetSearch();
		
		if (variation == Variation.PIRATE) {
			requireMarketFaction(Factions.PIRATES);
		} else {
			requireMarketFaction(Factions.LUDDIC_CHURCH);
		}
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		requireMarketLocationNot("galatia");
		preferMarketInDirectionOfOtherMissions();
		market = pickMarket();
		
//		market = getMarket("kantas_den");
//		variation = Variation.PIRATE;
		
		if (market == null) {
			return false;
		}
		
		if (variation == Variation.PIRATE) {
			contact = findOrCreateCriminal(market, true);
		} else {
			contact = findOrCreatePerson(Factions.LUDDIC_CHURCH, market, true,
				 						 Ranks.KNIGHT_CAPTAIN, Ranks.POST_GENERIC_MILITARY); 
		}
		if (contact == null) {
			return false;
		}
		
		setStartingStage(Stage.GO_TO_MARKET);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		addNoPenaltyFailureStages(Stage.FAILED_DECIV);
		
		//setPersonDoGenericPortAuthorityCheck(contact);
		makeImportant(contact, "$gaRR_contact", Stage.GO_TO_MARKET);
		makeImportant(getPerson(), "$gaRR_returnHere", Stage.RETURN_TO_ACADEMY);
		
		connectWithGlobalFlag(Stage.GO_TO_MARKET, Stage.RETURN_TO_ACADEMY, "$gaRR_needToReturn");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaRR_completed");
		setStageOnGlobalFlag(Stage.FAILED, "$gaRR_failed");
		connectWithMarketDecivilized(Stage.GO_TO_MARKET, Stage.FAILED_DECIV, market);
		
		if (WITH_TIME_LIMIT) {
			setTimeLimit(Stage.FAILED, MISSION_DAYS, market.getStarSystem(), Stage.RETURN_TO_ACADEMY);
		}
		//setCreditReward(30000, 40000);
		setCreditReward(CreditReward.AVERAGE);
		setDefaultGARepRewards();
		
		costNormal = genRoundNumber(60000, 70000);
		costHigh = genRoundNumber(90000, 100000);
		costVeryLow = genRoundNumber(8000, 10000);
		
		researcher = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(genRandom);
		researcher.setRankId(Ranks.CITIZEN);
		researcher.setPostId(Ranks.POST_ACADEMICIAN);
		
		beginStageTrigger(Stage.FAILED, Stage.FAILED_DECIV);
		triggerSetGlobalMemoryValuePermanent("$gaRR_playerOwes", true);
		triggerSetGlobalMemoryValuePermanent("$gaRR_failedCredits", Misc.getWithDGS(costNormal));
		endTrigger();
		
		return true;
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								Map<String, MemoryAPI> memoryMap) {
		if (dialog != null && action.equals("showResearcher")) {
			showPersonInfo(researcher, dialog, false, false);
			return true;
		}
		return false;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaRR_department", department);
		set("$gaRR_marketName", market.getName());
		set("$gaRR_onOrAt", market.getOnOrAt());
		
		set("$gaRR_contactName", contact.getNameString());
		set("$gaRR_raidDifficulty", RAID_DIFFICULTY);
		set("$gaRR_researcherName", researcher.getNameString());
		
		set("$gaRR_costVeryLow", Misc.getWithDGS(costVeryLow));
		set("$gaRR_costNormal", Misc.getWithDGS(costNormal));
		set("$gaRR_costHigh", Misc.getWithDGS(costHigh));
		
		set("$gaRR_systemName", market.getStarSystem().getNameWithNoType());
		set("$gaRR_dist", getDistanceLY(market));
		set("$gaRR_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaRR_variation", variation);
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_MARKET) {
			String adjective = "kidnapped";
			if (variation == Variation.CHURCH) adjective = "imprisoned"; 
			info.addPara(getGoToMarketText(market) +
					", and ransom the " + adjective + " researcher from " + contact.getNameString() + ".", opad);
			//addCustomRaidInfo((int)RAID_DIFFICULTY, RaidDangerLevel.MINIMAL, info, opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara(getReturnText("the Galatia Academy") + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_MARKET) {
			info.addPara(getGoToMarketText(market), tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara(getReturnTextShort("the Galatia Academy"), tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Ransom Researcher";
	}

}





