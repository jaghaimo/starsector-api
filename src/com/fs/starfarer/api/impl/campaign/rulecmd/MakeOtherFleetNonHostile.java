package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * @author Alex Mosolov
 *
 *	MakeOtherFleetHostile <reason> <true or false> <expire>
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class MakeOtherFleetNonHostile extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		String reason = "generic";
		boolean value;
		float expire = 0f;
		if (params.size() >= 2) {
			reason = params.get(0).getString(memoryMap);
			value = params.get(1).getBoolean(memoryMap);
			if (params.size() >= 3) {
				expire = params.get(2).getFloat(memoryMap);
			}
		} else {
			value = params.get(0).getBoolean(memoryMap);
		}
		
		boolean wasHostile = false;
		if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			wasHostile = fleet.isHostileTo(Global.getSector().getPlayerFleet());
		}
		
		
		MemoryAPI memory = dialog.getInteractionTarget().getMemoryWithoutUpdate();
		
		boolean stillSet = Misc.setFlagWithReason(memory, MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, reason, value, expire);
		
		if (stillSet) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			if (fleet.getAI() instanceof ModularFleetAIAPI) {
				ModularFleetAIAPI mAI = (ModularFleetAIAPI) fleet.getAI();
				mAI.getTacticalModule().forceTargetReEval();
			}
		}
		
		boolean isHostile = false;
		if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			isHostile = fleet.isHostileTo(Global.getSector().getPlayerFleet());
		}
		
		if (isHostile != wasHostile) {
			Global.getSoundPlayer().restartCurrentMusic();
		}
		
		return true;
	}

}





