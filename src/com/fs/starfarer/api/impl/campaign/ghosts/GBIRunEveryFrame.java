package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.Script;

public class GBIRunEveryFrame extends BaseGhostBehaviorInterrupt {

	protected Script script;

	public GBIRunEveryFrame(float delay, Script script) {
		super(delay);
		this.script = script;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;

		if (script != null) {
			script.run();
		}
		return false;
	}

	
}
