package com.fs.starfarer.api.impl.campaign.rulecmd.academy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Unhide the comm directory entities for baird and sebestyen in the Galatia Academy
 */
public class UnhideGACharacters extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		if (dialog.getInteractionTarget().getMarket() == null) return false;
		if (dialog.getInteractionTarget().getMarket().getCommDirectory() == null) return false;
		if (!dialog.getInteractionTarget().getId().equals("station_galatia_academy")) return false;
		
		if (dialog.getInteractionTarget().getMarket().getCommDirectory().getEntryForPerson("baird") != null) {
			dialog.getInteractionTarget().getMarket().getCommDirectory().getEntryForPerson("baird").setHidden(false);
		}
		if (dialog.getInteractionTarget().getMarket().getCommDirectory().getEntryForPerson("sebestyen") != null) {
			dialog.getInteractionTarget().getMarket().getCommDirectory().getEntryForPerson("sebestyen").setHidden(false);
		}

		return true;
	}
}










