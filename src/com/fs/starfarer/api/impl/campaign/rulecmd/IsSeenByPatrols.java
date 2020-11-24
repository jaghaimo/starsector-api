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
 * IsSeenByPatrols <factionId> <patrolMustCareAboutTransponder>
 */
public class IsSeenByPatrols extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		//if (true) return false;
		
		String factionId = params.get(0).getString(memoryMap);
		
		boolean patrolMustCareAboutTransponder = false;
		if (params.size() > 1) {
			patrolMustCareAboutTransponder = params.get(1).getBoolean(memoryMap);
		}
		//float range = Global.getSettings().getFloat("sensorRangeMax");

		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
			if (!fleet.getFaction().getId().equals(factionId)) continue;
			if (fleet.getFaction().isPlayerFaction()) continue;
			if (fleet.getBattle() != null) continue;
			
			if (patrolMustCareAboutTransponder && !Misc.caresAboutPlayerTransponder(fleet)) continue;
//			float dist = Misc.getDistance(playerFleet.getLocation(), fleet.getLocation()) - playerFleet.getRadius() - fleet.getRadius();
//			if (dist > range) continue;
			
			VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			if (!mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF) && 
					!mem.contains(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
				if (level == VisibilityLevel.NONE) continue;
			}
			
			float dist = Misc.getDistance(fleet.getLocation(), playerFleet.getLocation());
			if (dist > 1000f) continue;
			
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




