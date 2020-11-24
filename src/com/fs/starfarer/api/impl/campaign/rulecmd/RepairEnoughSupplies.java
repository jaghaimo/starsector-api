package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class RepairEnoughSupplies extends BaseCommandPlugin {

	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		//SectorEntityToken entity = dialog.getInteractionTarget();
		//if (entity.getFaction().getRelationship(playerFleet.getFaction().getId()) >= 0) {
			float needed = playerFleet.getLogistics().getTotalRepairAndRecoverySupplyCost();
			if (needed > 0) needed = Math.max(1, Math.round(needed));
			float supplies = playerFleet.getCargo().getSupplies();

			if (supplies < needed) {
				return false;
			}
		//}
		
		return true;
	}
}
