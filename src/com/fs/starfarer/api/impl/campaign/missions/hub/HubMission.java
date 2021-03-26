package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;

public interface HubMission {
	String getTriggerPrefix();
	String getBlurbText();
	
	String getMissionId();
	void setMissionId(String missionId);
	
	void setGenRandom(Random random);
	
	void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	void updateInteractionData(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	
	//void create(boolean barEvent);
	void createAndAbortIfFailed(MarketAPI market, boolean barEvent);
	void abort();
	
	boolean isMissionCreationAborted();
	
	MissionHub getHub();
	HubMissionCreator getCreator();
	void setCreator(HubMissionCreator creator);
	void setHub(MissionHub hub);
	
	PersonAPI getPerson();
	PersonAPI getPersonOverride();
	void setPersonOverride(PersonAPI personOverride);
	
//	float getRepRewardSuccess();
//	float getRepPenaltyFailure();
	
}
