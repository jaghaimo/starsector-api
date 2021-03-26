package com.fs.starfarer.api.combat;

import java.util.Iterator;

import org.lwjgl.util.vector.Vector2f;

/**
 * Bin-lattice with combat entities - ships, missiles, asteroids, projectiles, beams, etc -
 * sorted into buckets based on their location. May only have a subset of these objects in it,
 * depending on this grid's role.
 * 
 * Used to fulfill "all objects in area" queries without having to iterate through every object.
 * 
 * Occasionally just getting a list of the entities - i.e. via CombatEngineAPI.getShips() may be a bit faster than using the grid.
 * In particular, this will more often be the case when using the grid for a large area. For something very performance
 * intensive, it makes sense to try each way to see which is faster in a given situation.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public interface CollisionGridAPI {

	/**
	 * Adds the object to every bucket that the area overlaps.
	 * @param object
	 * @param loc
	 * @param objWidth
	 * @param objHeight
	 */
	void addObject(Object object, Vector2f loc, float objWidth, float objHeight);
	
	/**
	 * Removes the object from every bucket that the area overlaps.
	 * @param object
	 * @param loc
	 * @param objWidth
	 * @param objHeight
	 */
	void removeObject(Object object, Vector2f loc, float objWidth, float objHeight);
	
	/**
	 * Returns an iterator for all the objects in this grid that are in the specified area.
	 * @param loc
	 * @param checkWidth
	 * @param checkHeight
	 * @return
	 */
	Iterator<Object> getCheckIterator(Vector2f loc, float checkWidth, float checkHeight);

}




