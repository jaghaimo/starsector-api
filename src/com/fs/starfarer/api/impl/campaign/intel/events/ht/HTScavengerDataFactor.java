package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HTScavengerDataFactor extends BaseOneTimeFactor {
	
	public HTScavengerDataFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Bought topographic data";
	}

	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Topographic data and sensor readings bought from a fellow traveller, "
						+ "far out in the black.",
						0f);
			}
			
		};
	}
	
}
