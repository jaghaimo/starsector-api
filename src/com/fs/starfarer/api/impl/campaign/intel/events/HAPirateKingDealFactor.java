package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HAPirateKingDealFactor extends BaseOneTimeFactor {
	
	public HAPirateKingDealFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Deal made with pirate king";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("You've recently made a deal with a pirate king in control of a nearby station.",
						0f);
			}
			
		};
	}
	
}
