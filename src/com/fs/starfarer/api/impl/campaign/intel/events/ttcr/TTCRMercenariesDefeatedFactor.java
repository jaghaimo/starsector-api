package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class TTCRMercenariesDefeatedFactor extends BaseOneTimeFactor {
	
	public TTCRMercenariesDefeatedFactor() {
		super(TTCRPoints.MERCS_DEFEATED);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Defeated mercenaries sent to attack you";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A mercenary company was sent to disrupt your industrial base, but you've successfully "
						+ "defeated the attack.",
						0f);
			}
			
		};
	}
	
}
