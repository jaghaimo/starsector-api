package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;


public class GBCircle extends BaseGhostBehavior {
	
	protected SectorEntityToken other;
	protected int maxBurn;
	protected float desiredRange;
	protected float circleDir;
	
	public GBCircle(SectorEntityToken other, float duration, int maxBurn, float desiredRange, float circleDir) {
		super(duration);
		this.other = other;
		this.maxBurn = maxBurn;
		this.desiredRange = desiredRange;
		this.circleDir = circleDir;
	}

	@Override
	public void advance(float amount, SensorGhost ghost) {
		if (other.getContainingLocation() != ghost.getEntity().getContainingLocation() || !other.isAlive()) {
			end();
			return;
		}
		super.advance(amount, ghost);
		
		float dist = Misc.getDistance(ghost.getEntity(), other) - ghost.getEntity().getRadius() - other.getRadius();
		
		float dirToOther = Misc.getAngleInDegrees(ghost.getEntity().getLocation(), other.getLocation());
		//Vector2f toOther = Misc.getUnitVectorAtDegreeAngle(dirToOther);
		
		float clockwiseDir = dirToOther + 90f;
		
		float bandRadius = desiredRange * 0.25f;
		float distOffset = (dist - desiredRange);
		if (distOffset > bandRadius) distOffset = bandRadius;
		if (distOffset < -bandRadius) distOffset = -bandRadius;
		
		float angleOffset = distOffset / bandRadius * -60f;
		//if (angleOffset > 30f) angleOffset = 30f;
				
		
		float useCircleDir = circleDir;
		if (Misc.isReversePolarity(other)) {
			useCircleDir = -useCircleDir;
		}
		if (useCircleDir == 0f) {
			float velDir = Misc.getAngleInDegrees(ghost.getEntity().getVelocity());
			float angleDiffCW = Misc.getAngleDiff(dirToOther + 90f, velDir);
			float angleDiffCCW = Misc.getAngleDiff(dirToOther - 90f, velDir);
			if (angleDiffCW > angleDiffCCW) {
				useCircleDir = 1f;
			} else {
				useCircleDir = -1f;
			}
		}
		
		
		float moveDir = clockwiseDir;
		if (useCircleDir > 0) {
			moveDir += 180f;
			angleOffset = -angleOffset;
		}
		
		moveDir += angleOffset;
		moveDir = Misc.normalizeAngle(moveDir);
		
		Vector2f dest = Misc.getUnitVectorAtDegreeAngle(moveDir);
		dest.scale(10000f);
		Vector2f.add(dest, ghost.getEntity().getLocation(), dest);
		
		ghost.moveTo(dest, maxBurn);
	}
	
}













