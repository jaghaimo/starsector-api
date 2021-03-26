package com.fs.starfarer.api.impl.campaign.rulecmd.academy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * "I'll buy you a pint on coatl station"
 */
public class AddGAOfficerToCoatl extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(People.HEGEMONY_GA_OFFICER);
		if (person == null) return false;
		

		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (!market.getId().equals("coatl")) return false;
		
		market.getCommDirectory().addPerson(person);
		market.addPerson(person);

		return true;
	}
}










