package com.fs.starfarer.api.campaign.rules;

import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;

public interface ExpressionAPI {
	Object execute(String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	boolean doesCommandAddOptions();
	int getOptionOrder(Map<String, MemoryAPI> memoryMap);
}
