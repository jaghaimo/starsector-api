package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;

public class GBITowardsEntity extends BaseGhostBehaviorInterrupt {

	protected SectorEntityToken from;
	protected SectorEntityToken to;
	
	public GBITowardsEntity(float delay, SectorEntityToken from, SectorEntityToken to) {
		super(delay);
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;
		
		float grace = 15f;
		float angle = Misc.getAngleInDegrees(from.getLocation(), ghost.getEntity().getLocation());
		float facing = Misc.getAngleInDegrees(from.getLocation(), to.getLocation());
		
		return Misc.getAngleDiff(angle, facing) < grace;
	}

	
}
