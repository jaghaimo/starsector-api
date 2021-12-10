package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;

public class GBITowardsDirection extends BaseGhostBehaviorInterrupt {

	protected float facing;
	protected SectorEntityToken from;
	
	public GBITowardsDirection(float delay, SectorEntityToken from, float facing) {
		super(delay);
		this.from = from;
		this.facing = facing;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;
		
		float grace = 15f;
		float angle = Misc.getAngleInDegrees(from.getLocation(), ghost.getEntity().getLocation());
		
		return Misc.getAngleDiff(angle, facing) < grace;
	}

	
}
