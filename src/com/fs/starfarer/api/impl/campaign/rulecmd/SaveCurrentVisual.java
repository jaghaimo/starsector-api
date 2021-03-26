package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Saves current visual so it can be restored using RestoreCurrentVisual. Useful e.g. when starting a conversation
 * with a person and needing to go back to whatever was going on before without knowing what it might have been.
 * 
 * SaveCurrentVisual
 */
public class SaveCurrentVisual extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		dialog.getVisualPanel().saveCurrentVisual();
		return true;
	}

}










