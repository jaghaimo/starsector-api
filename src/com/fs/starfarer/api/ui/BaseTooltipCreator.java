package com.fs.starfarer.api.ui;

import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class BaseTooltipCreator implements TooltipCreator {
	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}

	public float getTooltipWidth(Object tooltipParam) {
		return 500;
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		
	}

}
