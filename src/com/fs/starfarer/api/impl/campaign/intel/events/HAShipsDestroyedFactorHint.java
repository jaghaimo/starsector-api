package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HAShipsDestroyedFactorHint extends HAShipsDestroyedFactor {
	
	public HAShipsDestroyedFactorHint() {
		super(0);
		timestamp = 0; // makes it not expire
	}

	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return !hasOtherFactorsOfClass(intel, HAShipsDestroyedFactor.class);
//		for (EventFactor factor : intel.getFactors()) {
//			if (factor != this && factor instanceof HAShipsDestroyedFactor) {
//				return false;
//			}
//		}
//		return true;
	}
	
	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("Destroying hostile ships in or near one of the star systems "
						+ "with your colonies will reduce event progress.",
						0f);
			}
			
		};
	}

//	@Override
//	public String getProgressStr(BaseEventIntel intel) {
//		return "";
//	}
//
//	@Override
//	public Color getDescColor(BaseEventIntel intel) {
//		return Misc.getGrayColor();
//	}

}
