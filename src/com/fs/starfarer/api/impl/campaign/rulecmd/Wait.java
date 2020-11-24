package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.BaseCampaignEventListenerAndScript;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignProgressIndicatorAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.LeashScript;
import com.fs.starfarer.api.util.RuleException;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;


public class Wait extends BaseCommandPlugin {
	private EveryFrameScript waitScript;
	private LeashScript leash;
	private CampaignProgressIndicatorAPI indicator;
	private VarAndMemory handle;
	private VarAndMemory finished;
	private VarAndMemory interrupted;
	private VarAndMemory inProgress;
	
	//Wait $handle duration $finished $interrupted $inProgress $text
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (params.size() != 6) {
			throw new RuleException("Wait usage: Wait $handle duration $finished $interrupted $inProgress <text>");
		}
		
		handle = params.get(0).getVarNameAndMemory(memoryMap);
		final float durationDays = Float.parseFloat(params.get(1).string);
		finished = params.get(2).getVarNameAndMemory(memoryMap);
		interrupted = params.get(3).getVarNameAndMemory(memoryMap);
		inProgress = params.get(4).getVarNameAndMemory(memoryMap);
		
		String text = "Waiting";
		if (params.size() >= 5) {
			text = params.get(5).getString(memoryMap);
		}
		
		//Global.getSoundPlayer().playUISound("ui_wait_start", 1, 1);
		
		final SectorEntityToken target = dialog.getInteractionTarget();
		
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		playerFleet.setInteractionTarget(null);
		Vector2f offset = Vector2f.sub(playerFleet.getLocation(), target.getLocation(), new Vector2f());
//		float dir = Misc.getAngleInDegrees(offset);
//		offset = Misc.getUnitVectorAtDegreeAngle(dir)
		float len = offset.length();
		float radSum = playerFleet.getRadius() + target.getRadius() - 1f;
		if (len > 0) {
			offset.scale(radSum / len);
		} else {
			offset.set(radSum, 0);
		}
		
		indicator = Global.getFactory().createProgressIndicator(text, target, durationDays);
		target.getContainingLocation().addEntity(indicator);
		
		
		waitScript = new BaseCampaignEventListenerAndScript(durationDays + 0.1f) {
			private float elapsedDays = 0f;
			private boolean done = false;
			private boolean battleOccured = false;
			private boolean interactedWithSomethingElse = false;
			public boolean runWhilePaused() {
				return false;
			}
			public boolean isDone() {
				return done;
			}
			public void advance(float amount) {
				CampaignClockAPI clock = Global.getSector().getClock();
				
				Global.getSector().getCampaignUI().setDisallowPlayerInteractionsForOneFrame();
				
				float days = clock.convertToDays(amount);
				elapsedDays += days;
				
				inProgress.memory.set(inProgress.name, true);
				inProgress.memory.expire(inProgress.name, 0.1f);
				
//				float sinceLastBattle = clock.getElapsedDaysSince(Global.getSector().getLastPlayerBattleTimestamp());
//				if (sinceLastBattle <= elapsedDays) {
				if (battleOccured || interactedWithSomethingElse) {
					done = true;
					interrupted.memory.set(interrupted.name, true);
					interrupted.memory.expire(interrupted.name, 2f);
					handle.memory.unset(handle.name);
					indicator.interrupt();
					Global.getSector().removeScript(leash);
					
					Global.getSoundPlayer().playUISound("ui_wait_interrupt", 1, 1);
				} else if (elapsedDays >= durationDays && !Global.getSector().getCampaignUI().isShowingDialog()) {
					done = true;
					finished.memory.set(finished.name, true);
					finished.memory.expire(finished.name, 0f);
					
					inProgress.memory.unset(inProgress.name);
					handle.memory.unset(handle.name);
					
					Global.getSector().removeScript(leash);
					indicator.getContainingLocation().removeEntity(indicator);
					Global.getSector().getCampaignUI().showInteractionDialog(target);
					Global.getSoundPlayer().playUISound("ui_wait_finish", 1, 1);
				}
			}
			@Override
			public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
				if (battle.getSnapshotSideFor(playerFleet) != null || 
						(target instanceof CampaignFleetAPI && battle.getSnapshotSideFor((CampaignFleetAPI)target) != null)) {
					battleOccured = true;
				}
			}
			@Override
			public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
				interactedWithSomethingElse |= dialog.getInteractionTarget() != target;
			}
			@Override
			public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
				if (fleet == playerFleet || fleet == target) {
					battleOccured = true;
				}
			}
			
		};
		
		leash = new LeashScript(playerFleet, 50, target, offset, new Script() {
			public void run() {
				interrupted.memory.set(interrupted.name, true);
				interrupted.memory.expire(interrupted.name, 2f);
//				memory.set(keyInterrupted, true);
//				memory.expire(keyInterrupted, 2f);
				//indicator.getContainingLocation().removeEntity(indicator);
				handle.memory.unset(handle.name);
				indicator.interrupt();
				Global.getSector().removeScript(waitScript);
				Global.getSoundPlayer().playUISound("ui_wait_interrupt", 1, 1);
			}
		});
		Global.getSector().addScript(leash);
		
		handle.memory.set(handle.name, this);
		
		Global.getSector().addScript(waitScript);
		
		Global.getSector().setPaused(false);
		dialog.dismiss();
		
		return true;
	}

	public EveryFrameScript getWaitScript() {
		return waitScript;
	}

	public LeashScript getLeashScript() {
		return leash;
	}

	public CampaignProgressIndicatorAPI getIndicator() {
		return indicator;
	}

	public VarAndMemory getHandle() {
		return handle;
	}

	public VarAndMemory getFinished() {
		return finished;
	}

	public VarAndMemory getInProgress() {
		return inProgress;
	}

	public VarAndMemory getInterrupted() {
		return interrupted;
	}

}




