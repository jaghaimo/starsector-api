package com.fs.starfarer.api.campaign;

import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.campaign.econ.MarketAPI;


public interface MissionBoardAPI {
	public static interface MissionAvailabilityAPI {
		Set<SectorEntityToken> getAvailableAt();
		CampaignMissionPlugin getMission();
	}

	List<MissionAvailabilityAPI> getMissionsCopy();

	MissionAvailabilityAPI getAvailabilityForMission(String id);

	
	void makeAvailableAt(CampaignMissionPlugin mission, SectorEntityToken loc);
	void makeUnavailableAt(CampaignMissionPlugin mission, SectorEntityToken loc);
	void makeAvailableAt(CampaignMissionPlugin mission, String entityId);
	void makeUnavailableAt(CampaignMissionPlugin mission, String entityId);

	int getNumMissions(Class<?> clazz);

	void makeAvailableAt(CampaignMissionPlugin mission, MarketAPI market);

	void removeMission(CampaignMissionPlugin mission, boolean withCleanup);
	void removeMission(String id, boolean withCleanup);
}
