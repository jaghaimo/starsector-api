package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

/**
 * Unused; replaced with Active Sensor Burst scanning these types of things, and with HTScanFactor.
 * 
 */
@Deprecated public class HTBlackHoleFactor extends BaseOneTimeFactor {
	
	public HTBlackHoleFactor() {
		super(HTPoints.SCAN_BLACK_HOLE_SHORT_RANGE);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Black hole scanned";
	}

	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A close-range scan of a black hole provides insight into the topography of "
						+ "the surrounding area in hyperspace.",
						0f);
			}
			
		};
	}
	
}
