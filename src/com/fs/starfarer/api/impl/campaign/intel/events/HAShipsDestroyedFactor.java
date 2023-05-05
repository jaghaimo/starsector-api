package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HAShipsDestroyedFactor extends BaseOneTimeFactor {
	
	public HAShipsDestroyedFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Hostile ships destroyed";
	}

	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Hostile ships destroyed by your fleet in or near one of the star systems "
						+ "with your colonies.",
						0f);
			}
			
		};
	}
	
}
