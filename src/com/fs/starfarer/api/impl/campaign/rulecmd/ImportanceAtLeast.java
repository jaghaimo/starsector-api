package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * gte = greater than or equals
 * ImportanceAtLeast $importance test
 */
public class ImportanceAtLeast extends RepIsAtWorst {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String s1 = params.get(0).getString(memoryMap);
		String s2 = params.get(0).getString(memoryMap);
		PersonImportance value = PersonImportance.valueOf(s1);
		PersonImportance test = PersonImportance.valueOf(s2);
		return value.ordinal() >= test.ordinal();
	}

}
