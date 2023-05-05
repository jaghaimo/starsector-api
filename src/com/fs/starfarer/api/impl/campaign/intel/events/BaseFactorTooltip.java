package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class BaseFactorTooltip implements TooltipCreator {

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}

	public float getTooltipWidth(Object tooltipParam) {
		return BaseEventFactor.TOOLTIP_WIDTH;
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		
	}

}
