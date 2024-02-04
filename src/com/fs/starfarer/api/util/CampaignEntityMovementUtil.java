package com.fs.starfarer.api.util;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;

public class CampaignEntityMovementUtil {
	
	public static interface EngineGlowControls {
		void showAccelerating();
		void showIdling();
		void showSuppressed();
		void showOtherAction();
	}

	public static float DIRECTION_UNSET = Float.MAX_VALUE;
	
	protected SectorEntityToken entity;
	protected SmoothFacingUtil facingUtil;
	protected SmoothMovementUtil movementUtil;
	protected boolean turnThenAccelerate = true;
	protected boolean faceInOppositeDirection = false;
	protected float moveDir;
	protected float desiredFacing = DIRECTION_UNSET;
	protected Vector2f moveDest;
	protected EngineGlowControls engineGlow;
	
	public CampaignEntityMovementUtil(SectorEntityToken entity,
				float turnAccel, float maxTurnRate, float accel, float maxSpeed) {
		this.entity = entity;
		
		facingUtil = new SmoothFacingUtil(turnAccel, maxTurnRate);
		movementUtil = new SmoothMovementUtil();
		
		movementUtil.setAcceleration(accel);
		movementUtil.setMaxSpeed(maxSpeed);
		
		moveDir = DIRECTION_UNSET;
		moveDest = null;
	}
	
	public boolean isFaceInOppositeDirection() {
		return faceInOppositeDirection;
	}

	public void setFaceInOppositeDirection(boolean faceInOppositeDirection) {
		this.faceInOppositeDirection = faceInOppositeDirection;
	}

	public boolean isTurnThenAccelerate() {
		return turnThenAccelerate;
	}

	public void setTurnThenAccelerate(boolean turnThenAccelerate) {
		this.turnThenAccelerate = turnThenAccelerate;
	}

	public SmoothFacingUtil getFacingUtil() {
		return facingUtil;
	}

	public SmoothMovementUtil getMovementUtil() {
		return movementUtil;
	}
	
	public void moveInDirection(float dir) {
		moveDir = dir;
		moveDest = null;
		leaveOrbit();
	}
	public void moveToLocation(Vector2f loc) {
		moveDir = DIRECTION_UNSET;
		moveDest = loc;
		leaveOrbit();
	}
	
	public void stop() {
		moveDir = DIRECTION_UNSET;
		moveDest = new Vector2f(entity.getLocation());
		leaveOrbit();
	}
	
	public void leaveOrbit() {
		if (entity.getOrbit() != null) {
			setFacing(entity.getFacing());
			setLocation(entity.getLocation());
			setVelocity(entity.getVelocity());
			entity.setOrbit(null);
		}
	}
	
	protected Vector2f getPointInDirectionOppositeToVelocity() {
		Vector2f p = new Vector2f(entity.getLocation());
		if (entity.getVelocity().length() > 0) {
			Vector2f back = new Vector2f(entity.getVelocity());
			Misc.normalise(back);
			back.negate();
			back.scale(10f);
			Vector2f.add(back, entity.getLocation(), back);
			p = back;
		}
		return p;
	}
	
	public void advance(float amount) {
		//movementUtil.setAcceleration(20f);
		if (entity.getOrbit() == null) {
			Vector2f dest;
			
			if (moveDir != DIRECTION_UNSET) {
				dest = Misc.getUnitVectorAtDegreeAngle(moveDir);
				dest.scale(100000000f);
				Vector2f.add(dest, entity.getLocation(), dest);
				desiredFacing = moveDir;
			} else if (moveDest != null) {
				desiredFacing = Misc.getAngleInDegrees(entity.getLocation(), moveDest);
				dest = moveDest;
			} else {
				desiredFacing = entity.getFacing();
				dest = getPointInDirectionOppositeToVelocity();
			}
			
			if (faceInOppositeDirection) desiredFacing += 180f;
			
			float angleDiff = Misc.getAngleDiff(entity.getFacing(), desiredFacing);
			boolean turnOnly = turnThenAccelerate && angleDiff > 2f;
			if (turnOnly) {
				dest = getPointInDirectionOppositeToVelocity();
			}
			
			movementUtil.setDest(dest, new Vector2f());
			movementUtil.advance(amount);
			facingUtil.advance(desiredFacing, amount);
		
			entity.setFacing(facingUtil.getFacing());
			entity.getLocation().set(movementUtil.getLocation());
			entity.getVelocity().set(movementUtil.getVelocity());
			
			if (engineGlow != null) {
				if (turnOnly || angleDiff > 30f) {
					engineGlow.showOtherAction();
				} else {
					engineGlow.showAccelerating();
				}
			}
			
		} else {
			if (engineGlow != null) {
				engineGlow.showIdling();
			}
			desiredFacing = DIRECTION_UNSET;
		}
	}

	public boolean isDesiredFacingSet() {
		return desiredFacing != DIRECTION_UNSET;
	}
	
	public float getDesiredFacing() {
		return desiredFacing;
	}


	public SectorEntityToken getEntity() {
		return entity;
	}
	
	public void setFacing(float facing) {
		facingUtil.setFacing(facing);
		entity.setFacing(facing);
	}
	
	public void setLocation(Vector2f loc) {
		movementUtil.getLocation().set(loc);
		entity.getLocation().set(movementUtil.getLocation());
	}
	public void setVelocity(Vector2f vel) {
		movementUtil.getVelocity().set(vel);
		entity.getVelocity().set(movementUtil.getVelocity());
	}

	public EngineGlowControls getEngineGlow() {
		return engineGlow;
	}

	public void setEngineGlow(EngineGlowControls engineGlow) {
		this.engineGlow = engineGlow;
	}
	
}







