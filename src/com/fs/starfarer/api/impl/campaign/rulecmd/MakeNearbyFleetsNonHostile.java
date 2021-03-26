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
 * MakeNearbyFleetsNonHostile <faction id> <reason> <range> <days>
 */
public class MakeNearbyFleetsNonHostile extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (dialog.getInteractionTarget() == null) return false;

		final String factionId = params.get(0).getString(memoryMap);
		String reason = params.get(1).getString(memoryMap);
		float range = params.get(2).getFloat(memoryMap);
		float days = params.get(3).getFloat(memoryMap);
		
		List<CampaignFleetAPI> fleets = Misc.findNearbyFleets(dialog.getInteractionTarget(), range, new FleetFilter() {
			public boolean accept(CampaignFleetAPI curr) {
				return curr.getFaction().getId().equals(factionId);
			}
		});
		
		for (CampaignFleetAPI curr : fleets) {
			MemoryAPI memory = curr.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(memory, MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, reason, true, days);
		}
	
		
		return true;
	}

}
