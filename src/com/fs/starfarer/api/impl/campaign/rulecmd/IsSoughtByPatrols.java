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
 * IsSeenByPatrols <factionId>
 */
public class IsSoughtByPatrols extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String factionId = params.get(0).getString(memoryMap);

		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
			if (!fleet.getFaction().getId().equals(factionId)) continue;
			if (!isPatrol(fleet)) continue;
			if (fleet.getFaction().isPlayerFaction()) continue;
			if (fleet.getBattle() != null) continue;
			
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			
//			boolean caresAboutTransponder = true;
//			if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE)) {
//				caresAboutTransponder = false;
//			}
//			MarketAPI source = Misc.getSourceMarket(fleet);
//			if (source != null && source.hasCondition(Conditions.FREE_PORT)) {
//				caresAboutTransponder = false;
//			}
			boolean caresAboutTransponder = Misc.caresAboutPlayerTransponder(fleet);
			
			//VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
			
			float dist = Misc.getDistance(fleet.getLocation(), playerFleet.getLocation());
			if (dist > 1000f) continue;
			
			if ((mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF) && caresAboutTransponder) || 
					mem.contains(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
				return true;
			}
			
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




