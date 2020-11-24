package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * OpenCoreTab <CoreUITabId> <CoreUITradeMode (optional)>
 */
public class OpenCommDirectory extends BaseCommandPlugin {

	/** 
	 * OpenCoreUI <CoreUITabId>
	 */
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog.getInteractionTarget().getMarket() == null) return false;
		
		dialog.showCommDirectoryDialog(dialog.getInteractionTarget().getMarket().getCommDirectory());
		
		return true;
	}

}
