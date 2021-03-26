package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * PLAddEntry <text>
 */
public class PLAddEntry extends BaseCommandPlugin {

	public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String text = params.get(0).string;
		PlaythroughLog.getInstance().addEntry(text);
		return true;
	}

}
