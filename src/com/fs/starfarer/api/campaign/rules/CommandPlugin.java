package com.fs.starfarer.api.campaign.rules;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.util.Misc.Token;

public interface CommandPlugin {
	boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap);
	boolean doesCommandAddOptions();
	int getOptionOrder(List<Token> params, Map<String, MemoryAPI> memoryMap);
}
