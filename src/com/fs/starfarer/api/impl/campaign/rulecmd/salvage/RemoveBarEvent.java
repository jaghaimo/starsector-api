package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * RemoveBarEvent <option id>
 */
public class RemoveBarEvent extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (market == null) return true;
		
		String optionId = params.get(0).getString(memoryMap);
		AddBarEvent.removeTempEvent(market, optionId);
		return true;
	}
	
}









