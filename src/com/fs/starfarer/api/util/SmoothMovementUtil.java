package com.fs.starfarer.api.util;

import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class SmoothMovementUtil {

	protected Vector2f vel = new Vector2f();
	protected Vector2f loc = new Vector2f();
	protected Vector2f accel = new Vector2f();
	
	protected Vector2f dest = new Vector2f();
	protected Vector2f destVel = new Vector2f();
	
	protected float acceleration, maxSpeed;
	protected boolean smoothCap = false;

	protected float hardSpeedLimit = -1f;
	
	public float getHardSpeedLimit() {
		return hardSpeedLimit;
	}

	public void setHardSpeedLimit(float hardSpeedLimit) {
		this.hardSpeedLimit = hardSpeedLimit;
	}

	public SmoothMovementUtil() {
		acceleration = 1f;
		maxSpeed = 1f;
		smoothCap = true;
	}

	public void setDest(Vector2f dest, Vector2f destVel) {
		if (dest == null) {
			this.dest.set(0, 0);
		} else {
			this.dest.set(dest);
		}
		if (destVel == null) {
			float dir = Misc.getAngleInDegrees(loc, this.dest);
			this.destVel = Misc.getUnitVectorAtDegreeAngle(dir);
			this.destVel.scale(10000f);
		} else {
			this.destVel.set(destVel);
		}
	}
	
	public void advance(float amount) {
		
		if (amount * amount == 0 || amount <= 0) {
			return;
		}
		
		float effectiveMaxSpeed = maxSpeed;
		float accelMult = 1f;
//		if (true && fleet != null) {
//			accelMult = 1f - Math.max(0, (fleet.getRadius() - 22f) / (100 - 22f));
//			accelMult = 0.25f + accelMult * 0.75f;
//		}
//		if (fleet != null && fleet.isPlayerFleet()) {
//			System.out.println("fwefwefe");
//		}
			
		float speed = vel.length();
		float acc = acceleration;
//		if (speed > effectiveMaxSpeed) {
//			acc = Math.max(speed, 200f) + fleet.getAcceleration();
//		}
		
		float effectiveAccel = acc * accelMult;
		
		if (effectiveAccel <= 0f) {
			accel.set(0, 0);
			return;
		}
		//amount *= 20f;
		
		Vector2f toDest = Vector2f.sub(dest, loc, new Vector2f());
		Vector2f velDiff = Vector2f.sub(destVel, vel, new Vector2f());
		
		toDest.scale(3f);
		
		//Vector2f dir = Vector2f.sub(dest, loc, new Vector2f());
		//Utils.normalise(dir);
		
		// positive means away from loc
		//float relativeSpeed = Vector2f.dot(dir, destVel);
		//float dist = Utils.getDistance(loc, dest);
//		if (delegate == CampaignEngine.getInstance().getPlayerFleet()) {
//			System.out.println("23rsd1sdfew");
//		}
		float timeToMatchVel = velDiff.length() / effectiveAccel;
		//toDest.scale(timeToMatchVel + 2f);
		velDiff.scale(timeToMatchVel + 0.75f);
		//velDiff.scale(0.5f * timeToMatchVel);
		
//		if (relativeSpeed > 0) {
//			velDiff.scale(dist / relativeSpeed);
//		} else {
//			velDiff.scale(timeToMatchVel);
//		}
		
		// this is the vector we want to negate (reduce to zero) in order to match
		// location and speed. Units are distance.
		Vector2f negate = (Vector2f) Vector2f.add(toDest, velDiff, new Vector2f()).negate();
		//if (negate.lengthSquared() < 0.25f) negate.set(0, 0);
		
//		if (delegate == CampaignEngine.getInstance().getPlayerFleet()) {
//			System.out.println(toDest);
//		}
		
		float maxAccel = negate.length() / (amount * amount);
		if (maxAccel > effectiveAccel) maxAccel = effectiveAccel;
		//maxAccel = 280f;
		//maxAccel = acceleration;
		if (maxAccel > 0) {
			accel = (Vector2f) Misc.normalise(negate).scale(-maxAccel);
		} else {
			accel = (Vector2f) negate.negate();
		}
//		accel = (Vector2f) Utils.normalise(negate).scale(-maxAccel);
//		if (delegate == CampaignEngine.getInstance().getPlayerFleet()) {
//			System.out.println("Max: " + maxAccel);
//		}
		
//		float speedPre = vel.length();
//		if (delegate == CampaignEngine.getInstance().getPlayerFleet()) {
//			System.out.println("Speed pre: " + vel.length());
//		}
		vel.x += accel.x * amount;
		vel.y += accel.y * amount;
		
		speed = vel.length();
//		float speedPost = vel.length();
//		if (delegate == CampaignEngine.getInstance().getPlayerFleet()) {
//			System.out.println("Speed post: " + vel.length());
//			System.out.println("");
//		}
//		if (delegate == CampaignEngine.getInstance().getPlayerFleet()) {
//			if (speedPre < speedPost) {
//				System.out.println(accel);
//			}
//		}
		
		//float speed = vel.length();
		if (speed >= effectiveMaxSpeed && speed > 0) {
			if (smoothCap) {
//				if (delegate == CampaignEngine.getInstance().getPlayerFleet()) {
//					System.out.println("23refwef");
//				}
				Vector2f cap = new Vector2f(vel);
				cap.negate();
				Misc.normalise(cap);
				//float mag = 1f;
				//if (maxAccel * 2f > 1) {
					//mag = maxAccel * 2f;
				//}
				//if (mag < 1000) mag = 1000;
				float mag = speed - effectiveMaxSpeed;
				if (mag < 50f) mag = 50f;
				
				float minMag = maxAccel * 2f;
				if (mag < minMag) mag = minMag;
				
				if (mag * amount > (speed - effectiveMaxSpeed) && amount > 0) {
					mag = (speed - effectiveMaxSpeed) / amount;
				}
				cap.scale(mag);
				vel.x += cap.x * amount;
				vel.y += cap.y * amount;
			} else {
				vel.scale(effectiveMaxSpeed / speed);
			}
		}
		
		if (hardSpeedLimit >= 0 && speed > 0) {
			vel.scale(hardSpeedLimit / speed);
		}
		
		loc.x += vel.x * amount;
		loc.y += vel.y * amount;
		
	}
	
	
	public ReadableVector2f getAccelVector() {
		return accel;
	}

	public float getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(float acceleration) {
		this.acceleration = acceleration;
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}
	
	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public Vector2f getVelocity() {
		return vel;
	}

	public Vector2f getLocation() {
		return loc;
	}

	public Vector2f getDest() {
		return dest;
	}
	
}




