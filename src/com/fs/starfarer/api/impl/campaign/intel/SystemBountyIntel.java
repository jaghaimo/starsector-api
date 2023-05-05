package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SystemBountyIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener {
	public static Logger log = Global.getLogger(SystemBountyIntel.class);

	public static float MAX_DURATION = 60;

	protected MarketAPI market;
	protected LocationAPI location;
	protected float elapsedDays = 0f;
	protected float duration = MAX_DURATION;
	
	protected float baseBounty = 0;
	
	protected FactionAPI faction = null;
	protected FactionAPI enemyFaction = null;
	protected SystemBountyResult latestResult;
	
	protected boolean commerceMode = false; // due to Commerce industry in player system

	protected MilitaryResponseScript script;
	
	public SystemBountyIntel(MarketAPI market) {
		this(market, -1, false);
	}
	public SystemBountyIntel(MarketAPI market, int baseReward, boolean commerceMode) {
		this.market = market;
		this.commerceMode = commerceMode;
		
		location = market.getContainingLocation();
		faction = market.getFaction();
		if (commerceMode) {
			faction = Global.getSector().getFaction(Factions.INDEPENDENT);
		}
		
		if (!commerceMode && market.getFaction().isPlayerFaction()) {
			endImmediately();
			return;
		}
		
		baseBounty = Global.getSettings().getFloat("baseSystemBounty");
		float marketSize = market.getSize();
		
		baseBounty *= (marketSize + 5f) / 10f;
		
		//float lowStabilityMult = BaseMarketConditionPlugin.getLowStabilityPenaltyMult(market);
		float highStabilityMult = BaseMarketConditionPlugin.getHighStabilityBonusMult(market);
		highStabilityMult = 1f + (highStabilityMult - 1f) * 0.5f;
		
		//baseBounty *= lowStabilityMult;
		baseBounty *= highStabilityMult;
		
		baseBounty = (int) baseBounty;
		if (baseReward > 0) {
			baseBounty = baseReward;
		}
		
		log.info(String.format("Starting bounty at market [%s], %d credits per frigate", market.getName(), (int) baseBounty));
		
		updateLikelyCauseFaction();
		
		
		//conditionToken = market.addCondition(Conditions.EVENT_SYSTEM_BOUNTY, this);
		
		if (commerceMode) {
			Global.getSector().getIntelManager().addIntel(this);
		} else {
			Global.getSector().getIntelManager().queueIntel(this);
		}
		
		Global.getSector().getListenerManager().addListener(this);
		
		if (!commerceMode) {
			MilitaryResponseParams params = new MilitaryResponseParams(ActionType.HOSTILE, 
					"system_bounty_" + market.getId(), 
					getFactionForUIColors(),
					market.getPrimaryEntity(),
					0.75f,
					duration);
			script = new MilitaryResponseScript(params);
			location.addScript(script);
		}
	}
	
	public void reportMadeVisibleToPlayer() {
		if (!isEnding() && !isEnded()) {
			duration = Math.max(duration * 0.5f, Math.min(duration * 2f, MAX_DURATION));
		}
	}
	
	public float getElapsedDays() {
		return elapsedDays;
	}

	public void setElapsedDays(float elapsedDays) {
		this.elapsedDays = elapsedDays;
	}
	
	public void reset() {
		elapsedDays = 0f;
		endingTimeRemaining = null;
		ending = null;
		ended = null;
		script.setElapsed(0f);
		if (!Global.getSector().getListenerManager().hasListener(this)) {
			Global.getSector().getListenerManager().addListener(this);
		}
	}



	public MarketAPI getMarket() {
		return market;
	}
	
	
	private void updateLikelyCauseFaction() {
		int maxSize = 0;
		MarketAPI maxOther = null;
		for (MarketAPI other : Misc.getNearbyMarkets(market.getLocationInHyperspace(), 0f)) {
			if (market.getFaction() == other.getFaction()) continue;
			if (!market.getFaction().isHostileTo(other.getFaction())) continue;
			
			int size = other.getSize();
			if (size > maxSize) {
				maxSize = size;
				maxOther = other;
			}
		}
		
		if (maxOther != null) {
			enemyFaction = maxOther.getFaction();
		} else {
			enemyFaction = Global.getSector().getFaction(Factions.PIRATES);
		}
		
	}
	
	@Override
	protected void advanceImpl(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		elapsedDays += days;

		if (elapsedDays >= duration && !isDone() && !commerceMode) {
			endAfterDelay();
			boolean current = market.getContainingLocation() == Global.getSector().getCurrentLocation();
			sendUpdateIfPlayerHasIntel(new Object(), !current);
			return;
		}
		if (faction != market.getFaction() || !market.isInEconomy()) {
			endAfterDelay();
			boolean current = market.getContainingLocation() == Global.getSector().getCurrentLocation();
			sendUpdateIfPlayerHasIntel(new Object(), !current);
			return;
		}
	}
	
	public float getTimeRemainingFraction() {
		if (commerceMode) return 1f;
		float f = 1f - elapsedDays / duration;
		return f;
	}
	
	

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		log.info(String.format("Ending bounty at market [%s]", market.getName()));
		
		Global.getSector().getListenerManager().removeListener(this);
		
		location.removeScript(script);
		
		//market.removeSpecificCondition(conditionToken);
	}
	
	
	public static class SystemBountyResult {
		public int payment;
		public float fraction;
		public ReputationAdjustmentResult rep;
		public SystemBountyResult(int payment, float fraction, ReputationAdjustmentResult rep) {
			this.payment = payment;
			this.fraction = fraction;
			this.rep = rep;
		}
		
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
			if (commerceMode) {
				if (!market.getFaction().isHostileTo(otherFleet.getFaction()) && 
						!otherFleet.getFaction().isHostileTo(Factions.INDEPENDENT)) continue;
				
				if (Misc.isTrader(otherFleet)) continue;
				if (Factions.INDEPENDENT.equals(otherFleet.getFaction().getId())) continue;
			} else {
				if (!market.getFaction().isHostileTo(otherFleet.getFaction())) continue;
			}
			
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
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.SYSTEM_BOUNTY_REWARD, new Float(repFP), null, null, true, false), 
							market.getFaction().getId());
			latestResult = new SystemBountyResult(payment, battle.getPlayerInvolvementFraction(), rep);
			sendUpdateIfPlayerHasIntel(latestResult, false);
		}
	}
	
	public boolean runWhilePaused() {
		return false;
	}
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		if (isEnding() && isUpdate) {
			//info.addPara("Over", initPad);
		} else {
			if (isUpdate && latestResult != null) {
				info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(latestResult.payment));
				if (Math.round(latestResult.fraction * 100f) < 100f) {
					info.addPara("%s share based on damage dealt", 0f, tc, h, 
							"" + (int) Math.round(latestResult.fraction * 100f) + "%");
				}
				CoreReputationPlugin.addAdjustmentMessage(latestResult.rep.delta, faction, null, 
														  null, null, info, tc, isUpdate, 0f);
			} else if (mode == ListInfoMode.IN_DESC) {
				info.addPara("%s base reward per frigate", initPad, tc, h, Misc.getDGSCredits(baseBounty));
				if (!commerceMode) {
					addDays(info, "remaining", duration - elapsedDays, tc);
				}
			} else {
				if (!isEnding()) {
					info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
								 faction.getBaseUIColor(), faction.getDisplayName());
					info.addPara("%s base reward per frigate", 0f, tc, h, Misc.getDGSCredits(baseBounty));
					if (!commerceMode) {
						addDays(info, "remaining", duration - elapsedDays, tc);
					}
				}
			}
		}
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	public String getSortString() {
		return "System Bounty";
	}
	
	public String getName() {
		String name = market.getName();
		StarSystemAPI system = market.getStarSystem();
		if (system != null) {
			name = system.getBaseName();
		}
		if (isEnding()) {
			return "Bounty Ended - " + name;
		}
		return "System Bounty - " + name;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
		//return null;
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		createSmallDescription(info, width, height, false);
	}
	public void createSmallDescription(TooltipMakerAPI info, float width, float height, 
									   boolean forMarketConditionTooltip) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		//info.addPara(getName(), c, 0f);
		
		//info.addSectionHeading(getName(), Alignment.MID, 0f);
		
		if (!forMarketConditionTooltip) {
			info.addImage(faction.getLogo(), width, 128, opad);
		}

		String locStr = "near " + market.getName();
		if (market.getStarSystem() != null) {
			locStr = "in or near the " + market.getStarSystem().getNameWithLowercaseType();
		}
		
		if (commerceMode) {
			info.addPara("%s commercial concerns " + market.getOnOrAt() + " " + market.getName() + 
					" have banded together and posted a modest but long-term bounty on all "
					+ "hostile fleets " + locStr + ".",
					opad, faction.getBaseUIColor(), Misc.ucFirst(faction.getPersonNamePrefix()));
			info.addPara("The bounty stipulates that trade fleets are an exception - "
					+ "attacking them will not result in a reward, regardless of their faction.", opad);
		} else {
			info.addPara("%s authorities " + market.getOnOrAt() + " " + market.getName() + 
					" have posted a bounty on all hostile fleets " + locStr + ".",
					opad, faction.getBaseUIColor(), Misc.ucFirst(faction.getPersonNamePrefix()));
		}

		if (isEnding()) {
			info.addPara("This bounty is no longer on offer.", opad);
			return;
		}
		
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//			info.addPara("Listener not registered!", opad);
//		}
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		if (!commerceMode) {
			if (enemyFaction != null) {
				info.addPara("Likely triggered by %s activity.",
						opad, enemyFaction.getBaseUIColor(), enemyFaction.getPersonNamePrefix());
			}
		} else {
			info.addPara("Triggered by the presence of Commerce on one of your colonies in-system, and by the "
					+ "level of hostile activity.", opad);
		}
		
		
		info.addPara("Payment depends on the number and size of ships destroyed. " +
					 "Standing with " + faction.getDisplayNameWithArticle() + " may also improve.",
					 opad);
