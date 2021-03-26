package com.fs.starfarer.api.impl.campaign.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseOnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class InvestigationEventGoodRepWithOther extends BaseEventPlugin {
	public static final String PERSON_CHECKOUT_REASON = "IGR_investigator";
	
	public static class InvestigationGoodRepData {
		protected FactionAPI faction;
		protected FactionAPI other;
		public InvestigationGoodRepData(FactionAPI faction, FactionAPI other) {
			this.faction = faction;
			this.other = other;
		}
		public InvestigationGoodRepData(String faction, String other) {
			this.faction = Global.getSector().getFaction(faction);
			this.other = Global.getSector().getFaction(other);
		}
		public FactionAPI getFaction() {
			return faction;
		}
		public FactionAPI getOther() {
			return other;
		}
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((faction == null) ? 0 : faction.hashCode());
			result = prime * result + ((other == null) ? 0 : other.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InvestigationGoodRepData other = (InvestigationGoodRepData) obj;
			if (faction == null) {
				if (other.faction != null)
					return false;
			} else if (faction != other.faction)
				return false;
			if (this.other == null) {
				if (other.other != null)
					return false;
			} else if (this.other != other.other)
				return false;
			return true;
		}
	}
	
	
	public static Logger log = Global.getLogger(InvestigationEventGoodRepWithOther.class);
	
	private float elapsedDays = 0f;
	private float duration = 0f;
	
	private InvestigationGoodRepData target = null;
	
	private float guiltProb = 0f;
	private int bribeAmount = 1;
	private boolean bribed = false;
	private PersonAPI investigator;
	private FactionAPI other;
	private boolean ended = false;
	private float numPrior;
	private RepActions punishment = null;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
	}
	
	public void startEvent() {
		super.startEvent();
		
		
		if (!(eventTarget.getCustom() instanceof InvestigationGoodRepData)) {
			endEvent();
			return;
		}
		target = (InvestigationGoodRepData) eventTarget.getCustom();
		faction = target.faction;
		other = target.other;
		
		numPrior = faction.getMemoryWithoutUpdate().getFloat(MemFlags.MEMORY_KEY_NUM_GR_INVESTIGATIONS);
		
		pickMarket();
		if (market == null) {
			endEvent();
			return;
		}
		
		if (faction.isAtBest(Factions.PLAYER, RepLevel.HOSTILE) ||
				other.isAtBest(Factions.PLAYER, RepLevel.FAVORABLE) ||
				faction.isAtWorst(other, RepLevel.COOPERATIVE) ||
				!faction.getCustom().optBoolean(Factions.CUSTOM_INVESTIGATES_PLAYER_FOR_GOOD_REP) ||
				!other.getCustom().optBoolean(Factions.CUSTOM_WORTH_INVESTIGATING_FOR_GOOD_REP) ||
				faction == other) {
			// shouldn't have gotten here, abort
			endEvent();
			return;
		}
		
		
		float repFaction = faction.getRelationship(Factions.PLAYER);
		float repOther = other.getRelationship(Factions.PLAYER);
		RepLevel relFaction = faction.getRelationshipLevel(Factions.PLAYER);
		RepLevel relOther = other.getRelationshipLevel(Factions.PLAYER);
		
//		switch (relOther) {
//		case WELCOMING:
//			guiltProb = 0.25f + numPrior * 0.1f;
//			punishment = RepActions.OTHER_FACTION_GOOD_REP_INVESTIGATION_MINOR;
//			break;
//		case FRIENDLY:
//			guiltProb = 0.5f + numPrior * 0.1f;
//			punishment = RepActions.OTHER_FACTION_GOOD_REP_INVESTIGATION_MAJOR;
//			break;
//		case COOPERATIVE:
//			guiltProb = 1f;
//			punishment = RepActions.OTHER_FACTION_GOOD_REP_INVESTIGATION_CRITICAL;
//			break;
//		}
		switch ((int) numPrior) {
		case 0:
			guiltProb = 0.25f;
			punishment = RepActions.OTHER_FACTION_GOOD_REP_INVESTIGATION_MINOR;
			break;
		case 1:
			guiltProb = 0.5f;
			punishment = RepActions.OTHER_FACTION_GOOD_REP_INVESTIGATION_MAJOR;
			break;
		default:
			guiltProb = 1f;
			punishment = RepActions.OTHER_FACTION_GOOD_REP_INVESTIGATION_CRITICAL;
			break;
		}
		bribeAmount = (int) (10000 + (int)(10000 * (numPrior + guiltProb * 2f + (float) Math.random())) / 1000 * 1000);

		
		duration = 60f + 30f * (float) Math.random();
		//duration = 1f;
		
		
		log.info(String.format("Starting faction investigation at %s, %s -> %s", market.getName(), faction.getDisplayName(), other.getDisplayName()));
		
		investigator = Global.getSector().getImportantPeople().getPerson(market.getFaction(), market,
							PERSON_CHECKOUT_REASON, Ranks.CITIZEN, Ranks.POST_INVESTIGATOR).getPerson();
		market.getCommDirectory().addPerson(investigator);

		investigator.getMemoryWithoutUpdate().set("$igr_eventRef", this, duration);
		investigator.getMemoryWithoutUpdate().set("$igr_investigator", true, duration);
		investigator.getMemoryWithoutUpdate().set("$igr_bribeAmount", "" + bribeAmount, duration);
		investigator.getMemoryWithoutUpdate().set("$igr_bribeAmountDGS", Misc.getWithDGS(bribeAmount), duration);
		Misc.setFlagWithReason(investigator.getMemoryWithoutUpdate(), 
				MemFlags.MEMORY_KEY_REQUIRES_DISCRETION, "igr",
				true, duration);
		
		Global.getSector().reportEventStage(this, "start_goodrep", null, MessagePriority.ENSURE_DELIVERY, null);
	}
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsedDays += days;
		
		if (elapsedDays >= duration) {
			if (!bribed && 
					!other.isAtBest(Factions.PLAYER, RepLevel.FAVORABLE) &&
					(float) Math.random() < guiltProb) {
				Global.getSector().reportEventStage(this, "player_guilty_goodrep", null, MessagePriority.ENSURE_DELIVERY,  new BaseOnMessageDeliveryScript() {
					public void beforeDelivery(CommMessageAPI message) {
						if (punishment != null) {
							Global.getSector().adjustPlayerReputation(
									new RepActionEnvelope(punishment, null, null, true), 
														  market.getFactionId());
						}
					}
				});
			} else {
				if (bribed) {
					Global.getSector().reportEventStage(this, "clear_goodrep_bribe", null, MessagePriority.ENSURE_DELIVERY, null);
				} else {
					Global.getSector().reportEventStage(this, "clear_goodrep", null, MessagePriority.ENSURE_DELIVERY, null);
				}
			}
			numPrior++;
			faction.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NUM_GR_INVESTIGATIONS, numPrior);
			endEvent();
		}
	}
	
	@Override
	public String getEventName() {
		if (isDone()) {
			return other.getDisplayName() + " ties investigation - " + faction.getDisplayName() + " (over)";
		}
		return other.getDisplayName() + " ties investigation - " + faction.getDisplayName() + "";
//		if (isDone()) {
//			return faction.getDisplayName() + " investigation - " + other.getDisplayName() + " ties (over)";
//		}
//		return faction.getDisplayName() + " investigation - " + other.getDisplayName() + " ties";
//		if (isDone()) {
//			return "Faction Investigation - " + market.getName() + " (over)";
//		}
//		return "Faction Investigation - " + market.getName();
	}
	
	

	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, 
							List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		if (action.equals("paidBribe")) {
			return bribed;
		} else if (action.equals("setBribePaid")) {
			bribed = true;
			return true;
		}
		return true;
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();
		
		map.put("$otherFaction", other.getDisplayName());
		map.put("$OtherFaction", Misc.ucFirst(other.getDisplayName()));
		map.put("$TheOtherFaction", Misc.ucFirst(other.getDisplayNameWithArticle()));
		map.put("$theOtherFaction", other.getDisplayNameWithArticle());
		map.put("$TheOtherFactionLong", Misc.ucFirst(other.getDisplayNameLongWithArticle()));
		map.put("$theOtherFactionLong", other.getDisplayNameLongWithArticle());
		
		map.put("$InvestigatorPost", Misc.ucFirst(investigator.getPost()));
		map.put("$investigatorName", investigator.getName().getFullName());
		if (investigator.isMale()) {
			map.put("$invHimOrHer", "him");
		} else {
			map.put("$invHimOrHer", "her");
		}
		
		map.put("$sender", "Unknown");
		map.put("$duration", Misc.getAtLeastStringForDays((int)duration));
		
		return map;
	}
	
	public String[] getHighlights(String stageId) {
		List<String> result = new ArrayList<String>();
		addTokensToList(result, "$duration");
		return result.toArray(new String[0]);
	}


	protected void endEvent() {
		if (investigator != null) {
			investigator.getMemoryWithoutUpdate().unset("$igr_eventRef");
			investigator.getMemoryWithoutUpdate().unset("$igr_investigator");
			investigator.getMemoryWithoutUpdate().unset("$igr_bribeAmount");
			investigator.getMemoryWithoutUpdate().unset("$igr_bribeAmountDGS");
			Misc.setFlagWithReason(investigator.getMemoryWithoutUpdate(), 
					MemFlags.MEMORY_KEY_REQUIRES_DISCRETION, "igr",
					false, 0f);
			
			Global.getSector().getImportantPeople().returnPerson(investigator, PERSON_CHECKOUT_REASON);
			if (!Global.getSector().getImportantPeople().isCheckedOutForAnything(investigator)) {
				market.getCommDirectory().removePerson(investigator);
			}
		}
		ended = true;
	}

	public boolean isDone() {
		return ended;
	}

	private void pickMarket() {
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getFaction() != faction) continue;
			
			if (market.hasCondition(Conditions.DECIVILIZED)) continue;
			if (market.getSize() < 5) continue;
			
			float weight = market.getSize();
			
			picker.add(market, weight);
		}
		
		market = picker.pick();
		if (market == null) return;
		//market = Global.getSector().getEconomy().getMarket("jangala");
		eventTarget.setEntity(market.getPrimaryEntity());
		eventTarget.setLocation(market.getContainingLocation());
	}
}







