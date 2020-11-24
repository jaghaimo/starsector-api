package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

// SetEnabled <optionId> true|false
public class SetEnabled extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String option = params.get(0).getString(memoryMap);
		boolean enabled = params.get(1).getBoolean(memoryMap);

		dialog.getOptionPanel().setEnabled(option, enabled);
		return true;
	}

}
