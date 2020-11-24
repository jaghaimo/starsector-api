package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class EndConversation extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		FleetInteractionDialogPluginImpl.inConversation = false;
		
		boolean doNotFire = false;
		boolean withContinue = true;
		if (params.size() > 0) {
			String str = params.get(0).getString(memoryMap);
			if (str != null && str.equals("DO_NOT_FIRE")) {
				doNotFire = true;
			}
			if (str != null && str.equals("NO_CONTINUE")) {
				withContinue = false;
			}
		}
		
		if (!withContinue) {
			new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), memoryMap);
		}
		
		if (dialog.getPlugin() instanceof RuleBasedDialog) {
			dialog.getInteractionTarget().setActivePerson(null);
			((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
			
			if (!doNotFire) {
				if (dialog.getPlugin() instanceof FleetInteractionDialogPluginImpl) {
					//((FleetInteractionDialogPluginImpl)dialog.getPlugin()).optionSelected(null, OptionId.INIT);
					((RuleBasedDialog) dialog.getPlugin()).reinit(withContinue);
				} else {
					MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
					if (local != null && local.getBoolean("$hasMarket") && !local.contains("$menuState")) {
						FireBest.fire(null, dialog, memoryMap, "MarketPostOpen");
					} else {
						FireAll.fire(null, dialog, memoryMap, "PopulateOptions");
					}
				}
			}
		}
		return true;
	}

}
