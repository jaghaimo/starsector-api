package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * IsSeenByPatrols <factionId>
 */
public class CaresAboutTransponder extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		
		//boolean caresAboutTransponder = Misc.caresAboutPlayerTransponder(fleet);
		return Misc.flagHasReason(mem, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "tOff");
	}

}




