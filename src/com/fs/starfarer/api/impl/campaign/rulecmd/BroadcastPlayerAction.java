package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;


public class BroadcastPlayerAction extends BaseCommandPlugin {
	
	//BroadcastPlayerAction <type> <range> <responseVariable>
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		ActionType type = Enum.valueOf(ActionType.class, params.get(0).string);
		float range = Float.parseFloat(params.get(1).string);
		String responseVariable = params.get(2).string;
		
		final SectorEntityToken target = dialog.getInteractionTarget();
		if (target.getContainingLocation() == null) return false;
		
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		broadcast(type, range, responseVariable, playerFleet, target);
		
		return true;
	}
	
	public static void broadcast(ActionType type, float range, String responseVariable, 
			SectorEntityToken actor, SectorEntityToken target) {
		broadcast(type, range, responseVariable, actor, target, null);
	}
	public static void broadcast(ActionType type, float range, String responseVariable, 
									SectorEntityToken actor, SectorEntityToken target, SectorEntityToken exclude) {
		List<CampaignFleetAPI> fleets = target.getContainingLocation().getFleets();
		for (CampaignFleetAPI fleet : fleets) {
			if (fleet == exclude) continue;
			if (fleet.getAI() instanceof CampaignFleetAIAPI) {
				float dist = Misc.getDistance(target.getLocation(), fleet.getLocation());
				if (dist <= range) {
					CampaignFleetAIAPI ai = (CampaignFleetAIAPI) fleet.getAI();
					ai.reportNearbyAction(type, actor, target, responseVariable);
				}
			}
		}
	}

}






