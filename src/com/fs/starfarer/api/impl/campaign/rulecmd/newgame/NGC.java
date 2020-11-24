package com.fs.starfarer.api.impl.campaign.rulecmd.newgame;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	Not actually used.
 */
public class NGC extends BaseCommandPlugin {
	@Override
	public boolean doesCommandAddOptions() {
		return true;
	}

	@Override
	public int getOptionOrder(List<Token> params, Map<String, MemoryAPI> memoryMap) {
		return super.getOptionOrder(params, memoryMap);
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String command = params.get(0).getString(memoryMap);
		CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
		
		
		if ("test".equals(command)) {
			dialog.getOptionPanel().addOption("Testing 123", "sdfefew");
			dialog.getOptionPanel().addOption("Testing 123aaaa", "sdfefew");
		}
		
		if ("done".equals(command)) {
			data.setDone(true);
		}
		
		
		return true;
	}

}









