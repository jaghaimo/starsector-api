package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

/**
 * There can be multiple "causes" for the same type of hostile activity. For example,
 * there may be more pirates because:
 * 1) General pirate activity due to having a colony
 * 2) Free port attracting more of them
 * 3) A pirate base nearby
 * 
 * And so on.
 * 
 * 
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public interface HostileActivityCause2 {
	//void addDescToTooltip(TooltipMakerAPI tooltip, float pad);
	float getMagnitudeContribution(StarSystemAPI system);
	
	/**
	 * Progress is used to advance the event, MagnitudeContribution is for fleet spawning for... legacy reasons.
	 * May refactor this later.
	 * @return
	 */
	int getProgress();
	
	boolean shouldShow();
	String getDesc();
	String getProgressStr();
	Color getDescColor(BaseEventIntel intel);
	Color getProgressColor(BaseEventIntel intel);
	TooltipCreator getTooltip();

	void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel);
}




