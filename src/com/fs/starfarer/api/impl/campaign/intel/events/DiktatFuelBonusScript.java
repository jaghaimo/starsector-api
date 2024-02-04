package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

public class DiktatFuelBonusScript {

	public static float FUEL_EXPORT_BONUS = Global.getSettings().getFloat("diktatDefeatedExportBonus");
	public static String MOD_ID = "dfb_export_mod";
	
	
	public static void grantBonus() {
		sendGainedMessage();
		
		Global.getSector().getPlayerStats().getDynamic().getStat(
				Stats.getCommodityExportCreditsMultId(Commodities.FUEL)).modifyMult(MOD_ID, 1f + FUEL_EXPORT_BONUS,
						"Proven stable source (due to outcome of Diktat conflict)");
	}
	
	public void removeBonus() {
		sendLostMessage();
		
		Global.getSector().getPlayerStats().getDynamic().getStat(
				Stats.getCommodityExportCreditsMultId(Commodities.FUEL)).unmodifyFlat(MOD_ID);
	}
	
	public static void sendGainedMessage() {
		MessageIntel msg = new MessageIntel();
		msg.addLine("Fuel exports increased", Misc.getBasePlayerColor());
		msg.addLine(BaseIntelPlugin.BULLET + "%s income from fuel exports", Misc.getTextColor(),
				new String [] {"+" + (int)Math.round(FUEL_EXPORT_BONUS * 100f) + "%"},
				Misc.getHighlightColor());

		msg.setIcon(Global.getSettings().getCommoditySpec(Commodities.FUEL).getIconName());
		msg.setSound(Sounds.REP_GAIN);
		Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
	}
	
	public static void sendLostMessage() {
		MessageIntel msg = new MessageIntel();
		msg.addLine("Fuel export bonus lost", Misc.getBasePlayerColor());
		msg.setIcon(Global.getSettings().getCommoditySpec(Commodities.FUEL).getIconName());
		msg.setSound(Sounds.REP_LOSS);
		Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
	}

	
}



