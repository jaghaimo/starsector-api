package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class HTHighBurnFactor extends BaseOneTimeFactor {
	
	public HTHighBurnFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "High burn sensor readings";
	}

	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Sensor readings gathered in hyperspace while traveling at a burn level above %s "
						+ "are particularly useful in providing insight into the non-Euclidian properties of hyperspace. "
						+ "Traveling at higher speeds produces even more valuable readings.",
						0f, Misc.getHighlightColor(), "" + 20);
			}
			
		};
	}
	
}
