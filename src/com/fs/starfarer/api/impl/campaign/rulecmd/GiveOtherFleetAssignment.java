package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;


/**
 * GiveOtherFleetAssignment <assignment> <duration> <actionText> <optional target id>
 */
public class GiveOtherFleetAssignment extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;

		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		FleetAssignment assignment = FleetAssignment.valueOf(params.get(0).getString(memoryMap));
		float duration = params.get(1).getFloat(memoryMap);
		String actionText = null;
		if (params.size() > 2) {
			actionText = params.get(2).getString(memoryMap);
		}
		
		SectorEntityToken target = null;
		if (params.size() > 3) {
			String id = params.get(3).getString(memoryMap);
			target = Global.getSector().getEntityById(id);
		}
		fleet.getAI().addAssignmentAtStart(assignment, target, duration, actionText, null);
		return true;
	}

}
