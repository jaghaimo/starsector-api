package com.fs.starfarer.api.campaign.events;


/**
 * 
 * Probability = 1 means event is guaranteed to occur next time it's checked for.
 * 
 * If an event occurs, the probability is reset to 0.
 * If an event fails to occur, the probability is multiplied by 0.5
 * 
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */

public interface EventProbabilityAPI {
	
	
	float getProbability();
	void setProbability(float probability);
	String getEventType();
	CampaignEventTarget getEventTarget();
	void increaseProbability(float incr);
	void decreaseProbability(float decr);
	CampaignEventPlugin getPlugin();
	
	/**
	 * probabilityMult from events.json.
	 * @return
	 */
	float getMult();
	void setProbabilityAfterMult(float p);
	boolean isPrimed();
	void setPrimed(boolean primed);
	
}
