package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class TTCRCommerceRaidersDestroyedFactor extends BaseOneTimeFactor {
	
	public TTCRCommerceRaidersDestroyedFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Commerce raiders destroyed";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Ships belonging to commerce raider fleets sent to harass your colonies "
						+ "by the Tri-Tachyon Corporation, destroyed by your fleet.",
						0f);
			}
			
		};
	}
	
}
