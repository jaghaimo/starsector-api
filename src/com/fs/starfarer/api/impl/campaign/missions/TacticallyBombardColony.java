package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TacticallyBombardColony extends HubMissionWithBarEvent implements ColonyPlayerHostileActListener {

	public static float PROB_PATROL_ENCOUNTER_AFTER = 0.5f;
	public static float MISSION_DAYS = 120f;
	
	public static enum Stage {
		BOMBARD,
		COMPLETED,
		FAILED,
		FAILED_NO_PENALTY,
	}
	
	
	
	protected void createBarGiver(MarketAPI createdAt) {
		List<String> posts = new ArrayList<String>();
		posts.add(Ranks.POST_AGENT);
		if (createdAt.getSize() >= 6) {
			posts.add(Ranks.POST_ADMINISTRATOR);
		}
		if (Misc.isMilitary(createdAt)) {
			posts.add(Ranks.POST_BASE_COMMANDER);
		}
		if (Misc.hasOrbitalStation(createdAt)) {
			posts.add(Ranks.POST_STATION_COMMANDER);
		}
		String post = pickOne(posts);
		if (post == null) return;
		
		// rank used only when it's an agent, since the other posts mean the person already exists
		// and doesn't need to be created
		setGiverRank(pickOne(Ranks.GROUND_CAPTAIN, Ranks.GROUND_COLONEL, Ranks.GROUND_MAJOR,
					 Ranks.SPACE_COMMANDER, Ranks.SPACE_CAPTAIN, Ranks.SPACE_ADMIRAL));
		setGiverTags(Tags.CONTACT_MILITARY);
		setGiverPost(post);
		setGiverImportance(pickHighImportance());
		findOrCreateGiver(createdAt, false, false);
	}
	
	
	protected MarketAPI market;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		if (barEvent) {
			createBarGiver(createdAt);
		}
		
		if (!Misc.isMilitary(createdAt)) return false;
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (!setPersonMissionRef(person, "$tabo_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		requireMarketIsNot(createdAt);
		requireMarketFactionNotPlayer();
		requireMarketFactionHostileTo(createdAt.getFactionId());
		requireMarketTacticallyBombardable();
		requireMarketNotHidden();
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
		
		market = pickMarket();
		if (market == null) return false;
		
		if (!setMarketMissionRef(market, "$tabo_ref")) {
			return false;
		}
		makeImportant(market, "$tabo_target", Stage.BOMBARD);
		
		setStartingStage(Stage.BOMBARD);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, market, "$tabo_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		addNoPenaltyFailureStages(Stage.FAILED_NO_PENALTY);
		connectWithMarketDecivilized(Stage.BOMBARD, Stage.FAILED_NO_PENALTY, market);
		setStageOnMarketDecivilized(Stage.FAILED_NO_PENALTY, createdAt);
		connectWithHostilitiesEnded(Stage.BOMBARD, Stage.FAILED_NO_PENALTY, person, market);
		setStageOnHostilitiesEnded(Stage.FAILED_NO_PENALTY, person, market);
		
		//setCreditReward(80000, 100000);
		setCreditReward(CreditReward.VERY_HIGH, market.getSize());
		
		if (market.getSize() <= 4) {
			triggerCreateLargePatrolAroundMarket(market, Stage.BOMBARD, 0f);
			triggerCreateSmallPatrolAroundMarket(market, Stage.BOMBARD, 0f);
		} else if (market.getSize() <= 6) {
			triggerCreateLargePatrolAroundMarket(market, Stage.BOMBARD, 0f);
			triggerCreateMediumPatrolAroundMarket(market, Stage.BOMBARD, 0f);
			triggerCreateSmallPatrolAroundMarket(market, Stage.BOMBARD, 0f);
		} else {
			triggerCreateLargePatrolAroundMarket(market, Stage.BOMBARD, 0f);
			triggerCreateLargePatrolAroundMarket(market, Stage.BOMBARD, 0f);
			triggerCreateMediumPatrolAroundMarket(market, Stage.BOMBARD, 0f);
			triggerCreateSmallPatrolAroundMarket(market, Stage.BOMBARD, 0f);
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$tabo_barEvent", isBarEvent());
		set("$tabo_manOrWoman", getPerson().getManOrWoman());
		set("$tabo_reward", Misc.getWithDGS(getCreditsReward()));
		
		set("$tabo_personName", getPerson().getNameString());
		set("$tabo_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$tabo_marketName", market.getName());
		set("$tabo_marketOnOrAt", market.getOnOrAt());
		set("$tabo_dist", getDistanceLY(market));
		
		int fuel = getBombardmentFuel(market);
		set("$tabo_fuel", Misc.getWithDGS(fuel));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.BOMBARD) {
			addStandardMarketDesc("Perform a tactical bombardment of", market, info, opad);
			addBombardmentInfo(market, info, opad);
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
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Bombard " + market.getName();
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
			if (!isEnded() || isEnding()) {
				if (rollProbability(PROB_PATROL_ENCOUNTER_AFTER)) {
					DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
					e.setDelayMedium();
					e.setLocationCoreOnly(true, market.getFactionId());
					e.beginCreate();
					e.triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.HIGHER, market.getFactionId(), FleetTypes.PATROL_LARGE, new Vector2f());
					e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
					e.triggerSetAdjustStrengthBasedOnQuality(true, getQuality());
					e.triggerSetPatrol();
					e.triggerSetStandardAggroInterceptFlags();
					e.triggerSetFleetMemoryValue("$tabo_marketName", market.getName());
					e.triggerSetFleetGenericHailPermanent("TABOPatrolHail");
					e.endCreate();
				}
			}
			
			Global.getSector().getListenerManager().removeListener(this);
			// need to set non-zero expiration since bombardment code advances the market with 0/very small elapsed time
			// a few times, so if set to 0 the value would be removed from memory
			market.getMemoryWithoutUpdate().set("$tabo_bombardedColony", true, 1f);
		}		
	}
	
}





