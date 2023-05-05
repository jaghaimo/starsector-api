package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	RemoveShipWithId <id>
 */
public class RemoveShipWithId extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String id = params.get(0).getString(memoryMap);

		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
			if (member.getId().equals(id)) {
				pf.getFleetData().removeFleetMember(member);
				RemoveShip.addShipLossText(member, dialog.getTextPanel());
				return true;
			}
		}
		return false;
	}
}
