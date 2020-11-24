package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class ShowPersonVisual extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		SectorEntityToken target = dialog.getInteractionTarget();
		
		if (target.getActivePerson() != null) {
			dialog.getVisualPanel().showPersonInfo(target.getActivePerson());
		} else {
			if (target instanceof CampaignFleetAPI) {
				CampaignFleetAPI fleet = (CampaignFleetAPI) target;
				if (fleet.getCommander() != null) {
					dialog.getVisualPanel().showPersonInfo(fleet.getCommander());
				}
			}
		}
	
		return true;
	}

}
