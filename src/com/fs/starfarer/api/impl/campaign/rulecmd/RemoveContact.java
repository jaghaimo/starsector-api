package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * RemoveContact <optional person id>
 */
public class RemoveContact extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (entity == null) return false;
		
		PersonAPI person = null;
		if (params.size() > 0) {
			String personId = params.get(0).getString(memoryMap);
			PersonDataAPI data = Global.getSector().getImportantPeople().getData(personId);
			if (data != null) {
				person = data.getPerson();
			}
		}
		
		if (person == null) {
			person = entity.getActivePerson();
		}
		if (person == null) return false;
		
		ContactIntel.removeContact(person, dialog);
		return true;
	}

}








