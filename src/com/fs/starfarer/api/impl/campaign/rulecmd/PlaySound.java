package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * The sound should be stereo.
 * 
 * PlaySound <sound id>
 */
public class PlaySound extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String soundId = params.get(0).getString(memoryMap);
		Global.getSoundPlayer().playUISound(soundId, 1f, 1f);
		return true;
	}
}


