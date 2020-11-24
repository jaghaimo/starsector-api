package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class unsetAll extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		String string = params.get(0).string;
		int index = string.indexOf(".");
		String memoryKey;
		String varPrefix;
		if (index > 0) {
			memoryKey = string.substring(1, index);
			varPrefix = "$" + string.substring(index + 1);
		} else {
			memoryKey = MemKeys.LOCAL;
			varPrefix = string;
		}
		
		MemoryAPI memory = memoryMap.get(memoryKey);
		List<String> unset = new ArrayList<String>();
		for (String key : memory.getKeys()) {
			if (key.startsWith(varPrefix)) {
				unset.add(key);
			}
		}
		for (String key : unset) {
			memory.unset(key);
		}

		
		return true;
	}

}
