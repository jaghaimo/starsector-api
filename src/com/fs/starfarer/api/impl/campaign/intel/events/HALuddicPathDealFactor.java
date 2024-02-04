package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HALuddicPathDealFactor extends BaseOneTimeFactor {
	
	public HALuddicPathDealFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Deal made with Luddic Path";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("You've recently reached an understanding with the Luddic Path.",
						0f);
			}
			
		};
	}
	
}
