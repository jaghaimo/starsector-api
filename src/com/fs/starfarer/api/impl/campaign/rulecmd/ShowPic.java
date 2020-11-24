package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Parameter is key from settings.json under "illustrations".
 * 
 * @author Alex Mosolov
 *
 * Copyright 2017 Fractal Softworks, LLC
 */
public class ShowPic extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String key = params.get(0).getString(memoryMap);
		dialog.getVisualPanel().showImagePortion("illustrations", key, 640, 400, 0, 0, 480, 300);

		return true;
	}

}


