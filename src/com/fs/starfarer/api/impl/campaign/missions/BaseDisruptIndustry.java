package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseDisruptIndustry extends HubMissionWithBarEvent implements ColonyPlayerHostileActListener {

	public static float MISSION_DAYS = 240f;
	
	public static enum Stage {
		DISRUPT,
		COMPLETED,
		FAILED,
		FAILED_NO_PENALTY,
	}
	
	protected MarketAPI market;
	protected Industry industry;
	protected int disruptDays;
	
	protected void createBarGiver(MarketAPI createdAt) {
	}
	
	protected String [] getTargetIndustries() {
		return new String[] {Industries.HEAVYINDUSTRY, Industries.ORBITALWORKS};
	}
	
	protected CreditReward getRewardTier() {
		return CreditReward.HIGH;
	}
	
	protected boolean requireFactionHostile() {
		return true;
	}
	
	protected void setMarketSearchParameters(MarketAPI createdAt, String [] industries) {
		requireMarketIsNot(createdAt);
		requireMarketLocationNot(createdAt.getContainingLocation());
		requireMarketFactionNotPlayer();
		if (requireFactionHostile()) {
			requireMarketFactionHostileTo(createdAt.getFactionId());
		}
		requireMarketNotHidden();
		requireMarketIndustries(ReqMode.ANY, industries);
		requireMarketNotInHyperspace();
		
		float q = getQuality();
		if (q <= 0) {
			preferMarketSizeAtMost(4);
		} else if (q <= 0.25) {
			preferMarketSizeAtMost(5);
		} else if (q <= 0.5) {
			preferMarketSizeAtMost(6);
		} else if (q <= 0.75) {
			preferMarketSizeAtMost(7);
		}
	}
	
	protected void addExtraTriggers(MarketAPI createdAt) {
		
	}
	
	protected boolean availableAtMarket(MarketAPI createdAt) {
		return true;
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		//if (Factions.PIRATES.equals(createdAt.getFaction().getId())) return false;
		
		if (!availableAtMarket(createdAt)) {
			return false;
		}
		
		if (barEvent) {
			createBarGiver(createdAt);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		String id = getMissionId();
		if (!setPersonMissionRef(person, "$" + id + "_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		String [] industries = getTargetIndustries();
		
		setMarketSearchParameters(createdAt, industries);
		
		market = pickMarket();
		if (market == null) return false;
		
		for (String indId : industries) {
			industry = market.getIndustry(indId);
			if (industry != null) {
				break;
			}
		}
		if (industry == null) return false;
		
		disruptDays = MarketCMD.getDisruptDaysPerToken(market, industry) * 3;
		
		if (!setMarketMissionRef(market, "$" + id + "_ref")) {
			return false;
		}
		
		int marines = getMarinesRequiredToDisrupt(market, industry, disruptDays);
		if (!isOkToOfferMissionRequiringMarines(marines)) {
			return false;
		}
		
		makeImportant(market, "$" + id + "_target", Stage.DISRUPT);
		
		setStartingStage(Stage.DISRUPT);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, market, "$" + id + "_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		addNoPenaltyFailureStages(Stage.FAILED_NO_PENALTY);
		connectWithMarketDecivilized(Stage.DISRUPT, Stage.FAILED_NO_PENALTY, market);
		setStageOnMarketDecivilized(Stage.FAILED_NO_PENALTY, createdAt);
		if (requireFactionHostile()) {
			connectWithHostilitiesEnded(Stage.DISRUPT, Stage.FAILED_NO_PENALTY, person, market);
			setStageOnHostilitiesEnded(Stage.FAILED_NO_PENALTY, person, market);
		}
		
		//setCreditReward(80000, 100000);
		//setCreditReward(getRewardTier(), market.getSize());
		
		int bonus = getRewardBonusForMarines(getMarinesRequiredToDisrupt(market, industry, disruptDays));
		setCreditRewardWithBonus(getRewardTier(), bonus);
		
		addExtraTriggers(createdAt);
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		String id = getMissionId();
		set("$" + id + "_barEvent", isBarEvent());
		set("$" + id + "_manOrWoman", getPerson().getManOrWoman());
		set("$" + id + "_hisOrHer", getPerson().getHisOrHer());
		set("$" + id + "_heOrShe", getPerson().getHeOrShe());
		set("$" + id + "_reward", Misc.getWithDGS(getCreditsReward()));
		set("$" + id + "_industry", industry.getCurrentName());
		set("$" + id + "_disruptDays", disruptDays);
		set("$" + id + "_marines", Misc.getWithDGS(getMarinesRequiredToDisrupt(market, industry, disruptDays)));
		
		set("$" + id + "_personName", getPerson().getNameString());
		set("$" + id + "_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$" + id + "_marketName", market.getName());
		set("$" + id + "_marketOnOrAt", market.getOnOrAt());
		set("$" + id + "_dist", getDistanceLY(market));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DISRUPT) {
//			info.addPara("Disrupt " + industry.getCurrentName().toLowerCase() + " " + market.getOnOrAt() + " "+ market.getName() + 
//					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad);
			
			addStandardMarketDesc("Disrupt " + industry.getCurrentName() + " " + market.getOnOrAt(), market, info, opad);
			//addStandardMarketDesc(market, info, opad);
			
			addDisruptRaidInfo(market, industry, disruptDays, info, opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DISRUPT) {
			info.addPara("Disrupt " + industry.getCurrentName().toLowerCase() + " " + market.getOnOrAt() + " "+ market.getName() + 
					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);			
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Disrupt " + industry.getCurrentName();
	}
	
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.acceptImpl(dialog, memoryMap);
		Global.getSector().getListenerManager().addListener(this);
	}

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		Global.getSector().getListenerManager().removeListener(this);
	}

	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog,
							MarketAPI market, TempData actionData, Industry industry) {
		if (this.industry == industry && industry.getDisruptedDays() >= disruptDays) {
			Global.getSector().getListenerManager().removeListener(this);
			String id = getMissionId();
			market.getMemoryWithoutUpdate().set("$" + id + "_raidedTargetIndustry", true, 0);
		}
	}
	
	
	
	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, 
								MarketAPI market, TempData actionData,
								CargoAPI cargo) {
		// TODO Auto-generated method stub
	}


	public void reportSaturationBombardmentFinished(
			InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		// TODO Auto-generated method stub
		
	}

	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog,
			MarketAPI market, TempData actionData) {
		// TODO Auto-generated method stub
		
	}
	
}



