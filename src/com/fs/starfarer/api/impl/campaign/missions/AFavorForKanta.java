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
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AFavorForKanta extends HubMissionWithSearch implements ColonyPlayerHostileActListener {

	public static float MISSION_DAYS = 365f;
	
	public static enum Stage {
		BOMBARD,
		COMPLETED,
		FAILED,
		FAILED_NO_PENALTY,
	}
	
	protected MarketAPI market;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		PersonAPI person = People.getPerson(People.KANTA);
		if (person == null) return false;

		MarketAPI kantasDen = person.getMarket();
		
		setPersonOverride(person);
		
		setStoryMission();
		setNoAbandon();
		
		if (!setGlobalReference("$affk_ref", "$affk_inProgress")) {
			return false;
		}
		
		String commission = Misc.getCommissionFactionId();

		if (kantasDen != null) {
			requireMarketIsNot(kantasDen);
		}
		requireMarketFactionNotPlayer();
		requireMarketFactionNot(Factions.PIRATES);
		requireMarketIsMilitary();
		requireMarketTacticallyBombardable();
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		
		if (commission != null) {
			preferMarketFactionNot(commission);
		}
		preferMarketFactionNotHostileTo(Factions.PLAYER);
		
		market = pickMarket();
		if (market == null) return false;
		
		makeImportant(market, "$affk_target", Stage.BOMBARD);
		
		setStartingStage(Stage.BOMBARD);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, market, "$affk_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		//setTimeLimit(Stage.FAILED, 0.5f, null);
		
		// handled in endFailure calling KantaCMD.loseProtection()
		setRepPenaltyFaction(0f);
		setRepPenaltyPerson(0f);
		
		addNoPenaltyFailureStages(Stage.FAILED_NO_PENALTY);
		connectWithMarketDecivilized(Stage.BOMBARD, Stage.FAILED_NO_PENALTY, market);
		setStageOnMarketDecivilized(Stage.FAILED_NO_PENALTY, market);

		
		triggerCreateLargePatrolAroundMarket(market, Stage.BOMBARD, 0f);
		triggerCreateLargePatrolAroundMarket(market, Stage.BOMBARD, 0f);
		triggerCreateMediumPatrolAroundMarket(market, Stage.BOMBARD, 0f);
		triggerCreateSmallPatrolAroundMarket(market, Stage.BOMBARD, 0f);
		
		return true;
	}
	
	@Override
	protected void endAbandonImpl() {
		super.endAbandonImpl();
		endFailureImpl(null, null);
	}

	@Override
	protected void endFailureImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		if (currentStage == Stage.FAILED) {
			KantaCMD.loseProtection(dialog);
		}
	}
	
	
	
	protected void updateInteractionDataImpl() {
		set("$affk_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$affk_marketName", market.getName());
		set("$affk_marketOnOrAt", market.getOnOrAt());
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.BOMBARD) {
			addStandardMarketDesc("Perform a tactical bombardment of", market, info, opad);
			addBombardmentInfo(market, info, opad);
		} else if (currentStage == Stage.FAILED) {
			//info.addPara("Kanta's Protection lost", Misc.getNegativeHighlightColor(), opad);
			info.addPara("Kanta's Protection lost", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.BOMBARD) {
			info.addPara("Tactically bombard " + market.getName() + 
					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);			
			return true;
		}
//		else if (currentStage == Stage.FAILED) {
//			//info.addPara("Kanta's Protection lost", Misc.getNegativeHighlightColor(), opad);
//			info.addPara("You have failed this mission and lost Kanta's protection.", pad);
//			return true;
//		} else if (currentStage == Stage.FAILED_NO_PENALTY) {
//			info.addPara("You have failed this mission through no fault of your own, and retain Kanta's protection.", pad);
//			return true;
//		}
		return false;
	}
	
	protected String getMissionTypeNoun() {
		return "task";
	}
	
	@Override
	public String getBaseName() {
		//return "Bombard " + market.getName();
		return "A Favor for Kanta";
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
		// TODO Auto-generated method stub
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
		if (this.market == market) {
			Global.getSector().getListenerManager().removeListener(this);
			// need to set non-zero expiration since bombardment code advances the market with 0/very small elapsed time
			// a few times, so if set to 0 the value would be removed from memory
			market.getMemoryWithoutUpdate().set("$affk_bombardedColony", true, 1f);
		}		
	}
	
}





