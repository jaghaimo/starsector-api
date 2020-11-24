package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	LPTitheCalc
 */
public class LPTitheCalc extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
		float tithe = (int) Global.getSector().getPlayerFleet().getFleetPoints() * 200;
		
		memoryMap.get(MemKeys.LOCAL).set("$LP_tithe", (int)tithe, 0);
		memoryMap.get(MemKeys.LOCAL).set("$LP_titheDGS", Misc.getWithDGS(tithe), 0);
		return credits >= tithe;
	}

}
