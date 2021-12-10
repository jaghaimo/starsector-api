package com.fs.starfarer.api.impl.campaign.ghosts;

public interface GhostBehaviorInterrupt {
	public void advance(float amount, SensorGhost ghost, GhostBehavior behavior);
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior);
}
