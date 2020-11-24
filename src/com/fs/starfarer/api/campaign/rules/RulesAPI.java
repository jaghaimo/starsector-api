package com.fs.starfarer.api.campaign.rules;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface RulesAPI {
	
	List<RuleAPI> getAllMatching(String currentRule, String trigger, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	RuleAPI getBestMatching(String currentRule, String trigger, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	
	void addTokenReplacementGenerator(RuleTokenReplacementGeneratorPlugin generator);
	Map<String, String> getTokenReplacements(String ruleId, SectorEntityToken target, Map<String, MemoryAPI> memoryMap);
	
	String performTokenReplacement(String ruleId, String text, SectorEntityToken entity, Map<String, MemoryAPI> memoryMap);
}
