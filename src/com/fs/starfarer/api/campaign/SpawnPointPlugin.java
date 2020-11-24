package com.fs.starfarer.api.campaign;

/**
 * Use com.fs.starfarer.api.EveryFrameScript instead.
 * 
 * Or, if you need to use a spawnpoint, extend BaseSpawnPoint.
 * 
 * @author Alex Mosolov
 * 
 * Copyright 2012 Fractal Softworks, LLC
 */
@Deprecated
public interface SpawnPointPlugin {
	void advance(SectorAPI sector, LocationAPI location);
}
