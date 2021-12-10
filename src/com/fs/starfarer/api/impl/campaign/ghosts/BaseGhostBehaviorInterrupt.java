package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.Global;

public class BaseGhostBehaviorInterrupt implements GhostBehaviorInterrupt {
	protected float delay;
	
	public BaseGhostBehaviorInterrupt(float delay) {
		this.delay = delay;
	}

	public void advance(float amount, SensorGhost ghost, GhostBehavior behavior) {
		float days = Global.getSector().getClock().convertToDays(amount);
		delay -= days;
	}
	
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		return false;
	}
	
	protected boolean hasDelayRemaining() {
		return delay > 0;
	}


}
