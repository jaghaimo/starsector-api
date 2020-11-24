package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

/**
 * CallEvent $eventHandle <params> 
 * 
 */
public class CallEvent extends BaseCommandPlugin {
	
	public static interface CallableEvent {
		boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap);
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		VarAndMemory handle = params.get(0).getVarNameAndMemory(memoryMap);
		if (handle.memory.contains(handle.name)) {
			CallableEvent event = (CallableEvent) handle.memory.get(handle.name);
			
			if (event != null) {
				List<Token> notifyParams = new ArrayList<Token>();
				for (int i = 1; i < params.size(); i++) {
					notifyParams.add(params.get(i));
					//params.get(i).getString(memoryMap)
				}
				return event.callEvent(ruleId, dialog, notifyParams, memoryMap);
			}
		}
		
		return false;
	}

}




