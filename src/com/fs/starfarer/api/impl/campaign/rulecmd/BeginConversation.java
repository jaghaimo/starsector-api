package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Person must have been added to SectorAPI.getImportantPeople().
 * 
 * BeginConversation <person id> <minimal mode (no faction shown), optional> <show relationship bar>
 */
public class BeginConversation extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String id = params.get(0).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
		
		boolean minimal = false;
		boolean showRel = true;
		if (params.size() > 1) {
			minimal = params.get(1).getBoolean(memoryMap);
		}
		if (params.size() > 2) {
			showRel = params.get(2).getBoolean(memoryMap);
		}
		
		PersonDataAPI data = Global.getSector().getImportantPeople().getData(id);
		PersonAPI person = null;
		
		if (data == null) {
			if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null) {
				for (PersonAPI curr : dialog.getInteractionTarget().getMarket().getPeopleCopy()) {
					if (curr.getId().equals(id)) {
						person = curr;
						break;
					}
				}
				if (person == null) {
					CommDirectoryEntryAPI entry = dialog.getInteractionTarget().getMarket().getCommDirectory().getEntryForPerson(id);
					if (entry != null) {
						person = (PersonAPI) entry.getEntryData();
					}
				}
			}
		} else {
			person = data.getPerson();
		}
		if (person == null) return false;
		
		dialog.getInteractionTarget().setActivePerson(person);
		((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
		dialog.getVisualPanel().showPersonInfo(person, minimal, showRel);

		return true;
	}

}










