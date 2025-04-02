package com.fs.starfarer.api.impl.campaign.enc;

/**
 * Implementations should generally avoid using member variables since instances will be shared across
 * saves.
 * @author Alex
 *
 */
public interface EPEncounterCreator {

	String getId();
	float getPointTimeoutMin();
	float getPointTimeoutMax();
	float getCreatorTimeoutMin();
	float getCreatorTimeoutMax();
	
	float getFrequencyForPoint(EncounterManager manager, EncounterPoint point);
	void createEncounter(EncounterManager manager, EncounterPoint point);
}
