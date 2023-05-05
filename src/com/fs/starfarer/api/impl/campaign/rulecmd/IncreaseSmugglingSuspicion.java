package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 * IncreaseSmugglingSuspicion <amount>
 * An amount of 1 is maxed-out suspicion.
 * 
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public class IncreaseSmugglingSuspicion extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		float amount = params.get(0).getFloat(memoryMap);

		if (dialog.getInteractionTarget() == null) return false;
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (market == null) return false;
		
		float curr = market.getMemoryWithoutUpdate().getFloat(MemFlags.MARKET_EXTRA_SUSPICION);
		curr += amount;
		market.getMemoryWithoutUpdate().set(MemFlags.MARKET_EXTRA_SUSPICION, curr);
		
		return true;
	}

}
