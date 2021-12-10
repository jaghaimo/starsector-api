package com.fs.starfarer.api.impl.campaign.enc;

public interface EPEncounterCreator {

	String getId();
	float getPointTimeoutMin();
	float getPointTimeoutMax();
	float getCreatorTimeoutMin();
	float getCreatorTimeoutMax();
	
	float getFrequencyForPoint(EncounterManager manager, EncounterPoint point);
	void createEncounter(EncounterManager manager, EncounterPoint point);
}
