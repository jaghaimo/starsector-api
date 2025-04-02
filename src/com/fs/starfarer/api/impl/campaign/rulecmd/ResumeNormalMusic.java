package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Ends custom music playback and restarts whatever the default music would be.
 */
public class ResumeNormalMusic extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
		Global.getSoundPlayer().restartCurrentMusic();
		return true;
	}
}


