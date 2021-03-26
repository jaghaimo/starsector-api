package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	SetFleetAllowJump <true|false>
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class SetOtherFleetAllowJump extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		boolean canJump = params.get(0).getBoolean(memoryMap);
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		if (canJump) {
			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_NO_JUMP);
		} else {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
		}
		
		
		return true;
	}

}





