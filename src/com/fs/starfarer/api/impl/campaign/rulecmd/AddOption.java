package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class AddOption extends BaseCommandPlugin {

	
	//AddSelector <order> <result variable> <text> <color> <min> <max>
	public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String id = params.get(1).getString(memoryMap);
		String text = params.get(2).getString(memoryMap);
		
		OptionPanelAPI options = dialog.getOptionPanel();
		options.addOption(text, id);
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
