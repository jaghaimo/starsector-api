package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class UninstallCommSniffer extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (dialog.getInteractionTarget() == null) return false;

		Global.getSector().getIntel().getCommSnifferLocations().remove(dialog.getInteractionTarget());
		dialog.getInteractionTarget().getMemoryWithoutUpdate().unset("$commSnifferInstalled");
		return true;
	}
}
