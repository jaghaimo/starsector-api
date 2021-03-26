package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.Random;

import com.fs.starfarer.api.loading.PersonMissionSpec;


public interface HubMissionCreator {
	HubMission createHubMission(MissionHub hub);
	
	void incrCompleted();
	int getNumCompleted();
	void setNumCompleted(int numCompleted);
	
	void incrFailed();
	int getNumFailed();
	void setNumFailed(int numFailed);
	
	float getFrequencyWeight();
	float getWasShownTimeoutDuration();
	float getAcceptedTimeoutDuration();
	float getCompletedTimeoutDuration();
	float getFailedTimeoutDuration();
	
	boolean isPriority();
	//void updateSeed();
	void updateRandom();
	Random getGenRandom();

	//float getRequiredRep();
	boolean matchesRep(float rep);

	String getSpecId();

	boolean wasAutoAdded();
	void setWasAutoAdded(boolean wasAutoAdded);

	boolean isActive();
	void setActive(boolean isActive);

	PersonMissionSpec getSpec();

	void setSeed(long seed);

}
