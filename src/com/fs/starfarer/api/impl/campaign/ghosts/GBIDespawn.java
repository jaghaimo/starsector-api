package com.fs.starfarer.api.impl.campaign.ghosts;

public class GBIDespawn extends BaseGhostBehaviorInterrupt {

	public GBIDespawn(float delay) {
		super(delay);
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;
		
		ghost.clearScript();
		
		return true;
	}

	
}
