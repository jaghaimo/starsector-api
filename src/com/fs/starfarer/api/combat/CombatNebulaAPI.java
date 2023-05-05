package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

/**
 * Covers the map plus a 100 pixel area around it.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2016 Fractal Softworks, LLC
 */
public interface CombatNebulaAPI {

	public interface CloudAPI {
		void thicken(float thicknessGain);
		Vector2f getLocation();
		float getThickness();
		void setThickness(float thickness);
		
	}
	
	int getTilesWide();
	int getTilesHigh();
	
	float getTileSizeInPixels();
	boolean tileHasNebula(int cellX, int cellY);
	boolean locationHasNebula(float x, float y);
	
	/**
	 * Number of cells to the left of x = 0.
	 * @return
	 */
	int getLeftOf();
	
	
	/**
	 * Number of cells below y = 0.
	 * @return
	 */
	int getBelow();
	
	/**
	 * Number of cells to the right of x = 0.
	 * @return
	 */
	int getRightOf();
	
	/**
	 * Number of cells above y = 0.
	 * @return
	 */
	int getAbove();
	
	void setHasNebula(int cellX, int cellY, float brightness);
	CloudAPI getCloud(float x, float y);
	CloudAPI getCloud(int cellX, int cellY);
}
