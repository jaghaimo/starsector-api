package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ShowPersonVisual <optional - minimal mode> <optional person id>
 */
public class ShowPersonVisual extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		SectorEntityToken target = dialog.getInteractionTarget();

		boolean minimal = false;
		if (params.size() > 0) {
			minimal = params.get(0).getBoolean(memoryMap);
		}
		if (params.size() > 1) {
			String id = params.get(1).getString(memoryMap);
			PersonAPI person = Global.getSector().getImportantPeople().getData(id).getPerson();
			dialog.getVisualPanel().showPersonInfo(person, minimal);
		} else if (target.getActivePerson() != null) {
			dialog.getVisualPanel().showPersonInfo(target.getActivePerson(), minimal);
		} else {
			if (target instanceof CampaignFleetAPI) {
				CampaignFleetAPI fleet = (CampaignFleetAPI) target;
				if (fleet.getCommander() != null) {
					dialog.getVisualPanel().showPersonInfo(fleet.getCommander(), minimal);
				}
			}
		}
		
		//dialog.getVisualPanel().showSecondPerson(Global.getSector().getPlayerFaction().createRandomPerson());
	
		return true;
	}

}
