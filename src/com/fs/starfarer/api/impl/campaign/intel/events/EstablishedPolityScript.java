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
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.util.Misc;

public class EstablishedPolityScript implements EconomyUpdateListener {

	public static String KEY = "$eps_ref";
	
	public static EstablishedPolityScript get() {
		return (EstablishedPolityScript) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	

	protected long timestamp;
	
	public EstablishedPolityScript() {
		
		sendGainedMessage();
		
		// to avoid duplicates
		EstablishedPolityScript existing = get();
		if (existing != null) {
			return;
		}
		
		Global.getSector().getEconomy().addUpdateListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		economyUpdated();
	}
	
	public void sendGainedMessage() {
		MessageIntel msg = new MessageIntel();
		msg.addLine("Established Polity gained", Misc.getBasePlayerColor());
		msg.addLine(BaseIntelPlugin.BULLET + "Colonies receive %s accessibility", Misc.getTextColor(),
				new String [] {"+" + (int)Math.round(PiracyRespite.ACCESSIBILITY_BONUS * 100f) + "%"},
				Misc.getHighlightColor());
		msg.setIcon(Global.getSettings().getSpriteName("events", "established_polity"));
		msg.setSound(Sounds.REP_GAIN);
		Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
	}
	
	public void commodityUpdated(String commodityId) {
		
	}

	public void economyUpdated() {
		//for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			if (curr.isPlayerOwned() && curr.getFaction() != null && curr.getFaction().isPlayerFaction()) {
				if (!curr.hasCondition(Conditions.ESTABLISHED_POLITY)) {
					curr.addCondition(Conditions.ESTABLISHED_POLITY);
				}
			} else {
				if (curr.hasCondition(Conditions.ESTABLISHED_POLITY)) {
					curr.removeCondition(Conditions.ESTABLISHED_POLITY);
				}
			}
		}
	}

	public void cleanup() {
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
		//for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			if (curr.hasCondition(Conditions.ESTABLISHED_POLITY)) {
				curr.removeCondition(Conditions.ESTABLISHED_POLITY);
			}
		}
	}
	
	public boolean isEconomyListenerExpired() {
		if (!PerseanLeagueMembership.isDefeatedBlockadeOrPunEx() && !PerseanLeagueMembership.isLeagueMember()) {
			cleanup();
			return true;
		}
		return false;
	}

}



