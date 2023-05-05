package com.fs.starfarer.api.impl.campaign.fleets.misc;

import java.util.Random;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.fleets.misc.MiscFleetRouteManager.MiscRouteData;

public interface MiscFleetCreatorPlugin {
	String getId();
	float getFrequency();
	
	int getMaxFleetsForThisCreator();
	
	MiscRouteData createRouteParams(MiscFleetRouteManager manager, Random random);
	CampaignFleetAPI createFleet(MiscFleetRouteManager manager, RouteData route, Random random);
	
	void reportBattleOccurred(MiscFleetRouteManager manager, CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle);
	void reportFleetDespawnedToListener(MiscFleetRouteManager manager, CampaignFleetAPI fleet, FleetDespawnReason reason, Object param);
	
	
	String getStartingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data);
	String getEndingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data);
	String getTravelToDestActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data);
	String getTravelReturnActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data);
	
	String getAtDestUnloadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data);
	String getAtDestLoadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data);
}
