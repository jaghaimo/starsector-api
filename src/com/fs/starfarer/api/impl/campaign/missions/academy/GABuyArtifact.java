package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GABuyArtifact extends GABaseMission {

	public static float RAID_DIFFICULTY = 100f;
	
	public static float MISSION_DAYS = 120f;
	
	public static float PROB_PIRATE_FLEET = 1f/4f;
	public static float PROB_PATHER_FLEET = 1f/3f;
	public static float PROB_TRITACH_FLEET = 1f/2f;
	
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
		PATHER,
	}
	
	protected MarketAPI market;
	protected Variation variation;
	protected PersonAPI contact;
	protected String item;
	
	protected int costNormal;
	protected int costHigh;
	protected int costVeryLow;

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaBA_ref")) {
			return false;
		}
		if (getGlobalMemory().contains("$gaBA_playerOwes")) {
			return false; // the player needs to pay back the advance before this mission is offered again
		}
		
		pickDepartment(GADepartments.INDUSTRIAL, GADepartments.MILITARY, GADepartments.SCIENCE, GADepartments.WEIRD);
		
		if (rollProbability(PROB_PIRATE_VARIATION)) {
			variation = Variation.PIRATE;
		} else {
			variation = Variation.PATHER;
		}
		
		resetSearch();
		
		if (variation == Variation.PIRATE) {
			requireMarketFaction(Factions.PIRATES);
		} else {
			requireMarketFaction(Factions.LUDDIC_PATH, Factions.LUDDIC_CHURCH);
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
			contact = findOrCreateCriminalTrader(market, true);
		} else {
			contact = findOrCreateTrader(Factions.LUDDIC_PATH, market, true);
		}
		if (contact == null) {
			return false;
		}
		
		item = pickOne(
				"unique regenerative structural elements",
				"an advanced phase conversion prototype",
				"an example of a lost technology",
				"experimental hull material samples",
				"uncommon power system components",
				"Domain-era forge components",
				"a small-scale momentum decoupling device",
				"a fascinating low-power field projector",
				"parts of an experimental bulk phase converter",
				"classified Explorarium probe samples"
		);
		
		setStartingStage(Stage.GO_TO_MARKET);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		addNoPenaltyFailureStages(Stage.FAILED_DECIV);
		
		//setPersonDoGenericPortAuthorityCheck(contact);
		makeImportant(contact, "$gaBA_contact", Stage.GO_TO_MARKET);
		makeImportant(getPerson(), "$gaBA_returnHere", Stage.RETURN_TO_ACADEMY);
		
		connectWithGlobalFlag(Stage.GO_TO_MARKET, Stage.RETURN_TO_ACADEMY, "$gaBA_needToReturn");
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaBA_completed");
		setStageOnGlobalFlag(Stage.FAILED, "$gaBA_failed");
		
		connectWithMarketDecivilized(Stage.GO_TO_MARKET, Stage.FAILED_DECIV, market);
		
		if (WITH_TIME_LIMIT) {
			setTimeLimit(Stage.FAILED, MISSION_DAYS, market.getStarSystem(), Stage.RETURN_TO_ACADEMY);
		}
		//setCreditReward(30000, 40000);
		setCreditReward(CreditReward.AVERAGE);
		setDefaultGARepRewards();
		
		costNormal = genRoundNumber(40000, 50000);
		costHigh = genRoundNumber(70000, 80000);
		costVeryLow = genRoundNumber(5000, 8000);
		
		beginStageTrigger(Stage.FAILED, Stage.FAILED_DECIV);
		triggerSetGlobalMemoryValuePermanent("$gaBA_playerOwes", true);
		triggerSetGlobalMemoryValuePermanent("$gaBA_failedItem", getWithoutArticle(item));
		triggerSetGlobalMemoryValuePermanent("$gaBA_failedCredits", Misc.getWithDGS(costNormal));
		endTrigger();
		
		if (rollProbability(PROB_PIRATE_FLEET)) {
			beginWithinHyperspaceRangeTrigger(createdAt, 3f, true, Stage.RETURN_TO_ACADEMY);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, createdAt.getPrimaryEntity());
			//triggerFleetSetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			triggerAutoAdjustFleetStrengthModerate();
			//triggerSetFleetDoctrineOther(5, 4);
			
			triggerSetStandardAggroPirateFlags();
			triggerFleetAllowLongPursuit();
			triggerSetFleetAlwaysPursue();
			triggerPickLocationTowardsPlayer(createdAt.getStarSystem().getHyperspaceAnchor(), 90f, getUnits(1.5f));
			triggerSpawnFleetAtPickedLocation("$pwi_wantsItem", null);
			triggerSetFleetMemoryValue("$pwi_item", getWithoutArticle(item));
			triggerSetFleetMemoryValue("$pwi_credits", Misc.getWithDGS(genRoundNumber(10000, 15000)));
			triggerSetFleetMemoryValue("$pwi_missionFailTrigger", "GABAHandedOverItemFailMission");
			triggerOrderFleetInterceptPlayer();
			triggerFleetMakeImportant(null, Stage.RETURN_TO_ACADEMY);
			endTrigger();
		} else if (rollProbability(PROB_PATHER_FLEET)) {
			beginWithinHyperspaceRangeTrigger(createdAt, 3f, true, Stage.RETURN_TO_ACADEMY);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.LUDDIC_PATH, FleetTypes.PATROL_MEDIUM, createdAt.getPrimaryEntity());
			triggerAutoAdjustFleetStrengthModerate();
			triggerSetStandardAggroPirateFlags();
			triggerFleetAllowLongPursuit();
			triggerSetFleetAlwaysPursue();
			triggerPickLocationTowardsPlayer(createdAt.getStarSystem().getHyperspaceAnchor(), 90f, getUnits(1.5f));
			triggerSpawnFleetAtPickedLocation("$pwi2_wantsItem", null);
			triggerSetFleetMemoryValue("$pwi2_item", getWithoutArticle(item));
			triggerSetFleetMemoryValue("$pwi2_missionFailTrigger", "GABAHandedOverItemFailMission");
			triggerOrderFleetInterceptPlayer();
			triggerFleetMakeImportant(null, Stage.RETURN_TO_ACADEMY);
			endTrigger();
		} else if (rollProbability(PROB_TRITACH_FLEET)) {
			beginWithinHyperspaceRangeTrigger(createdAt, 3f, true, Stage.RETURN_TO_ACADEMY);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.TRITACHYON, FleetTypes.TASK_FORCE, createdAt.getPrimaryEntity());
			triggerAutoAdjustFleetStrengthModerate();
			triggerFleetAllowLongPursuit();
			triggerPickLocationTowardsPlayer(createdAt.getStarSystem().getHyperspaceAnchor(), 90f, getUnits(1.5f));
			triggerSpawnFleetAtPickedLocation("$ttwi_wantsItem", null);
			triggerSetFleetMemoryValue("$ttwi_item", getWithoutArticle(item));
			triggerSetFleetMemoryValue("$ttwi_credits", Misc.getWithDGS(genRoundNumber(120000, 150000)));
			triggerSetFleetMemoryValue("$ttwi_missionFailTrigger", "GABAHandedOverItemFailMission");
			triggerOrderFleetInterceptPlayer();
			triggerFleetMakeImportant(null, Stage.RETURN_TO_ACADEMY);
			endTrigger();
		}

		
		return true;
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								Map<String, MemoryAPI> memoryMap) {
//		if (dialog != null && action.equals("showLeader")) {
//			showPersonInfo(leader, dialog, false, false);
//			return true;
//		}
		return false;
	}

	protected void updateInteractionDataImpl() {
		set("$gaBA_department", department);
		set("$gaBA_marketName", market.getName());
		set("$gaBA_onOrAt", market.getOnOrAt());
		
		set("$gaBA_contactName", contact.getNameString());
		set("$gaBA_raidDifficulty", RAID_DIFFICULTY);
		
		set("$gaBA_costVeryLow", Misc.getWithDGS(costVeryLow));
		set("$gaBA_costNormal", Misc.getWithDGS(costNormal));
		set("$gaBA_costHigh", Misc.getWithDGS(costHigh));
		
		set("$gaBA_systemName", market.getStarSystem().getNameWithNoType());
		set("$gaBA_dist", getDistanceLY(market));
		set("$gaBA_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaBA_variation", variation);
		
		set("$gaBA_aOrAnItem", item);
		set("$gaBA_item", getWithoutArticle(item));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_MARKET) {
			info.addPara(getGoToMarketText(market) +
					", and buy " + item + " from " + contact.getNameString() + ".", opad);
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
		return "Acquire Artifact";
	}

}





