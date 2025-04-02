package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.TempImmigrationModifier;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Per month can be negative.
 * AddPopGrowth <per month growth> <duration days> <description> <optional marketId>
 */
public class AddPopGrowth extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		float growth = params.get(0).getFloat(memoryMap);
		float dur = params.get(1).getFloat(memoryMap);
		String desc = params.get(2).getString(memoryMap);
		
		String marketId = null;
		if (params.size() > 3) marketId = params.get(3).getString(memoryMap);
		
		MarketAPI market = null;
		if (marketId != null) {
			market = Global.getSector().getEconomy().getMarket(marketId);
		} else if (dialog.getInteractionTarget() != null) {
			market = dialog.getInteractionTarget().getMarket();
		}
		
		if (market == null) return false;
		
		new TempImmigrationModifier(market, growth, dur, desc);
	
		return true;
	}
}
