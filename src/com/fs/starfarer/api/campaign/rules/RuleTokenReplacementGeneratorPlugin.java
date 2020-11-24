package com.fs.starfarer.api.campaign.rules;

import java.util.Map;

public interface RuleTokenReplacementGeneratorPlugin {
	/**
	 * Entity can be:
	 * SectorEntityToken
	 * PersonAPI
	 * 
	 * @param ruleId
	 * @param entity
	 * @param memoryMap
	 * @return
	 */
	Map<String, String> getTokenReplacements(String ruleId, Object entity, Map<String, MemoryAPI> memoryMap);
}
