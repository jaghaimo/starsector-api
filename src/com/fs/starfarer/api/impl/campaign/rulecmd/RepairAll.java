package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 * RepairAll <useSupplies, true by default>
 *
 * Copyright 2017 Fractal Softworks, LLC
 */
public class RepairAll extends BaseCommandPlugin {

	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
//		TextPanelAPI textPanel = dialog.getTextPanel();
		
//		textPanel.addParagraph(getString("repair"));
		
		boolean useSupplies = true;
		if (params.size() >= 1) {
			useSupplies = params.get(0).getBoolean(memoryMap);
		}
		
		float supplies = playerFleet.getCargo().getSupplies();
		float needed = playerFleet.getLogistics().getTotalRepairAndRecoverySupplyCost();
		if (needed > 0) needed = Math.max(1, Math.round(needed));
		
//		memoryMap.get("global").set("$repairSupplyCost", (int) needed);
		
//		textPanel.highlightLastInLastPara("" + (int) needed, HIGHLIGHT_COLOR);
		
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			if (member.canBeRepaired() || (member.isFighterWing() && !member.getRepairTracker().isSuspendRepairs())) {
				member.getStatus().repairFully();
				float max = member.getRepairTracker().getMaxCR();
				float curr = member.getRepairTracker().getBaseCR();
				if (max > curr) {
					member.getRepairTracker().applyCREvent(max - curr, "Repaired at dockyard");
				}
			}
		}
		
		if (needed > 0 && useSupplies) {
			playerFleet.getCargo().removeSupplies(needed);
			playerFleet.getLogistics().updateRepairUtilizationForUI();
		}
		
		return true;
	}
	
	
	private String getString(String id) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		String str = Global.getSettings().getString("stationInteractionDialog", id);

		float needed = playerFleet.getLogistics().getTotalRepairAndRecoverySupplyCost();
		float supplies = playerFleet.getCargo().getSupplies();
		str = str.replaceAll("\\$supplies", "" + (int) supplies);
		str = str.replaceAll("\\$repairSupplyCost", "" + (int) Math.ceil(needed));
		return str;
	}
}
