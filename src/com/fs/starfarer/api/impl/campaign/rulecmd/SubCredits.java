package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	SubCredits <credits>
 */
public class SubCredits extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		float credits = params.get(0).getFloat(memoryMap);
		
		Global.getSector().getPlayerFleet().getCargo().getCredits().add(-1 * (int)credits);
		if (Global.getSector().getPlayerFleet().getCargo().getCredits().get() < 0) {
			Global.getSector().getPlayerFleet().getCargo().getCredits().set(0);
		}
		
		
		MemoryAPI memory = Global.getSector().getCharacterData().getMemoryWithoutUpdate();
		memory.set("$credits", (int)Global.getSector().getPlayerFleet().getCargo().getCredits().get(), 0);
		memory.set("$creditsStr", Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()), 0);
		
		return true;
	}

}
