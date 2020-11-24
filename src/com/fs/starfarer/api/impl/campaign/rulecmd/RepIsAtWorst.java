package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * RepIsAtBest <factionId> RepLevel
 */
public class RepIsAtWorst extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String factionId = params.get(0).getString(memoryMap);
		String repLevelStr = params.get(1).getString(memoryMap);
		RepLevel repLevel = RepLevel.valueOf(repLevelStr);
		
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		return player.isAtWorst(factionId, repLevel);
	}

}
