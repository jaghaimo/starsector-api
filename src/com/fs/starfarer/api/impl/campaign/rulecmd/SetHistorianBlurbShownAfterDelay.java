package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * SetHistorianBlurbShownAfterDelay <blurb id>
 */
public class SetHistorianBlurbShownAfterDelay extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		final String id = params.get(0).getString(memoryMap);
		final float delay = 300f;
		
		Global.getSector().addScript(new EveryFrameScript() {
			float elapsed = 0f;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return elapsed > delay;
			}
			public void advance(float amount) {
				if (elapsed > delay) {
					return;
				}
				elapsed += amount;
				if (elapsed > delay) {
					SharedData.getData().getUniqueEncounterData().historianBlurbsShown.add(id);
				}
			}
		});
		
		return true;
	}

}
