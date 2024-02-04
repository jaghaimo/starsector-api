package com.fs.starfarer.api.util;

public class SmoothFacingUtil {

	private float turnAcceleration, maxTurnRate;
	private float turnRate;
	
	private float facing;
	
	public SmoothFacingUtil(float turnAcceleration, float maxTurnRate) {
		this.turnAcceleration = turnAcceleration;
		this.maxTurnRate = maxTurnRate;
	}

	
	public void advance(float desiredFacing, float amount) {
		float absTurnRate = Math.abs(turnRate);
		
		// v t - 0.5 a t t = dist
		// dv = a t;  t = v / a
		float decelTime = absTurnRate / turnAcceleration; 
		float decelDistance = absTurnRate * decelTime - 0.5f * turnAcceleration * decelTime * decelTime;
		
		float diffWithCurrFacing = Misc.getAngleDiff(facing, desiredFacing);

		float turnDir = Misc.getClosestTurnDirection(facing, desiredFacing);
		if (Math.signum(turnRate) == Math.signum(turnDir)) {
			if (decelDistance >= diffWithCurrFacing) {
				turnDir = -turnDir;
			}
		}
		
		turnRate += turnDir * turnAcceleration * amount;
		//turnRate = maxTurnRate * turnDir;
		if (Math.abs(turnRate) > maxTurnRate) {
			turnRate = maxTurnRate * Math.signum(turnRate);
			//System.out.println("capped at " + maxTurnRate);
		}
		
		
		facing += turnRate * amount;
		facing = Misc.normalizeAngle(facing);
		
		if (diffWithCurrFacing < turnRate * amount * 1.5f) {
			facing = desiredFacing;
		}
		
	}
	

	public float getFacing() {
		return facing;
	}

	public void setFacing(float facing) {
		this.facing = facing;
	}


	public float getTurnAcceleration() {
		return turnAcceleration;
	}

	public void setTurnAcceleration(float turnAcceleration) {
		this.turnAcceleration = turnAcceleration;
	}

	public float getMaxTurnRate() {
		return maxTurnRate;
	}

	public void setMaxTurnRate(float maxTurnRate) {
		this.maxTurnRate = maxTurnRate;
	}

	public float getTurnRate() {
		return turnRate;
	}

	public void setTurnRate(float turnRate) {
		this.turnRate = turnRate;
	}

	
}
