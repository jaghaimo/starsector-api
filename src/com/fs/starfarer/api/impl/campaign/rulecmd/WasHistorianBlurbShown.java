package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetHistorianBlurbShownAfterDelay <blurb id> <delay in days>
 */
public class WasHistorianBlurbShown extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String id = params.get(0).getString(memoryMap);
		return SharedData.getData().getUniqueEncounterData().historianBlurbsShown.contains(id);
	}

}
