package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class HTAbyssalLightFactor extends BaseOneTimeFactor {
	
	protected boolean multiple;

	public HTAbyssalLightFactor(int points, boolean multiple) {
		super(points);
		this.multiple = multiple;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Disrupted abyssal light" + (multiple ? "s" : "");
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Disrupting an %s can provide insight into the "
						+ "topography of surrounding abyssal hyperspace.",
						0f, Misc.getHighlightColor(), "Abyssal Light");
			}
			
		};
	}
	
}
