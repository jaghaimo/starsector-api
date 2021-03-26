package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;


/**
 * SetNearbyFleetsVariable <range> <faction id> <variable name> <value> <duration>
 */
public class SetNearbyFleetsVariable extends BaseCommandPlugin {
	
	//BroadcastPlayerAction <type> <range> <responseVariable>
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
	
		
		float range = Float.parseFloat(params.get(0).string);
		String factionId = params.get(1).getString(memoryMap);
		String varName = params.get(2).string;
		String value = params.get(3).getString(memoryMap);
		float dur = Float.parseFloat(params.get(4).string);
		
		final SectorEntityToken target = dialog.getInteractionTarget();
		if (target.getContainingLocation() == null) return false;
		
		List<CampaignFleetAPI> fleets = target.getContainingLocation().getFleets();
		for (CampaignFleetAPI fleet : fleets) {
			if (fleet == target) continue;
			if (!fleet.getFaction().getId().equals(factionId)) continue;
			if (fleet.getAI() instanceof CampaignFleetAIAPI) {
				float dist = Misc.getDistance(target.getLocation(), fleet.getLocation());
				if (dist <= range) {
					fleet.getMemoryWithoutUpdate().set(varName, value, dur);
				}
			}
		}
		
		return true;
	}
	

}






