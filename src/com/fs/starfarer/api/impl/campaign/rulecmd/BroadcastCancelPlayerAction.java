package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;


public class BroadcastCancelPlayerAction extends BaseCommandPlugin {
	
	//BroadcastCancelPlayerAction <range> <responseVariable>
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		ActionType type = ActionType.CANCEL;
		float range = Float.parseFloat(params.get(0).string);
		String responseVariable = params.get(1).string;
		
		final SectorEntityToken target = dialog.getInteractionTarget();
		if (target.getContainingLocation() == null) return false;
		
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		BroadcastPlayerAction.broadcast(type, range, responseVariable, playerFleet, target, target);
		
		return true;
	}
}






