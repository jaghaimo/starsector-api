package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

/**
 *	RemoveShip <fleet member reference>
 */
public class AddShipToOtherFleet extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
//		ShipVariantAPI variant = (ShipVariantAPI) var.memory.get(var.name);
//		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
		
		FleetMemberAPI member = (FleetMemberAPI) var.memory.get(var.name);
		if (member != null) {
			if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
				CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
				fleet.getFleetData().addFleetMember(member);
			}
		}
		return true;
	}

}
