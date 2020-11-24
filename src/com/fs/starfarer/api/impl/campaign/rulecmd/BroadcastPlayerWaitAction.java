package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;


public class BroadcastPlayerWaitAction extends BaseCommandPlugin {
	private EveryFrameScript broadcastScript;
	private VarAndMemory waitHandle;
	
	//BroadcastWaitAction <wait handle> <type> <range> <responseVariable>  
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		waitHandle = params.get(0).getVarNameAndMemory(memoryMap);

		final ActionType type = Enum.valueOf(ActionType.class, params.get(1).string);
		final float range = Float.parseFloat(params.get(2).string);
		final String responseVariable = params.get(3).string;
		
		final SectorEntityToken target = dialog.getInteractionTarget();
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		BroadcastPlayerAction.broadcast(type, range, responseVariable, playerFleet, target);
		broadcastScript = new EveryFrameScript() {
			private IntervalUtil tracker = new IntervalUtil(0.05f, 0.15f);
			private boolean done = false;

			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return done;
			}
			public void advance(float amount) {
				CampaignClockAPI clock = Global.getSector().getClock();
				
				float days = clock.convertToDays(amount);
				tracker.advance(days);
				
				if (tracker.intervalElapsed() && !done) {
					if (waitHandle.memory.contains(waitHandle.name)) {
						Wait wait = (Wait) waitHandle.memory.get(waitHandle.name);
						if (wait.getWaitScript().isDone()) {
							done = true;
							return;
						}
					} else {
						done = true;
						return;
					}
					BroadcastPlayerAction.broadcast(type, range, responseVariable, playerFleet, target);
				}
			}
		};
		
		Global.getSector().addScript(broadcastScript);
		
		return true;
	}


}




