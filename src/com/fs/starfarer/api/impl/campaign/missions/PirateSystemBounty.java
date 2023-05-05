package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PirateSystemBounty extends HubMissionWithBarEvent implements FleetEventListener {

	public static float BOUNTY_DAYS = 120f;
	public static float BASE_BOUNTY = 1000f;
	//public static float VS_STATION_BONUS = 50000f;
	
	public static class BountyResult {
		public int payment;
		public float fraction;
		public ReputationAdjustmentResult repFaction;
		public ReputationAdjustmentResult repPerson;
		public BountyResult(int payment, float fraction, ReputationAdjustmentResult repPerson, ReputationAdjustmentResult repFaction) {
			this.payment = payment;
			this.fraction = fraction;
			this.repFaction = repFaction;
			this.repPerson = repPerson;
		}
		
	}
	
	
	public static enum Stage {
		BOUNTY,
		DONE,
	}

	protected StarSystemAPI system;
	protected MarketAPI market;
	protected FactionAPI faction;
	protected FactionAPI enemy;
	protected int baseBounty;
	protected BountyResult latestResult;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		if (barEvent) {
			String post = null;
			setGiverRank(Ranks.CITIZEN);
			post = pickOne(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, 
			 			   Ranks.POST_MERCHANT, Ranks.POST_INVESTOR, Ranks.POST_EXECUTIVE,
			 			   Ranks.POST_PORTMASTER,
			 		 	   Ranks.POST_SENIOR_EXECUTIVE);
			setGiverTags(Tags.CONTACT_TRADE);
			setGiverPost(post);
			if (post.equals(Ranks.POST_SENIOR_EXECUTIVE)) {
				setGiverImportance(pickHighImportance());
			} else {
				setGiverImportance(pickImportance());
				
			}
			findOrCreateGiver(createdAt, false, false);
			setGiverIsPotentialContactOnSuccess();
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (Factions.PIRATES.equals(person.getFaction().getId())) return false;
		
		if (!setPersonMissionRef(person, "$psb_ref")) {
			return false;
		}
		
		//requireMarketFaction(Factions.PIRATES);
		requireMarketFactionCustom(ReqMode.ALL, Factions.CUSTOM_MAKES_PIRATE_BASES);
		requireMarketMemoryFlag(PirateBaseIntel.MEM_FLAG, true);
		requireMarketFactionNot(person.getFaction().getId());
		requireMarketHidden();
		requireMarketIsMilitary();
		preferMarketInDirectionOfOtherMissions();
		market = pickMarket();
		
		if (market == null || market.getStarSystem() == null) return false;
		if (!setMarketMissionRef(market, "$psb_ref")) { // just to avoid targeting the same base with multiple bounties
			return false;
		}
		
		makeImportant(market, "$psb_target", Stage.BOUNTY);
		
		system = market.getStarSystem();

		faction = person.getFaction();
		enemy = market.getFaction();
		
		baseBounty = getRoundNumber(BASE_BOUNTY * getRewardMult()); 
		
		setStartingStage(Stage.BOUNTY);
		setSuccessStage(Stage.DONE);
		setNoAbandon();
		setNoRepChanges();
		
		connectWithDaysElapsed(Stage.BOUNTY, Stage.DONE, BOUNTY_DAYS);
		
		addTag(Tags.INTEL_BOUNTY);
		
		
		int numPirates = 2 + genRandom.nextInt(3);

		FleetSize [] sizes = new FleetSize [] {
				FleetSize.MEDIUM,
				FleetSize.MEDIUM,
				FleetSize.LARGE,
				FleetSize.VERY_LARGE,
		};
		
		for (int i = 0; i < numPirates; i++) {
			FleetSize size = sizes[i % sizes.length];
			beginWithinHyperspaceRangeTrigger(system, 3f, false, Stage.BOUNTY);
			triggerCreateFleet(size, FleetQuality.DEFAULT, market.getFactionId(), FleetTypes.PATROL_MEDIUM, system);
			triggerAutoAdjustFleetStrengthMajor();
			triggerSetPirateFleet();
			triggerSpawnFleetNear(system.getCenter(), null, null);
			triggerOrderFleetPatrol(system, true, Tags.STATION, Tags.JUMP_POINT);
			endTrigger();
		}
		
		return true;
	}

	protected void updateInteractionDataImpl() {
		set("$psb_barEvent", isBarEvent());
		set("$psb_manOrWoman", getPerson().getManOrWoman());
		set("$psb_baseBounty", Misc.getWithDGS(baseBounty));
		set("$psb_days", "" + (int) BOUNTY_DAYS);
		set("$psb_systemName", system.getNameWithLowercaseType());
		set("$psb_systemNameShort", system.getNameWithLowercaseTypeShort());
		set("$psb_baseName", market.getName());
		set("$psb_dist", getDistanceLY(market));
	}
	
	@Override
	public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
		Color tc = getBulletColorForMode(ListInfoMode.IN_DESC);
		if (currentStage == Stage.BOUNTY) {
			float elapsed = getElapsedInCurrentStage();
			int d = (int) Math.round(BOUNTY_DAYS - elapsed);
			PersonAPI person = getPerson();
			
			String locStr = "in or near the " + market.getStarSystem().getNameWithLowercaseType();
			
			info.addPara("Applies to all %s fleets " + locStr + ", which is home to " + market.getName() + ", a pirate base.",
					opad, enemy.getBaseUIColor(), enemy.getPersonNamePrefix());

			if (isEnding()) {
				info.addPara("This bounty is no longer on offer.", opad);
				return;
			}
			
			bullet(info);
			info.addPara("%s base reward per frigate", opad, tc, h, Misc.getDGSCredits(baseBounty));
			addDays(info, "remaining", d, tc);
			unindent(info);
			
			info.addPara("Payment depends on the number and size of ships destroyed. " +
						 "Standing with " + faction.getDisplayNameWithArticle() + ", as well as " +
						 "with " + person.getNameString() + ", will improve.",
						 opad);
			
		} else if (currentStage == Stage.DONE) {
			info.addPara("This bounty is no longer on offer.", opad);
		}
		
		if (latestResult != null) {
			//Color color = faction.getBaseUIColor();
			//Color dark = faction.getDarkUIColor();
			//info.addSectionHeading("Most Recent Reward", color, dark, Alignment.MID, opad);
			info.addPara("Most recent bounty payment:", opad);
			bullet(info);
			info.addPara("%s received", pad, tc, h, Misc.getDGSCredits(latestResult.payment));
			if (Math.round(latestResult.fraction * 100f) < 100f) {
				info.addPara("%s share based on damage dealt", 0f, tc, h, 
						"" + (int) Math.round(latestResult.fraction * 100f) + "%");
			}
			if (latestResult.repPerson != null) {
				CoreReputationPlugin.addAdjustmentMessage(latestResult.repPerson.delta, null, getPerson(), 
														  null, null, info, tc, false, 0f);
			}
			if (latestResult.repFaction != null) {
				CoreReputationPlugin.addAdjustmentMessage(latestResult.repFaction.delta, faction, null, 
														  null, null, info, tc, false, 0f);
			}
			unindent(info);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		
		boolean isUpdate = getListInfoParam() != null;
		if (isUpdate && latestResult == getListInfoParam()) {
			info.addPara("%s received", pad, tc, h, Misc.getDGSCredits(latestResult.payment));
			if (Math.round(latestResult.fraction * 100f) < 100f) {
				info.addPara("%s share based on damage dealt", 0f, tc, h, 
						"" + (int) Math.round(latestResult.fraction * 100f) + "%");
			}
			if (latestResult.repPerson != null) {
				CoreReputationPlugin.addAdjustmentMessage(latestResult.repPerson.delta, null, getPerson(), 
													  	null, null, info, tc, isUpdate, 0f);
			}
			if (latestResult.repFaction != null) {
				CoreReputationPlugin.addAdjustmentMessage(latestResult.repFaction.delta, faction, null, 
													  	null, null, info, tc, isUpdate, 0f);
			}
			return true;
		}
		
		if (currentStage == Stage.BOUNTY) {
			float elapsed = getElapsedInCurrentStage();
			int d = (int) Math.round(BOUNTY_DAYS - elapsed);
			
			info.addPara("%s base reward per frigate", pad, tc, h, Misc.getDGSCredits(baseBounty));
			addDays(info, "remaining", d, tc);
			return true;
		} else if (currentStage == Stage.DONE) {
			return false;
		}
		return false;
	}
	
	public String getPostfixForState() {
		if (currentStage == Stage.DONE) return " - Over";
		return super.getPostfixForState();
	}

	@Override
	public String getBaseName() {
		return "Pirate Fleet Bounty";
	}
	
	protected String getMissionTypeNoun() {
		return "bounty";
	}
	

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return getMapLocationFor(market.getPrimaryEntity());
	}
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		Global.getSector().getListenerManager().addListener(this);
//		AddRemoveCommodity.addCreditsLossText(cost, dialog.getTextPanel());
//		Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(cost);
//		adjustRep(dialog.getTextPanel(), null, RepActions.MISSION_SUCCESS);
//		addPotentialContacts(dialog);
	}
	
	@Override
	protected void notifyEnding() {
		Global.getSector().getListenerManager().removeListener(this);
		super.notifyEnding();
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isEnded() || isEnding()) return;
		
		if (!battle.isPlayerInvolved()) return;
		
		if (!Misc.isNear(primaryWinner, market.getLocationInHyperspace())) return;
		
		int payment = 0;
		float fpDestroyed = 0;
		for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
			if (enemy != otherFleet.getFaction()) continue;
			
			float bounty = 0;
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
				float mult = Misc.getSizeNum(loss.getHullSpec().getHullSize());
				bounty += mult * baseBounty;
				fpDestroyed += loss.getFleetPointCost();
			}
			
			payment += (int) (bounty * battle.getPlayerInvolvementFraction());
		}
	
		if (payment > 0) {
			Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);
			
			float repFP = (int)(fpDestroyed * battle.getPlayerInvolvementFraction());
			
			float fDelta = 0f;
			float pDelta = 0f;
			if (repFP < 30) {
				fDelta = RepRewards.TINY;
				pDelta = RepRewards.TINY;
			} else if (repFP < 70) {
				fDelta = RepRewards.SMALL;
				pDelta = RepRewards.SMALL;
			} else {
				fDelta = RepRewards.SMALL;
				pDelta = RepRewards.MEDIUM;
			}
			
			MissionCompletionRep completionRepPerson = new MissionCompletionRep(
											pDelta, getRewardLimitPerson(), 0, null);
			MissionCompletionRep completionRepFaction = new MissionCompletionRep(
											fDelta, getRewardLimitFaction(), 0, null);
			
			boolean addContacts = latestResult == null;
			latestResult = new BountyResult(payment, battle.getPlayerInvolvementFraction(), null, null);
			if (pDelta != 0) {
				ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.MISSION_SUCCESS, completionRepPerson,
								null, true, false), 
								getPerson());
				latestResult.repPerson = rep;
			}

			if (completionRepFaction.successDelta != 0) {
				ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.MISSION_SUCCESS, completionRepFaction,
								null, true, false), 
								getPerson().getFaction().getId());
				latestResult.repFaction = rep;
			}
			
			sendUpdateIfPlayerHasIntel(latestResult, false);
			
			if (addContacts) {
				addPotentialContacts(null);
			}
		}
	}
	
}











