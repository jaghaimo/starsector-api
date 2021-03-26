package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	HighlightComms
 *
 * Changes the "open comm link" option to a yellow color. Undecided whether it's good to use this; creates the 
 * expectation that comms are pointless unless highlighted.
 * 
 * So, probably: only highlight comes (via other means) when the player is being hailed. See: HailPlayer. 
 *
 * Copyright 2020 Fractal Softworks, LLC
 */
public class HighlightComms extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();

		fleet.getMemoryWithoutUpdate().set("$highlightComms", true, 0);
		
		return true;
	}

}





