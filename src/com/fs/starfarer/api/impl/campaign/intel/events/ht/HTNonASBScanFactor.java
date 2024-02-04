package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class HTNonASBScanFactor extends BaseOneTimeFactor {
	
	protected String desc;

	public HTNonASBScanFactor(String desc, int points) {
		super(points);
		this.desc = desc;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return desc;
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A scan of the right entity or location can provide insight into the "
						+ "topography of surrounding hyperspace. The target usually needs to be "
						+ "massive enough to produce a significant gravity well, produce a high energy "
						+ "discharge, or be exotic or unusual in some other way.",
						0f, Misc.getHighlightColor());
			}
			
		};
	}
	
}
