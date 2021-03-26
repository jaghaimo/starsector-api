package com.fs.starfarer.api.impl.campaign.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseOnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
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
import com.fs.starfarer.api.util.Misc.Token;

/**
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class InvestigationEventSmugglingV2 extends BaseEventPlugin {

	public static final String PERSON_CHECKOUT_REASON = "ISE_investigator";
	
	public static class InvSmugglingParams {
		public float guiltChance;
		public int bribeAmount;

		public InvSmugglingParams() {
		}
	}
	
	public static Logger log = Global.getLogger(InvestigationEventSmugglingV2.class);
	
	private float elapsedDays = 0f;
	private float duration = 0f;
	
	private InvSmugglingParams params;
	private boolean bribed = false;
	private PersonAPI investigator;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
		params = new InvSmugglingParams();
	}
	
	public void startEvent() {
		super.startEvent();
		
		if (market == null || market.hasCondition(Conditions.DECIVILIZED)) {
			endEvent();
			return;
		}
		if (market.getFaction().isAtBest(Factions.PLAYER, RepLevel.HOSTILE)) {
			endEvent();
			return;
		}
//		float recentBMTrade = 0;
//		float recentOpenTrade = 0;
// 
//		for (SubmarketAPI sub : market.getSubmarketsCopy()) {
//			if (!sub.getPlugin().isParticipatesInEconomy()) continue;
//			
//			PlayerTradeDataForSubmarket tradeData =  SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(sub);
//			
//			if (sub.getPlugin().isBlackMarket()) {
//				recentBMTrade += tradeData.getRecentBaseTradeValueImpact();
//			} else {
//				recentOpenTrade += tradeData.getRecentBaseTradeValueImpact();
//			}
//		}
//		
//		// 10000 credits of "impact" is 1000 credit of actual trade
//		if (recentBMTrade > recentOpenTrade * 0.5f && recentBMTrade > 10000f) {
//			params.guiltChance = recentBMTrade / Math.min(recentBMTrade * 10f, market.getTradeVolume());
//			params.bribeAmount = (int) (params.guiltChance * 100000f);
//		} else {
//			endEvent();
//			return;
//		}
		
		params.guiltChance = Math.max(0.1f, Math.min(startProbability, 0.9f));
		params.bribeAmount = (int) ((10000f + params.guiltChance * 90000f) / 1000f) * 1000;

//		// don't bother with trifling investigations
//		if (params.guiltChance < .01f) {
//			endEvent();
//			return;
//		}
		
		
		
		duration = 90f + 30f * (float) Math.random();
		//duration = 5f;
		
		log.info(String.format("Starting smuggling investigation at %s", market.getName()));
		
		investigator = Global.getSector().getImportantPeople().getPerson(market.getFaction(), market,
							PERSON_CHECKOUT_REASON, Ranks.CITIZEN, Ranks.POST_INVESTIGATOR).getPerson();
		market.getCommDirectory().addPerson(investigator);

		investigator.getMemoryWithoutUpdate().set("$ise_eventRef", this, duration);
		investigator.getMemoryWithoutUpdate().set("$ise_investigator", true, duration);
		investigator.getMemoryWithoutUpdate().set("$ise_bribeAmount", "" + params.bribeAmount, duration);
		investigator.getMemoryWithoutUpdate().set("$ise_bribeAmountDGS", Misc.getWithDGS(params.bribeAmount), duration);
		Misc.setFlagWithReason(investigator.getMemoryWithoutUpdate(), 
				MemFlags.MEMORY_KEY_REQUIRES_DISCRETION, "ies",
				true, duration);
		
		Global.getSector().reportEventStage(this, "start_smuggling", null, MessagePriority.ENSURE_DELIVERY, null);
	}
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsedDays += days;
		
		if (elapsedDays >= duration) {
			if (!bribed && (float) Math.random() < params.guiltChance) {
				Global.getSector().reportEventStage(this, "player_guilty_smuggling", null, MessagePriority.ENSURE_DELIVERY,  new BaseOnMessageDeliveryScript() {
					public void beforeDelivery(CommMessageAPI message) {
						Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.SMUGGLING_INVESTIGATION_GUILTY, null, null, true), 
								market.getFactionId());
					}
				});
			} else {
				if (bribed) {
					Global.getSector().reportEventStage(this, "clear_smuggling_bribe", null, MessagePriority.ENSURE_DELIVERY, null);
				} else {
					Global.getSector().reportEventStage(this, "clear_smuggling", null, MessagePriority.ENSURE_DELIVERY, null);
				}
			}
			endEvent();
		}
	}
	
	@Override
	public String getEventName() {
		if (isDone()) {
			return "Smuggling investigation - " + market.getName() + " (over)";
		}
		return "Smuggling investigation - " + market.getName();
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

	private boolean ended = false;
	protected void endEvent() {
		if (investigator != null) {
			investigator.getMemoryWithoutUpdate().unset("$ise_eventRef");
			investigator.getMemoryWithoutUpdate().unset("$ise_investigator");
			investigator.getMemoryWithoutUpdate().unset("$ise_bribeAmount");
			investigator.getMemoryWithoutUpdate().unset("$ise_bribeAmountDGS");
			Misc.setFlagWithReason(investigator.getMemoryWithoutUpdate(), 
					MemFlags.MEMORY_KEY_REQUIRES_DISCRETION, "ies",
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

//	public static float getPlayerRepGuiltMult(FactionAPI faction) {
//		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
//		//RepLevel level = market.getFaction().getRelationshipLevel(player);
//		RepLevel level = faction.getRelationshipLevel(player);
//		switch (level) {
//		case COOPERATIVE:
//			return 0.1f;
//		case FRIENDLY:
//			return 0.2f;
//		case WELCOMING:
//			return 0.3f;
//		case FAVORABLE:
//			return 0.5f;
//		case NEUTRAL:
//			return 1f;
//		case SUSPICIOUS:
//			return 1.5f;
//		case INHOSPITABLE:
//			return 2f;
//		case HOSTILE:
//			return 5f;
//		case VENGEFUL:
//			return 10f;
//		}
//		return 1f;
//	}
	
}




