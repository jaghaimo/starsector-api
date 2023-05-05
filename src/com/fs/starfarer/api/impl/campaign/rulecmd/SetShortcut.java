package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetShortcut <option> <key> (from Keyboard, after KEY_) <putLast>
 */
public class SetShortcut extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String option = params.get(0).string;
		String keyName = params.get(1).string;
		boolean putLast = true;
		if (params.size() >= 3) {
			putLast = params.get(2).getBoolean(memoryMap);
		}

		int code = Global.getSettings().getCodeFor(keyName);
		if (code == -1) return false;
		
		dialog.getOptionPanel().setShortcut(option, code, false, false, false, putLast);
		return true;
	}

}
