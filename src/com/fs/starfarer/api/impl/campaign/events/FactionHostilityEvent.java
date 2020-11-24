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
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class FactionHostilityEvent extends BaseEventPlugin {
	
	public static final float HOSTILITY_PENALTY = 0.2f;
	
	public static class FactionHostilityPairKey {
		protected FactionAPI one;
		protected FactionAPI two;
		public FactionHostilityPairKey(FactionAPI one, FactionAPI two) {
			this.one = one;
			this.two = two;
			if (one.getId().compareTo(two.getId()) > 0) {
				FactionAPI temp = one;
				one = two;
				two = temp;
			}
		}
		public FactionHostilityPairKey(String one, String two) {
			this(Global.getSector().getFaction(one), Global.getSector().getFaction(two));
		}
		public FactionAPI getOne() {
			return one;
		}
		public FactionAPI getTwo() {
			return two;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((one == null) ? 0 : one.hashCode());
			result = prime * result + ((two == null) ? 0 : two.hashCode());
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
			FactionHostilityPairKey other = (FactionHostilityPairKey) obj;
			if (one == null) {
				if (other.one != null)
					return false;
			} else if (one != other.one)
				return false;
			if (this.two == null) {
				if (other.two != null)
					return false;
			} else if (this.two != other.two)
				return false;
			return true;
		}
	}
	
	
	public static Logger log = Global.getLogger(FactionHostilityEvent.class);
	
	protected float elapsedDays = 0f;
	protected float duration = 0f;
	
	protected FactionHostilityPairKey target = null;
	protected FactionAPI one, two;
	protected boolean ended = false;
	protected float prevRel = 0f;
	
	protected float prevRelOne = 0f;
	protected float prevRelTwo = 0f;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget, false);
	}
	
	public void startEvent() {
		super.startEvent(true);
		
		if (!(eventTarget.getCustom() instanceof FactionHostilityPairKey)) {
			endEvent();
			return;
		}
		target = (FactionHostilityPairKey) eventTarget.getCustom();
		one = target.one;
		two = target.two;
		
		duration = 365f * (0.5f + 0.5f * (float) Math.random());
		
		log.info(String.format("Starting hostility event: %s -> %s", one.getDisplayName(), two.getDisplayName()));
		
		FactionAPI commFac = Misc.getCommissionFaction();
		if (commFac != null && (commFac == one || commFac == two)) {
			Global.getSector().reportEventStage(this, "warning", Global.getSector().getPlayerFleet(), MessagePriority.ENSURE_DELIVERY, null);
		}
	}
	
	protected void startHostilities() {
		log.info(String.format("Making factions hostile: %s -> %s", one.getDisplayName(), two.getDisplayName()));
		prevRel = one.getRelationship(two.getId());
		one.setRelationship(two.getId(), RepLevel.HOSTILE);
		
		prevRelOne = one.getRelationship(Factions.PLAYER);
		prevRelTwo = two.getRelationship(Factions.PLAYER);
		
		final FactionAPI commFac = Misc.getCommissionFaction();
		if (commFac != null && (commFac == one || commFac == two)) {
			Global.getSector().reportEventStage(this, "start", Global.getSector().getPlayerFleet(), 
					MessagePriority.ENSURE_DELIVERY, new BaseOnMessageDeliveryScript() {
				public void beforeDelivery(CommMessageAPI message) {
					Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.MAKE_HOSTILE_AT_BEST, null, message, true), 
							(commFac == one ? two : one).getId());					
				}
			});
		} else {
			Global.getSector().reportEventStage(this, "start", Global.getSector().getPlayerFleet(), MessagePriority.ENSURE_DELIVERY, null);
		}
	}
	
	//protected boolean started = false;
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsedDays += days;
		
		if (!started && elapsedDays > 10f) {
			startHostilities();
			started = true;
		}
		
		if (elapsedDays >= duration) {
			endEvent();
		}
	}
	
	protected void endEvent() {
		if (ended) return;
		
		ended = true;
		
		one.setRelationship(two.getId(), Math.max(0f, prevRel));
		
		Global.getSector().reportEventStage(this, "end", Global.getSector().getPlayerFleet(), MessagePriority.ENSURE_DELIVERY, new BaseOnMessageDeliveryScript() {
			public void beforeDelivery(CommMessageAPI message) {
				FactionAPI commFac = Misc.getCommissionFaction();
				if (commFac == null) return;
				if (commFac != one && commFac != two) return;
				
				FactionAPI other = one;
				float prevRel = prevRelOne;
				if (other == commFac) {
					other = two;
					prevRel = prevRelTwo;
				}
				
				float currRel = other.getRelationship(Factions.PLAYER);
				CustomRepImpact impact = new CustomRepImpact();
				impact.delta = (prevRel - currRel - HOSTILITY_PENALTY);
				if (impact.delta < 0) impact.delta = 0;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, impact, message, true), 
											  other.getId());
			}
		});
	}
	
	@Override
	public String getEventName() {
		String postfix = " hostilities";
		if (isDone()) {
			postfix = " hostilities - over";
		}
		//return "Hostilities: " + Misc.ucFirst(one.getDisplayName()) + " / " + Misc.ucFirst(two.getDisplayName()) + postfix;
		return Misc.ucFirst(one.getDisplayName()) + " / " + Misc.ucFirst(two.getDisplayName()) + postfix;
	}
	
	

	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, 
							List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		if (action.equals("TODO")) {
		} 
		return true;
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();
		

		addFactionNameTokens(map, "one", one);
		addFactionNameTokens(map, "two", two);
		
		FactionAPI commFac = Misc.getCommissionFaction();
		if (commFac != null && (commFac == one || commFac == two)) {
			map.put("$sender", commFac.getDisplayName());
			addFactionNameTokens(map, "commission", commFac);
			addFactionNameTokens(map, "other", commFac == one ? two : one);
		}
		
		//map.put("$sender", "Unknown");
		return map;
	}
	
	public String[] getHighlights(String stageId) {
		List<String> result = new ArrayList<String>();
		//addTokensToList(result, "$duration");
		return result.toArray(new String[0]);
	}

	public boolean isDone() {
		return ended;
	}

}







