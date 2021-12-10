package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


public class GBFollowClosely extends BaseGhostBehavior {
	
	protected SectorEntityToken other;
	protected int maxBurn;
	protected float desiredRangeMin;
	protected float desiredRangeMax;
	protected IntervalUtil interval = new IntervalUtil(5f, 10f);
	protected float angleOffset = 0f;
	
	public GBFollowClosely(SectorEntityToken other, float duration, int maxBurn, float desiredRangeMin, float desiredRangeMax) {
		super(duration);
		this.other = other;
		this.maxBurn = maxBurn;
		this.desiredRangeMin = desiredRangeMin;
		this.desiredRangeMax = desiredRangeMax;
		interval.forceIntervalElapsed();
	}

	@Override
	public void advance(float amount, SensorGhost ghost) {
		if (other.getContainingLocation() != ghost.getEntity().getContainingLocation() || !other.isAlive()) {
			end();
			return;
		}
		super.advance(amount, ghost);
		
		interval.advance(amount * 1f);
		if (interval.intervalElapsed()) {
			angleOffset = 60f - Misc.random.nextFloat() * 120f;
		}
		
		float dist = Misc.getDistance(ghost.getEntity(), other);
		dist -= other.getRadius() + ghost.getEntity().getRadius();
		if (dist > desiredRangeMax) {
			ghost.moveTo(other.getLocation(), new Vector2f(), maxBurn);
		} else if (dist < desiredRangeMin) {
			float angle = Misc.getAngleInDegrees(other.getLocation(), ghost.getEntity().getLocation());
			angle += angleOffset;
			Vector2f dest = Misc.getUnitVectorAtDegreeAngle(angle);
			dest.scale(desiredRangeMin + (desiredRangeMax - desiredRangeMin) * 0.5f);
			Vector2f.add(other.getLocation(), dest, dest);
			ghost.moveTo(dest, new Vector2f(), maxBurn);
		} else if (other.getVelocity().length() > 10f) {
			int burn = (int) Misc.getBurnLevelForSpeed(other.getVelocity().length());
			if (burn < 1) burn = 1;
			Vector2f dest = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(other.getVelocity()) + angleOffset);
			dest.negate();
			dest.scale(desiredRangeMin + (desiredRangeMax - desiredRangeMin) * 0.5f);
			Vector2f.add(other.getLocation(), dest, dest);
			ghost.moveTo(dest, new Vector2f(), burn);
		}
	}
}













