package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission;
import com.fs.starfarer.api.util.Misc.Token;

/** 
 * Assumes BeginMission <id> false was called earlier.
 */
public class AbortMission extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		HubMission mission = (HubMission) Global.getSector().getMemoryWithoutUpdate().get(BeginMission.TEMP_MISSION_KEY);
		if (mission != null) {
			Global.getSector().getMemoryWithoutUpdate().unset(BeginMission.TEMP_MISSION_KEY);
			mission.abort();
			return true;
		}
		return false;
	}
}


