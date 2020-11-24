package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class SetFlagship extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>();
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			if (member.isFighterWing()) continue;
			members.add(member);
		}
		if (!members.isEmpty()) {
			dialog.showFleetMemberPickerDialog("Select new flagship", "Ok", "Cancel", 
					3, 7, 58f, false, false, members,
			new FleetMemberPickerListener() {
				public void pickedFleetMembers(List<FleetMemberAPI> members) {
					if (members != null && !members.isEmpty()) {
						FleetMemberAPI selectedFlagship = members.get(0);
						playerFleet.getFleetData().setFlagship(selectedFlagship);
						//addText(getString("selectedFlagship"));
					}
				}
				public void cancelledFleetMemberPicking() {
					
				}
			});
		}
		
		return true;
	}

}
