package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class AddTextSmall extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String text = params.get(0).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
		
		if (text == null || text.isEmpty()) return true;
		
		
		String [] split = text.split("(?s)\nOR\n");
		if (split != null && split.length > 1) {
			text = split[new Random().nextInt(split.length)];
			text = text.trim();
		}
		
		Color color = null;
		if (params.size() > 1) {
			color = params.get(1).getColor(memoryMap);
		}

		dialog.getTextPanel().setFontSmallInsignia();
		if (color == null) {
			dialog.getTextPanel().addParagraph(text);
		} else {
			dialog.getTextPanel().addParagraph(text, color); 
		}
		dialog.getTextPanel().setFontInsignia();
		return true;
	}

}
