package com.fs.starfarer.api.combat;

import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

public interface CombatEntityAPI {
	Vector2f getLocation();
	Vector2f getVelocity();
	float getFacing();
	void setFacing(float facing);
	float getAngularVelocity();
	void setAngularVelocity(float angVel);
	
	/**
	 * 0 = player
	 * 1 = enemy
	 * 100 = neutral (used for ship hulks)
	 * @return
	 */
	int getOwner();
	/**
	 * 0 = player
	 * 1 = enemy
	 * 100 = neutral (used for ship hulks)
	 * @return
	 */
	void setOwner(int owner);
	
	float getCollisionRadius();
	
	CollisionClass getCollisionClass();
	void setCollisionClass(CollisionClass collisionClass);

	float getMass();
	void setMass(float mass);
	
	/**
	 * Can return null if there aren't any bounds, in which case just the collision radius should be used.
	 * The bounds are guaranteed to be inside the collision radius.
	 * @return
	 */
	BoundsAPI getExactBounds();
	
	/**
	 * Returns null for entities without shields.
	 * @return
	 */
	ShieldAPI getShield();
	
	/**
	 * @return hull level, normalized to (0, 1)
	 */
	float getHullLevel();
	
	/**
	 * @return actual hull points left
	 */
	float getHitpoints();
	
	/**
	 * @return maximum hull points for the ship
	 */
	float getMaxHitpoints();
	
	
	/**
	 * Should always circumscribe the bounds, if any.
	 * @param radius
	 */
	void setCollisionRadius(float radius);
	
	
	Object getAI();
	boolean isExpired();
	void setCustomData(String key, Object data);
	void removeCustomData(String key);
	
	/**
	 * DO NOT call .put() methods on the returned map. Use setCustomData() instead.
	 * When the map is empty, it will return a new non-null map that will not be retained,
	 * so any additions to it would be lost. 
	 * 
	 * @return
	 */
	Map<String, Object> getCustomData();
	void setHitpoints(float hitpoints);
	boolean isPointInBounds(Vector2f p);
	boolean wasRemoved();
	
}


