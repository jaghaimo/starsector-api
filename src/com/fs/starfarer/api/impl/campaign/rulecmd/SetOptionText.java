package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetOptionText optionId newText
 */
public class SetOptionText extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String option = params.get(0).string;
		String newText = params.get(1).string;
		
		dialog.getOptionPanel().setOptionText(newText, option);
		return true;
	}

}
