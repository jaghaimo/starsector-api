package com.fs.starfarer.api.campaign;

import org.lwjgl.util.vector.Vector2f;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface OrbitAPI {
	SectorEntityToken getFocus();
	void advance(float amount);
	
	/**
	 * @param entity entity that's doing the orbiting.
	 */
	void setEntity(SectorEntityToken entity);
	
	
	OrbitAPI makeCopy();
	Vector2f computeCurrentLocation();
	float getOrbitalPeriod();

}
