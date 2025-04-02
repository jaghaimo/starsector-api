package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetCodexEntryId <entry id>
 * Sets the id of the entry to show when the Codex is opened, with an expiration of 0.
 * If no entry id is specified, it is unset instead.
 */
public class SetCodexEntryId extends BaseCommandPlugin {

	public static String GLOBAL_CODEX_ENTRY_TO_OPEN = "$codexEntryId";
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		if (params.size() <= 0) {
			Global.getSector().getMemoryWithoutUpdate().unset(GLOBAL_CODEX_ENTRY_TO_OPEN);
		} else {
			Global.getSector().getMemoryWithoutUpdate().set(
					GLOBAL_CODEX_ENTRY_TO_OPEN, params.get(0).getString(memoryMap), 0f);
		}
		
		
		return true;
	}

}
