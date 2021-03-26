package com.fs.starfarer.api.combat;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public enum CollisionClass {
	NONE,
	RAY,
	RAY_FIGHTER,
	FIGHTER,
	SHIP,
	PROJECTILE_NO_FF,
	PROJECTILE_FF,
	MISSILE_NO_FF,
	MISSILE_FF,
	HITS_SHIPS_AND_ASTEROIDS,
	HITS_SHIPS_ONLY_FF,
	HITS_SHIPS_ONLY_NO_FF,
	PROJECTILE_FIGHTER,
	ASTEROID,
	PLANET,
	GAS_CLOUD,
	STAR,
	;
	
}
