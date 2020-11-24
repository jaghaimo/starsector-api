package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

public interface AssignmentTargetAPI {
	Vector2f getLocation();
	Vector2f getVelocity();
	
	/**
	 * @return 0 for player, 1 for enemy, 100 for neutral.
	 */
	int getOwner();
}
