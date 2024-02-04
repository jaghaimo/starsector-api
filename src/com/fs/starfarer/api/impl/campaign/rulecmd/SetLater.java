package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

/**
 * SetLater <variable> <delay in days>
 */
public class SetLater extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		//String flag = params.get(0).getString(memoryMap);
		final VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
		final float delay = params.get(1).getFloat(memoryMap);
		
		Global.getSector().addScript(new EveryFrameScript() {
			float elapsed = 0f;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return elapsed > delay;
			}
			public void advance(float amount) {
				if (elapsed > delay) {
					return;
				}
				elapsed += amount;
				if (elapsed > delay) {
					var.memory.set(var.name, true);
				}
			}
		});
		
		return true;
	}

}
