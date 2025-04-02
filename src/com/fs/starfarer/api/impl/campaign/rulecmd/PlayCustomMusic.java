package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Plays custom music with the provided ID.
 * MUST call ResumeNormalMusic at some point after this to re-enable normal music playback, otherwise
 * normal music will not play.
 * Can call PauseMusic to stop all music playback.
 */
public class PlayCustomMusic extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String musicId = params.get(0).getString(memoryMap);
		Global.getSoundPlayer().playCustomMusic(1, 1, musicId, true);
		return true;
	}
}


