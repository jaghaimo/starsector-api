package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * AddBarEvent <option id> <option text> <blurb>
 */
public class AddBarEvent extends BaseCommandPlugin {

	public static class BarEventData {
		public String optionId;
		public String option;
		public String blurb;
		public Color optionColor;
		public BarEventData(String optionId, String option, String blurb) {
			this.optionId = optionId;
			this.option = option;
			this.blurb = blurb;
		}
	}
	
	public static class TempBarEvents {
		public Map<String, BarEventData> events = new LinkedHashMap<String, BarEventData>();
	}

	public static String KEY = "$core_tempBarEvents";
	public static TempBarEvents getTempEvents(MarketAPI market) {
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		TempBarEvents events = (TempBarEvents)mem.get(KEY);
		if (events == null) {
			events = new TempBarEvents();
			mem.set(KEY, events, 0f);
		}
		return events;
	}

	public static void removeTempEvent(MarketAPI market, String optionId) {
		getTempEvents(market).events.remove(optionId);
	}
	
	public static void clearTempEvents(MarketAPI market) {
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		mem.unset(KEY);
	}
	
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (market == null) return true;
		
		String optionId = params.get(0).getString(memoryMap);
		String option = params.get(1).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
		String blurb = params.get(2).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
		
		Color color = null;
		if (params.size() >= 4) {
			color = params.get(3).getColor(memoryMap);
		}
		
		TempBarEvents events = getTempEvents(market);
		
		BarEventData data = new BarEventData(optionId, option, blurb);
		data.optionColor = color;
		events.events.put(optionId, data);
		return true;
	}
	
}









