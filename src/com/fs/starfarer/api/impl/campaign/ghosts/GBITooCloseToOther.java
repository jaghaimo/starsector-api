package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;

public class GBITooCloseToOther extends BaseGhostBehaviorInterrupt {

	protected float distThreshold;
	protected SectorEntityToken to;
	protected SectorEntityToken other;
	
	public GBITooCloseToOther(float delay, SectorEntityToken to, SectorEntityToken other, float distThreshold) {
		super(delay);
		this.distThreshold = distThreshold;
		this.to = to;
		this.other = other;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;
		
		float dist = Misc.getDistance(to, other);
		dist -= to.getRadius();
		dist -= other.getRadius();
		
		boolean inRange = dist < distThreshold;
		return inRange;
	}

	
}
