package com.fs.starfarer.api.impl.campaign.rulecmd.academy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * CESetHidden <person id> <true|false>
 */
public class CESetHidden extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (market == null) return false;
		if (market.getCommDirectory() == null) return false;

		String id = params.get(0).getString(memoryMap);
		boolean hidden = params.get(1).getBoolean(memoryMap);
		
		
		CommDirectoryEntryAPI entry = market.getCommDirectory().getEntryForPerson(id);
		if (entry != null) {
			entry.setHidden(hidden);
		}
		return true;
	}
}










