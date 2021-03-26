package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

/**
 * SetPersonHidden set person hidden boolean by character id
 */
public class SetPersonHidden extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		// if you wanted to just pass in the person ID. The person has to have been
		// added to ImportantPeople at some point (as the people defined in People are)
		String id = params.get(0).getString(memoryMap);
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(id);
		
		//VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
		//PersonAPI person = (PersonAPI) var.memory.get(var.name);
		
		boolean hidden = params.get(1).getBoolean(memoryMap);
		
		if (person.getMarket() == null) return false;
		if (person.getMarket().getCommDirectory() == null) return false;
		if (person.getMarket().getCommDirectory().getEntryForPerson(person.getId()) == null) return false;

		person.getMarket().getCommDirectory().getEntryForPerson(person.getId()).setHidden(hidden);
		return true;
		
	}
}
