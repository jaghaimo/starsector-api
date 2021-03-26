package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

/**
 *	AddShip <fleet member reference>
 */
public class AddShip extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
//		ShipVariantAPI variant = (ShipVariantAPI) var.memory.get(var.name);
//		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
		
		FleetMemberAPI member = (FleetMemberAPI) var.memory.get(var.name);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		addShipGainText(member, dialog.getTextPanel());
		return true;
	}

	public static void addShipGainText(FleetMemberAPI member, TextPanelAPI text) {
		text.setFontSmallInsignia();
		text.addParagraph("Gained " + member.getVariant().getFullDesignationWithHullNameForShip(), Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), member.getVariant().getFullDesignationWithHullNameForShip());
		text.setFontInsignia();
	}
}
