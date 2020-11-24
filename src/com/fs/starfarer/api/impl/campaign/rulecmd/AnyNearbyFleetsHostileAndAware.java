package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * AnyNearbyFleetsHostileAndAware <factionId>
 */
public class AnyNearbyFleetsHostileAndAware extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String factionId = params.get(0).getString(memoryMap);
		//float range = Global.getSettings().getFloat("sensorRangeMax");

		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
			if (!fleet.getFaction().getId().equals(factionId)) continue;
			if (fleet.getAI() == null) continue;
			if (fleet.isStationMode()) continue;
			
//			VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
//			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
//			if (!mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF)) {
//				if (level == VisibilityLevel.NONE) continue;
//			}
			if (!fleet.getAI().isHostileTo(playerFleet)) continue;
			
			FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
			if (curr != null && curr.getTarget() == playerFleet) {
				return true;
			}
		}

		return false;
	}
	
}




