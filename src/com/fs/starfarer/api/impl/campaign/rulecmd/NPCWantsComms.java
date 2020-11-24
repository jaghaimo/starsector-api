package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class NPCWantsComms extends BaseCommandPlugin {

	/** 
	 * OpenCoreUI <CoreUITabId>
	 */
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (!(dialog.getPlugin() instanceof RuleBasedDialog)) return false;
		if (dialog.getInteractionTarget().getMarket() == null) return false;
		
		
		
		List<PersonAPI> people = dialog.getInteractionTarget().getMarket().getPeopleCopy();
		//System.out.println("RANKS:");
		for (PersonAPI person : people) { 
//			if (person.getRank().toLowerCase().contains("trader")) {
//				System.out.println("sdfkhsfjekwfh");
//			}
//			System.out.println("Rank: " + person.getRank());
			if (person.wantsToContactPlayer()) {
				return true;
			}
		}
		return false;
	}

}
