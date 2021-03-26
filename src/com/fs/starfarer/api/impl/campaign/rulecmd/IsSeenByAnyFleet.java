package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * IsSeenByAnyFleet <factionId>
 */
public class IsSeenByAnyFleet extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		//if (true) return false;
		
		String factionId = null;
		if (params.size() >= 1) {
			factionId = params.get(0).getString(memoryMap);
		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
			if (factionId != null && !fleet.getFaction().getId().equals(factionId)) continue;
			if (fleet.getFaction().isPlayerFaction()) continue;
			if (fleet.getBattle() != null) continue;
			if (fleet.isStationMode()) continue;
			
			VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			if (!mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF) && 
					!mem.contains(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
				if (level == VisibilityLevel.NONE) continue;
			}
			
			float dist = Misc.getDistance(fleet.getLocation(), playerFleet.getLocation());
			if (dist > 1000f) continue;
			
			return true;
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




