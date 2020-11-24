package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class RepairNeeded extends BaseCommandPlugin {

	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		TextPanelAPI textPanel = dialog.getTextPanel();
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		OptionPanelAPI options = dialog.getOptionPanel();
		
		//if (entity.getFaction().getRelationship(playerFleet.getFaction().getId()) >= 0) {
			float needed = playerFleet.getLogistics().getTotalRepairAndRecoverySupplyCost();
			if (needed > 0) needed = Math.max(1, Math.round(needed));

			if (needed <= 0) {
				return false;
			}
		//}
		return true;
	}
}
