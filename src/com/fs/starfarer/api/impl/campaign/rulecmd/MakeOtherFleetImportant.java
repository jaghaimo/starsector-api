package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * @author Alex Mosolov
 *
 *	MakeOtherFleetImportant <reason> <true or false?> <expire?>
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class MakeOtherFleetImportant extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		String reason = params.get(0).getString(memoryMap);
		boolean value = params.get(1).getBoolean(memoryMap);;
		float expire = -1f;
		if (params.size() >= 3) {
			expire = params.get(2).getFloat(memoryMap);
		}
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		if (value) {
			Misc.makeImportant(fleet, reason, expire);
		} else {
			Misc.makeUnimportant(fleet, reason);
		}
		return true;
	}

}





