package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class SetTooltipHighlightColors extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String option = params.get(0).string;
		List<Color> colors = new ArrayList<Color>();
		for (int i = 1; i < params.size(); i++) {
			Color color = params.get(i).getColor(memoryMap);
			colors.add(color);
		}
	
		dialog.getOptionPanel().setTooltipHighlightColors(option, colors.toArray(new Color[0]));
		return true;
	}

}
