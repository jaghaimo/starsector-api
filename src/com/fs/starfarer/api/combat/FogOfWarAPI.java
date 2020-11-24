package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface FogOfWarAPI {

	public int getPlayerId();
	public void revealAroundPoint(Object source, float x, float y, float radius);
	
	public boolean isVisible(Vector2f loc);
	public boolean isVisible(CombatEntityAPI entity);
	public boolean isVisible(float x, float y);
}
