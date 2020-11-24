package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;


/**
 * SetActiveMission $missionEventHandle
 */
public class SetActiveMission extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (!(dialog.getPlugin() instanceof RuleBasedDialog)) return false;
		
		VarAndMemory handle = params.get(0).getVarNameAndMemory(memoryMap);
		if (handle.memory.contains(handle.name)) {
			CampaignEventPlugin mission = (CampaignEventPlugin) handle.memory.get(handle.name);
			if (mission != null) {
				((RuleBasedDialog) dialog.getPlugin()).setActiveMission(mission);
			}
		}
		
		return true;
	}

}




