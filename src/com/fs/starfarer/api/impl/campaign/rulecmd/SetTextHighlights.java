package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class SetTextHighlights extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		List<String> strings = new ArrayList<String>();
		//Map<String, String> tokens = Global.getSector().getRules().getTokenReplacements(ruleId, dialog.getInteractionTarget(), memoryMap);
		for (int i = 0; i < params.size(); i++) {
			String string = null;
			
			string = params.get(i).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
//			if (params.get(i).isVariable()) {
//				string = tokens.get(params.get(i).string);
//				if (string == null) {
//					VarAndMemory var = params.get(i).getVarNameAndMemory(memoryMap);
//					string = var.memory.getString(var.name);
//				}
//			} else {
//				string = params.get(i).string;
//			}
			if (string != null) strings.add(string);
		}
	
		dialog.getTextPanel().highlightInLastPara(strings.toArray(new String[0]));
		
		return true;
	}

}


