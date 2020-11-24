package com.fs.starfarer.api;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface Script {
	
	/**
	 * An aribtrary script that can be run by the game engine. 
	 * 
	 * Should return quickly since it will be executed on the main thread and could otherwise hold up the game. 
	 */
	public void run();
}
