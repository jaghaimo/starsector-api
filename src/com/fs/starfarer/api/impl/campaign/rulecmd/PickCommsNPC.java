package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * OpenCoreTab <CoreUITabId> <CoreUITradeMode (optional)>
 */
public class PickCommsNPC extends BaseCommandPlugin {

	/** 
	 * OpenCoreUI <CoreUITabId>
	 */
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (!(dialog.getPlugin() instanceof RuleBasedDialog)) return false;
		if (dialog.getInteractionTarget().getMarket() == null) return false;
		
		List<PersonAPI> people = dialog.getInteractionTarget().getMarket().getPeopleCopy();
		WeightedRandomPicker<PersonAPI> picker = new WeightedRandomPicker<PersonAPI>();
		for (PersonAPI person : people) { 
			if (person.wantsToContactPlayer()) {
				picker.add(person, person.getContactWeight());
			}
		}
		
		if (picker.isEmpty()) return false;
		
		PersonAPI pick = picker.pick();
		dialog.getInteractionTarget().setActivePerson(pick);
		((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
		
		return true;
	}

}



