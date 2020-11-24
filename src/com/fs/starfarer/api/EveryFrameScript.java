package com.fs.starfarer.api;

public interface EveryFrameScript {
	/**
	 * @return true when the script is finished and can be cleaned up by the engine.
	 */
	boolean isDone();
	
	/**
	 * @return whether advance() should be called while the campaign engine is paused.
	 */
	boolean runWhilePaused();
	
	/**
	 * Use SectorAPI.getClock() to convert to campaign days.
	 * @param amount seconds elapsed during the last frame.
	 */
	void advance(float amount);
}
