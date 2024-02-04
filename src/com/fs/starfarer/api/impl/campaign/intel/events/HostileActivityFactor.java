package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public interface HostileActivityFactor {

	String getId();
	
	String getNameForThreatList(boolean first);
	Color getNameColorForThreatList();
	
	float getEffectMagnitude(StarSystemAPI system);
	int getMaxNumFleets(StarSystemAPI system);
	float getStayInHyperProbability(StarSystemAPI system);
	float getSpawnInHyperProbability(StarSystemAPI system);
	float getSpawnFrequency(StarSystemAPI system);
	
	CampaignFleetAPI createFleet(StarSystemAPI system, Random random);

	void setRandomizedStageSeed(long seed);
	float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage);
	void rollEvent(HostileActivityEventIntel intel, EventStageData stage);
	void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, 
							    TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, 
			   					Color tc, float initPad);

	void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info);

	String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage);
	String getEventStageSound(HAERandomEventData data);
	
	TooltipCreator getStageTooltipImpl(HostileActivityEventIntel intel, EventStageData stage);

	void resetEvent(HostileActivityEventIntel intel, EventStageData stage);

	void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad);

	boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage);
}
