package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


public class GBDartAround extends BaseGhostBehavior {
	
	protected SectorEntityToken other;
	protected int maxBurn;
	protected float desiredRange;
	protected float desiredRangeMin;
	protected float desiredRangeMax;
	protected IntervalUtil interval = new IntervalUtil(5f, 10f);
	protected float angleOffset = 0f;
	protected Vector2f dest = new Vector2f();
	
	public GBDartAround(SectorEntityToken other, float duration, int maxBurn, float desiredRangeMin, float desiredRangeMax) {
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
		
		float mult = 1f;
		if (maxBurn >= 25f) mult = 3f;
		else if (maxBurn >= 15f) mult = 2f;
		else mult = 1.5f;
		interval.advance(amount * mult);
		if (interval.intervalElapsed()) {
			angleOffset = (float) Math.random() * 360f;
			desiredRange = desiredRangeMin + (desiredRangeMax - desiredRangeMin) * (float) Math.random();
			dest = Misc.getUnitVectorAtDegreeAngle(
					angleOffset + Misc.getAngleInDegrees(other.getLocation(), ghost.getEntity().getLocation()));
			dest.scale(desiredRange + other.getRadius() + ghost.getEntity().getRadius());
			Vector2f.add(dest, other.getLocation(), dest);
		}
		
		if (Misc.getDistance(dest, ghost.getEntity().getLocation()) < 10f) {
			interval.forceIntervalElapsed();
		}
		
		ghost.moveTo(dest, new Vector2f(), maxBurn);
	}
	
	
	
}













