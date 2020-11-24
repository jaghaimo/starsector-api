package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetPromptText <text>
 * 
 * "-" is a special value for no text
 * 
 * Copyright 2015 Fractal Softworks, LLC
 */
public class SetPromptText extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		//Global.getSector().setPaused(false);
		String text = params.get(0).getString(memoryMap);
		if (dialog != null) {
			dialog.setPromptText(text);
		}
		return true;
	}

}
