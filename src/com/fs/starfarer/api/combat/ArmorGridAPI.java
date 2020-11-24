package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;


public interface ArmorGridAPI {
	/**
	 * @return Armor value that the player sees in the game, on tooltips and such.
	 */
	float getArmorRating();
	
	/**
	 * @return Actual per-cell maximum armor value. ~1/15th of the listed armor rating, due to how damage is distributed between cells.
	 */
	float getMaxArmorInCell();
	
	/**
	 * 0,0 is lower left corner of the sprite.
	 * @return Armor value normalized to (0, 1).
	 */
	float getArmorFraction(int cellX, int cellY);
	
	/**
	 * 0,0 is lower left corner of the sprite.
	 * @return Actual non-normalized armor value in cell.
	 */
	float getArmorValue(int cellX, int cellY);
	
	/**
	 * @param cellX
	 * @param cellY
	 * @param value actual value, NOT fraction.
	 */
	void setArmorValue(int cellX, int cellY, float value);
	float[][] getGrid();
	
	/**
	 * Armor cell size, in pixels.
	 */
	float getCellSize();
	
	
	/**
	 * Number of cells above the center of the ship.
	 * 
	 * Use together with getCellSize() and ShipAPI.getLocation() to determine the cell at a given location.
	 * @return
	 */
	int getAbove();
	
	/**
	 * Number of cells below the center of the ship.
	 * 
	 * Use together with getCellSize() and ShipAPI.getLocation() to determine the cell at a given location.
	 * @return
	 */
	int getBelow();
	
	/**
	 * Number of cells right of the center of the ship.
	 * 
	 * Use together with getCellSize() and ShipAPI.getLocation() to determine the cell at a given location.
	 * @return
	 */
	int getRightOf();
	
	/**
	 * Number of cells left of the center of the ship.
	 * 
	 * Use together with getCellSize() and ShipAPI.getLocation() to determine the cell at a given location.
	 * @return
	 */
	int getLeftOf();
	
	/**
	 * @param loc absolute location in engine coordinates.
	 * @return null if loc is off the grid, array with {int cellX, int cellY} otherwise.
	 */
	int [] getCellAtLocation(Vector2f loc);

	Vector2f getLocation(int cellX, int cellY);
}







