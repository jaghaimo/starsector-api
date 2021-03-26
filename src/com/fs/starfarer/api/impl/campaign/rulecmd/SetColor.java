package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetColor $variable color
 */
public class SetColor extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String variable = params.get(0).string;
		Color color = params.get(1).getColor(memoryMap);
		
		memoryMap.get(MemKeys.LOCAL).set(variable, color);
		
		return true;
	}

}
