package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

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
public interface HostileActivityCause {
	void addDescToTooltip(TooltipMakerAPI tooltip, float pad);
	float getMagnitudeContribution();
}
