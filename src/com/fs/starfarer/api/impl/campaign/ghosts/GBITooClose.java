package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;

public class GBITooClose extends BaseGhostBehaviorInterrupt {

	protected float distThreshold;
	protected SectorEntityToken to;
	protected SharedTrigger trigger;
	
	public GBITooClose(float delay, SectorEntityToken to, float distThreshold) {
		this(delay, to, distThreshold, null);
	}
	public GBITooClose(float delay, SectorEntityToken to, float distThreshold, SharedTrigger trigger) {
		super(delay);
		this.distThreshold = distThreshold;
		this.to = to;
		this.trigger = trigger;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;
		
		float dist = Misc.getDistance(to, ghost.getEntity());
		dist -= to.getRadius();
		dist -= ghost.getEntity().getRadius();
		
		boolean inRange = dist < distThreshold;
		if (inRange && trigger != null) {
			trigger.set(true);
		}
		return inRange || (trigger != null && trigger.isSet());
	}

	
}
