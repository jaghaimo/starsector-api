package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public interface HostileActivityPlugin {

	String getId();
	
	TooltipCreator getTooltip();
	
	String getName();
	Color getNameColor(float mag);
	
	
	/**
	 * What fraction of maximum stability/accessibility/etc penalty this contributes. A value of 1 would max
	 * out the penalty just from that one type of hostile activity.
	 * @return
	 */
	float getEffectMagnitude();
	
	boolean showInIntelEvenIfZeroMagnitude();
	
	float getSpawnFrequency();
	
	float getSpawnInHyperProbability();
	float getStayInHyperProbability();
	
	int getMaxNumFleets();
	
	CampaignFleetAPI createFleet(StarSystemAPI system, Random random);
	
	
	void addCause(HostileActivityCause cause);
	List<HostileActivityCause> getCauses();

	HostileActivityCause getCauseOfClass(Class c);
	void removeCauseOfClass(Class c);

	float getEffectMagnitudeAdjustedBySuppression();
}
