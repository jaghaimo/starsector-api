package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class PaginatedOptionsExample extends PaginatedOptions {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		super.execute(ruleId, dialog, params, memoryMap);
		
		optionsPerPage = 5;
		
		for (int i = 0; i < 14; i++) {
			addOption("Option " + i, "o" + i);
		}
		
		showOptions();
		return true;
	}

	@Override
	public String getNextPageText() {
		return super.getNextPageText();
	}

	@Override
	public String getPreviousPageText() {
		return super.getPreviousPageText();
	}
	
}

