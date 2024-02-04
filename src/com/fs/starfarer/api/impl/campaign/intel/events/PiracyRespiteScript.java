package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.PiracyRespite;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

public class PiracyRespiteScript implements EconomyUpdateListener {

	public static String KEY = "$prs_ref";
	
	//public static float DURATION = 730;
	public static float DURATION = -1f;
	
	public static PiracyRespiteScript get() {
		//if (true) return null;
		return (PiracyRespiteScript) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	

	protected long timestamp;
	
	public PiracyRespiteScript() {
		
		sendGainedMessage();
		
		// to avoid duplicates
		PiracyRespiteScript existing = get();
		if (existing != null) {
			existing.resetTimestamp();
			return;
		}
		
		resetTimestamp();
		Global.getSector().getEconomy().addUpdateListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		economyUpdated();
	}
	
	public void sendGainedMessage() {
		MessageIntel msg = new MessageIntel();
		msg.addLine("Piracy Respite gained", Misc.getBasePlayerColor());
		msg.addLine(BaseIntelPlugin.BULLET + "Colonies receive %s accessibility", Misc.getTextColor(),
				new String [] {"+" + (int)Math.round(PiracyRespite.ACCESSIBILITY_BONUS * 100f) + "%"},
				Misc.getHighlightColor());
		if (DURATION > 0) {
			msg.addLine(BaseIntelPlugin.BULLET + "Lasts for %s days", Misc.getTextColor(),
					new String [] {"" + (int)PiracyRespiteScript.DURATION},
				Misc.getHighlightColor());
		}
		msg.setIcon(Global.getSettings().getSpriteName("events", "piracy_respite"));
		msg.setSound(Sounds.REP_GAIN);
		Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
	}
	
	public void sendExpiredMessage() {
		MessageIntel msg = new MessageIntel();
		msg.addLine("Piracy Respite expired", Misc.getBasePlayerColor());
		msg.setIcon(Global.getSettings().getSpriteName("events", "piracy_respite"));
		msg.setSound(Sounds.REP_LOSS);
		Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
	}

	public void resetTimestamp() {
		timestamp = Global.getSector().getClock().getTimestamp();		
	}
	
	public float getDaysRemaining() {
		if (DURATION < 0) return DURATION;
		float rem = DURATION - Global.getSector().getClock().getElapsedDaysSince(timestamp);
		//rem = 1f - Global.getSector().getClock().getElapsedDaysSince(timestamp);
		if (rem < 0) rem = 0;
		return rem;
	}

	public void commodityUpdated(String commodityId) {
		
	}

	public void economyUpdated() {
		for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
			if (!curr.hasCondition(Conditions.PIRACY_RESPITE)) {
				curr.addCondition(Conditions.PIRACY_RESPITE);
			}
		}
	}

	public void cleanup() {
		if (Global.getSector().getMemoryWithoutUpdate().contains(KEY)) {
			sendExpiredMessage();
		}
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
		for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
			if (curr.hasCondition(Conditions.PIRACY_RESPITE)) {
				curr.removeCondition(Conditions.PIRACY_RESPITE);
			}
		}
	}
	
	public boolean isEconomyListenerExpired() {
		if (DURATION < 0) return false;
		
		float days = getDaysRemaining();
		if (days <= 0) {
			cleanup();
			return true;
		}
		return false;
	}

}



