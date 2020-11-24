package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class SetTooltip extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String option = params.get(0).getString(memoryMap);
		String tooltip = params.get(1).getStringWithTokenReplacement(ruleId, dialog, memoryMap);

		if (tooltip == null || tooltip.isEmpty()) return true;
		
		dialog.getOptionPanel().setTooltip(option, tooltip);
		return true;
	}

}
