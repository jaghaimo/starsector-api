package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;


public class GBIntercept extends BaseGhostBehavior {
	
	protected SectorEntityToken other;
	protected int maxBurn;
	protected boolean endOnIntercept;
	
	protected float desiredRange;
	
	public GBIntercept(SectorEntityToken other, float duration, int maxBurn, boolean endOnIntercept) {
		this(other, duration, maxBurn, 0f, endOnIntercept);
	}
	
	public GBIntercept(SectorEntityToken other, float duration, int maxBurn, float desiredRange, boolean endOnIntercept) {
		super(duration);
		this.other = other;
		this.maxBurn = maxBurn;
		this.endOnIntercept = endOnIntercept;
		this.desiredRange = desiredRange;
	}

	@Override
	public void advance(float amount, SensorGhost ghost) {
		if (other.getContainingLocation() != ghost.getEntity().getContainingLocation() || !other.isAlive()) {
			end();
			return;
		}
		super.advance(amount, ghost);
		
		float speed = Misc.getSpeedForBurnLevel(maxBurn);
		Vector2f loc = Misc.getInterceptPoint(ghost.getEntity(), other, speed);
		if (loc != null) {
			ghost.moveTo(loc, maxBurn);
		}
		
		if (endOnIntercept) {
			float dist = Misc.getDistance(ghost.getEntity(), other);
			if (dist < ghost.getEntity().getRadius() + other.getRadius() + desiredRange) {
				end();
				return;
			}
		}
	}
	
	
	
}













