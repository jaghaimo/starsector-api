package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class MakePlayerImmediatelyAttackable extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		EveryFrameScript script = new EveryFrameScript() {
			private boolean done = false;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return done;
			}
			public void advance(float amount) {
				if (!Global.getSector().getCampaignUI().isShowingDialog()) {
					Global.getSector().getPlayerFleet().setNoEngaging(0);
					done = true;
				}
			}
		};
		Global.getSector().addScript(script);
		
		return true;
	}

}
