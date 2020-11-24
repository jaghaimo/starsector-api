package com.fs.starfarer.api.campaign.rules;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;

public interface RuleAPI {
	
	List<String> getText();
	String pickText();
	String getId();
	String getTrigger();

	List<Option> getOptions();
	
	/**
	 * @param dialog can be null.
	 * @param memory
	 */
	void runScript(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	List<ExpressionAPI> getScriptCopy();
}
