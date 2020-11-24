/**
 * 
 */
package com.fs.starfarer.api.impl.campaign.terrain;

import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface AsteroidSource {
	/**
	 * Report that the game engine decided to persist the given asteroid,
	 * meaning it doesn't need to be re-generated on game load etc.
	 * @param asteroid
	 */
	void reportAsteroidPersisted(SectorEntityToken asteroid);
	
	
	/**
	 * Called after game load to regenerate asteroids that haven't been persisted.
	 */
	void regenerateAsteroids();
}