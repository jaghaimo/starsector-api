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
 * MakeNearbyFleetsHostile <faction id> <range> <days>
 */
public class MakeNearbyFleetsHostile extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;

		final String factionId = params.get(0).getString(memoryMap);
		float range = params.get(1).getFloat(memoryMap);
		float days = params.get(2).getFloat(memoryMap);
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		List<CampaignFleetAPI> fleets = Misc.findNearbyFleets(fleet, range, new FleetFilter() {
			public boolean accept(CampaignFleetAPI curr) {
				return curr.getFaction().getId().equals(factionId);
			}
		});
		
		if (fleet.getFaction().getId().equals(factionId)) {
			MemoryAPI memory = fleet.getMemoryWithoutUpdate();
			memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true, days);
		}
		
		for (CampaignFleetAPI curr : fleets) {
			MemoryAPI memory = curr.getMemoryWithoutUpdate();
			memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true, days);
		}
	
		
		return true;
	}

}
