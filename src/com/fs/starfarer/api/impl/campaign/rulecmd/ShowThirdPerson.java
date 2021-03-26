package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ShowSecondPerson <id>
 */
public class ShowThirdPerson extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String id = params.get(0).getString(memoryMap);
		
		PersonDataAPI data = Global.getSector().getImportantPeople().getData(id);
		if (data == null) return true;
		PersonAPI person = data.getPerson();
		if (person == null) return true;
		
		dialog.getVisualPanel().showThirdPerson(person);
		return true;
	}

}
