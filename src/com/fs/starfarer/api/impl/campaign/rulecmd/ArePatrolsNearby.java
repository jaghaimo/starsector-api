package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ArePatrolsNearby <factionId> <range>
 */
public class ArePatrolsNearby extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String factionId = params.get(0).getString(memoryMap);
		float range = params.get(1).getFloat(memoryMap);

		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
			if (!fleet.getFaction().getId().equals(factionId)) continue;
			
			float dist = Misc.getDistance(playerFleet.getLocation(), fleet.getLocation());
			if (dist > range) continue;
			
			if (isPatrol(fleet)) return true;
		}

		return false;
	}
	
	private boolean isPatrol(CampaignFleetAPI fleet) {
		if (!fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_PATROL_FLEET)) {
			return false;
		}
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (!member.isCivilian()) return true;
		}
		
		return false;
	}
}




