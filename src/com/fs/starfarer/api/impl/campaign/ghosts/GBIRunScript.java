package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.Script;

public class GBIRunScript extends BaseGhostBehaviorInterrupt {

	protected Script script;
	protected boolean endBehaviorWhenRun;

	public GBIRunScript(float delay, Script script, boolean endBehaviorWhenRun) {
		super(delay);
		this.script = script;
		this.endBehaviorWhenRun = endBehaviorWhenRun;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;

		if (script != null) {
			script.run();
			script = null;
		}
		
		return endBehaviorWhenRun;
	}

	
}
