package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HTTopographicDataFactor extends BaseOneTimeFactor {
	
	public HTTopographicDataFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Topographic data found";
	}

	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Recovered topographic data. Possibly out of date, but still valuable.",
						0f);
			}
			
		};
	}
	
}
