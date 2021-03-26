package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Gives the player the specified number of story points.
 * AddStoryPoints <num>
 */
public class AddStoryPoints extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		int num = params.get(0).getInt(memoryMap);
		Global.getSector().getPlayerStats().addStoryPoints(num, dialog.getTextPanel(), false);
		
		return true;
	}
	
	
}



