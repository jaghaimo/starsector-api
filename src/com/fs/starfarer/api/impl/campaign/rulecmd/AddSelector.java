package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.ValueDisplayMode;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

public class AddSelector extends BaseCommandPlugin {

	
	//AddSelector <order> <result variable> <text> <color> <min> <max>
	public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		final VarAndMemory resultVar = params.get(1).getVarNameAndMemory(memoryMap);
		final String text = params.get(2).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
		final Color color = params.get(3).getColor(memoryMap);
		final float min = params.get(4).getFloat(memoryMap);
		final float max = params.get(5).getFloat(memoryMap);
		
		final String id = Misc.genUID();
		final OptionPanelAPI options = dialog.getOptionPanel();
		options.addSelector(text, id, color, 250, 50, min, max, ValueDisplayMode.VALUE, null);
		
		Global.getSector().addTransientScript(new EveryFrameScript() {
			public boolean runWhilePaused() {
				return true;
			}
			private boolean done = false;
			public boolean isDone() {
				return done;
			}
			public void advance(float amount) {
				if (done ||
						!options.hasSelector(id) || 
						!Global.getSector().isPaused() ||
						!Global.getSector().getCampaignUI().isShowingDialog()) {
					done = true;
					return;
				}
				resultVar.memory.set(resultVar.name, Math.round(options.getSelectorValue(id)), 0);
			}
		});
		
		return true;
	}

	@Override
	public boolean doesCommandAddOptions() {
		return true;
	}

	@Override
	public int getOptionOrder(List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		int order = (int) params.get(0).getFloat(memoryMap);
		return order;
	}

	
}
