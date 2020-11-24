package com.fs.starfarer.api;

public interface EveryFrameScriptWithCleanup extends EveryFrameScript {
	
	/**
	 * Called when an entity that has this script attached to it is removed from the campaign engine. 
	 */
	void cleanup();
}
