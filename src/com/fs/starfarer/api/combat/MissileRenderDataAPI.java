package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.graphics.SpriteAPI;

public interface MissileRenderDataAPI {
	SpriteAPI getSprite();
	float getBrightness();
	
	/**
	 * Absolute engine coordinates, NOT relative to ship location.
	 * @return
	 */
	Vector2f getMissileCenterLocation();
	
	float getMissileFacing();
	
	String getMissileSpecId();
}
