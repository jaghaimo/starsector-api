package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

/**
 * Usage: AbortWait $waitHandle
 * 
 */
public class AbortWait extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		VarAndMemory handle = params.get(0).getVarNameAndMemory(memoryMap);

		if (handle.memory.contains(handle.name)) {
			Wait wait = (Wait) handle.memory.get(handle.name);
			
			Global.getSector().removeScript(wait.getWaitScript());
			Global.getSector().removeScript(wait.getLeashScript());
			wait.getIndicator().getContainingLocation().removeEntity(wait.getIndicator());
			
			wait.getFinished().memory.unset(wait.getFinished().name);
			wait.getInterrupted().memory.unset(wait.getInterrupted().name);
			wait.getInProgress().memory.unset(wait.getInProgress().name);
			
			handle.memory.unset(handle.name);
		}
		
		return true;
	}

}
