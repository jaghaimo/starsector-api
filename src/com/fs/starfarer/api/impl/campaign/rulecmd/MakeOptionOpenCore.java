package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * OpenCoreTab <MakeOptionOpenCore> <optionId> <defaultTab> <tradeMode> <optional: onlyShowTargetTabShortcut>
 */
public class MakeOptionOpenCore extends BaseCommandPlugin {

	private Map<String, MemoryAPI> memoryMap;
	private InteractionDialogAPI dialog;
	
	/** 
	 * OpenCoreTab <MakeOptionOpenCore> <optionId> <defaultTab> <tradeMode>
	 */
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		String optionId = params.get(0).getString(memoryMap);
		
		String tabIdStr = params.get(1).getString(memoryMap);
		
		CoreUITabId tabId = CoreUITabId.valueOf(tabIdStr);
		
		CoreUITradeMode mode = CoreUITradeMode.OPEN;
		if (params.size() > 1) {
			mode = CoreUITradeMode.valueOf(params.get(2).getString(memoryMap));
		}
		
		boolean onlyShowTargetTabShortcut = false;
		if (params.size() > 3) {
			onlyShowTargetTabShortcut = params.get(3).getBoolean(memoryMap);
		}
		
		dialog.makeOptionOpenCore(optionId, tabId, mode, onlyShowTargetTabShortcut);
		
		return true;
	}

}