//					 opad, faction.getBaseUIColor(),
//					 faction.getDisplayNameWithArticleWithoutArticle());
		
		
		if (!commerceMode) {
			String isOrAre = faction.getDisplayNameIsOrAre();
			FactionCommissionIntel temp = new FactionCommissionIntel(faction);
			List<FactionAPI> hostile = temp.getHostileFactions();
			if (hostile.isEmpty()) {
				info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + isOrAre + " not currently hostile to any major factions.", 0f);
			} else {
				info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + isOrAre + " currently hostile to:", opad);
				
				info.setParaFontDefault();
				
				info.setBulletedListMode(BaseIntelPlugin.INDENT);
				float initPad = pad;
				for (FactionAPI other : hostile) {
					info.addPara(Misc.ucFirst(other.getDisplayName()), other.getBaseUIColor(), initPad);
					initPad = 0f;
				}
				info.setBulletedListMode(null);
			}
		}
		
		if (latestResult != null) {
			//Color color = faction.getBaseUIColor();
			//Color dark = faction.getDarkUIColor();
			//info.addSectionHeading("Most Recent Reward", color, dark, Alignment.MID, opad);
			info.addPara("Most recent bounty:", opad);
			bullet(info);
			info.addPara("%s received", pad, tc, h, Misc.getDGSCredits(latestResult.payment));
			if (Math.round(latestResult.fraction * 100f) < 100f) {
				info.addPara("%s share based on damage dealt", 0f, tc, h, 
						"" + (int) Math.round(latestResult.fraction * 100f) + "%");
			}
			CoreReputationPlugin.addAdjustmentMessage(latestResult.rep.delta, faction, null, 
													  null, null, info, tc, false, 0f);
			unindent(info);
		}

	}
	
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "system_bounty");
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_BOUNTY);
		tags.add(faction.getId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return market.getPrimaryEntity();
	}

	
	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public float getBaseBounty() {
		return baseBounty;
	}

	public void setBaseBounty(float baseBounty) {
		this.baseBounty = baseBounty;
	}
	public boolean isCommerceMode() {
		return commerceMode;
	}
	
	public void setCommerceMode(boolean commerceMode) {
		this.commerceMode = commerceMode;
	}
	public LocationAPI getLocation() {
		return location;
	}
	
}


