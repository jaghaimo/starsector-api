package com.fs.starfarer.api.combat;


public interface ShipAIPlugin {
	/**
	 * Advise the AI not to fire for amount seconds.
	 * Used when fighters are taking off from a carrier to prevent bomb/torpedo friendly fire. 
	 * @param amount
	 */
	void setDoNotFireDelay(float amount);
	
	/**
	 * When this is called, the AI should immediately evaluate nearby threats and such,
	 * if it only does it periodically otherwise.
	 * 
	 * Called when the autopilot is toggled on.
	 */
	void forceCircumstanceEvaluation();
	
	
	/**
	 * The AI should do its main work here.
	 * @param amount
	 */
	void advance(float amount);
	
	
	/**
	 * Only called for fighters, not regular ships or drones.
	 * @return whether the fighter needs refit
	 */
	boolean needsRefit();
	
	ShipwideAIFlags getAIFlags();

	void cancelCurrentManeuver();

	ShipAIConfig getConfig();
}
