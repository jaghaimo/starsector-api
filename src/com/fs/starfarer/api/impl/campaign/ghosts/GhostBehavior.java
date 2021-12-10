package com.fs.starfarer.api.impl.campaign.ghosts;

public interface GhostBehavior {
	public boolean isDone();
	public void advance(float amount, SensorGhost ghost);
	void addInterrupt(GhostBehaviorInterrupt interrupt); 
}
