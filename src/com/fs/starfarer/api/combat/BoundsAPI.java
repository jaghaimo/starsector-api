package com.fs.starfarer.api.combat;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

/**
 * The bounds HAVE to be a polygon - can't just be a disconnected set of segments.
 * 
 * The polygon can be either concave or convex.
 * 
 * 
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface BoundsAPI {
	public interface SegmentAPI {
		Vector2f getP1();
		Vector2f getP2();
		void set(float x1, float y1, float x2, float y2);
	}
	
	/**
	 * Updates the coordinates of the bounds to reflect the specified location and facing.
	 */
	void update(Vector2f location, float facing);
	
	List<SegmentAPI> getSegments();
	
	
	/**
	 * Remove all segments.
	 */
	void clear();
	
	/**
	 * Add a new segment. Coordinates are relative to entity center, with entity facing 0 degrees (i.e., to the right).
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	void addSegment(float x1, float y1, float x2, float y2);
	
	/**
	 * Adds a segment using the end of the previously added segment as the starting point.
	 * @param x2
	 * @param y2
	 */
	void addSegment(float x2, float y2);

	List<SegmentAPI> getOrigSegments();
}
