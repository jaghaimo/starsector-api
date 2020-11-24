package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc.Token;

public class RepairAvailable extends BaseCommandPlugin {

	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		float needed = playerFleet.getLogistics().getTotalRepairAndRecoverySupplyCost();
		if (needed > 0) needed = Math.max(1, Math.round(needed));
		memoryMap.get(MemKeys.GLOBAL).set("$repairSupplyCost", (int) needed, 0);
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (entity.getMarket() != null && !entity.getMarket().hasSpaceport()) {
			return false;
		}
			
		RepLevel level = entity.getFaction().getRelationshipLevel(Factions.PLAYER);
		if (level.isAtWorst(RepLevel.SUSPICIOUS)) {
			return true;
		}
		
		
		return false;
	}
}
