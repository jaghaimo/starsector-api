package com.fs.starfarer.api.impl.campaign.fleets.misc;

import java.util.Random;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.fleets.misc.MiscFleetRouteManager.MiscRouteData;

public class BaseMiscFleetCreatorPlugin implements MiscFleetCreatorPlugin {

	public String getId() {
		return getClass().getSimpleName();
	}
	
	
	public float getFrequency() {
		return 10f;
	}

	public void reportBattleOccurred(MiscFleetRouteManager manager, CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}

	public void reportFleetDespawnedToListener(MiscFleetRouteManager manager, CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		
	}

	public MiscRouteData createRouteParams(MiscFleetRouteManager manager, Random random) {
		// TODO Auto-generated method stub
		return null;
	}


	public CampaignFleetAPI createFleet(MiscFleetRouteManager manager, RouteData route, Random random) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getStartingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return null;
	}

	public String getEndingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return null;
	}

	public String getTravelToDestActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return null;
	}

	public String getTravelReturnActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return null;
	}

	public String getAtDestUnloadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return null;
	}

	public String getAtDestLoadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return null;
	}


	public int getMaxFleetsForThisCreator() {
		return 10000;
	}

}
