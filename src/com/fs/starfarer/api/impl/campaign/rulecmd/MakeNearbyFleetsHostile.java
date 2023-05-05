package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetFilter;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Issues:
 * 	The dialog greeting text is still the same
 *	They still try to engage in toll collection
 *	NOT CURRENTLY USED, SHOULDN'T BE AS IT DOESN'T WORK
 *
 * MakeNearbyFleetsHostile <faction id> <range> <days> <optional: also know player identity regardless of transponder, defaults to false>
 */
public class MakeNearbyFleetsHostile extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;

		final String factionId = params.get(0).getString(memoryMap);
		float range = params.get(1).getFloat(memoryMap);
		float days = params.get(2).getFloat(memoryMap);
		
		boolean makeAware = false;
		if (params.size() >= 4) {
			makeAware = params.get(3).getBoolean(memoryMap);
		}
		
		
		List<CampaignFleetAPI> fleets = Misc.findNearbyFleets(dialog.getInteractionTarget(), range, new FleetFilter() {
			public boolean accept(CampaignFleetAPI curr) {
				return curr.getFaction().getId().equals(factionId);
			}
		});

		if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			if (fleet.getFaction().getId().equals(factionId)) {
				MemoryAPI memory = fleet.getMemoryWithoutUpdate();
				memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true, days);
				if (makeAware) {
					memory.set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
				}
			}
		}
		
		for (CampaignFleetAPI curr : fleets) {
			if (curr == dialog.getInteractionTarget()) continue;
			
			MemoryAPI memory = curr.getMemoryWithoutUpdate();
			
			//boolean stillSet = Misc.setFlagWithReason(memory, MemFlags.MEMORY_KEY_MAKE_HOSTILE, reason, true, days);
			
			memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true, days);
			if (makeAware) {
				memory.set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
			}
		}
	
		
		return true;
	}

}
