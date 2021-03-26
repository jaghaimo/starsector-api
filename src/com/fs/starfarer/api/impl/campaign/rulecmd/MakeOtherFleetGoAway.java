package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	MakeOtherFleetGoAway <withClear>
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class MakeOtherFleetGoAway extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		boolean clear = false;
		if (params.size() >= 1) {
			clear = params.get(0).getBoolean(memoryMap);
		}
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		if (clear && fleet.getAI() != null) {
			fleet.getAI().setActionTextOverride(null);
		}
		
		if (fleet.getAI() instanceof ModularFleetAIAPI) {
			ModularFleetAIAPI mAI = (ModularFleetAIAPI) fleet.getAI();
			mAI.getTacticalModule().forceTargetReEval();
		}
		
		Misc.giveStandardReturnToSourceAssignments(fleet, clear);
		
		
		return true;
	}

}





