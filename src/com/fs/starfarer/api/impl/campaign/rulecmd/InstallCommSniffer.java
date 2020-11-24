package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class InstallCommSniffer extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (dialog.getInteractionTarget() == null) return false;

		Global.getSector().getIntel().getCommSnifferLocations().add(dialog.getInteractionTarget());
		dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$commSnifferInstalled", true);
		return true;
	}
}
