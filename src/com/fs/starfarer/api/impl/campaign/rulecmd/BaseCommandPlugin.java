package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public abstract class BaseCommandPlugin implements CommandPlugin {
	public boolean doesCommandAddOptions() { 
		return false;
	}

	public int getOptionOrder(List<Token> params, Map<String, MemoryAPI> memoryMap) {
		return 0;
	}
	
	public static MemoryAPI getEntityMemory(Map<String, MemoryAPI> memoryMap) {
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memoryMap.containsKey(MemKeys.ENTITY)) {
			memory = memoryMap.get(MemKeys.ENTITY);
		}
		return memory;
	}
}
