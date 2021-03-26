package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * HostileFleetNearbyAndAware
 */
public class HostileFleetNearbyAndAware extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		//float range = params.get(0).getFloat(memoryMap);

		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
			if (fleet.getAI() == null) continue; // dormant Remnant fleets
			if (fleet.getFaction().isPlayerFaction()) continue;
			if (fleet.isStationMode()) continue;
			
			if (!fleet.isHostileTo(playerFleet)) continue;
			if (fleet.getBattle() != null) continue;
			
			if (Misc.isInsignificant(fleet)) {
				continue;
			}
			
			
			VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
//			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
//			if (!mem.contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF) && 
//					!mem.contains(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
//				if (level == VisibilityLevel.NONE) continue;
//			}
			if (level == VisibilityLevel.NONE) continue;
			
			if (fleet.getFleetData().getMembersListCopy().isEmpty()) continue;
			
			float dist = Misc.getDistance(playerFleet.getLocation(), fleet.getLocation());
			if (dist > 1500f) continue;
			
			//fleet.getAI().pickEncounterOption(null, playerFleet, true);
			if (fleet.getAI() instanceof ModularFleetAIAPI) {
				ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
				if (ai.getTacticalModule() != null && 
						(ai.getTacticalModule().isFleeing() || ai.getTacticalModule().isMaintainingContact() ||
								ai.getTacticalModule().isStandingDown())) {
					continue;
				}
			}
			
			return true;
		}

		return false;
	}
	
}




