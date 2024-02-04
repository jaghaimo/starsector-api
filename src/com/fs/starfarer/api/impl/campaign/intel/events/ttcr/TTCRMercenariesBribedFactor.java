package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class TTCRMercenariesBribedFactor extends BaseOneTimeFactor {
	
	public TTCRMercenariesBribedFactor() {
		super(TTCRPoints.MERCS_BRIBED);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Bribed mercenaries sent to attack you";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A mercenary company was sent to disrupt your industrial base, but you've successfully "
						+ "bribed them to attack a Tri-Tachyon system instead.",
						0f);
			}
			
		};
	}
	
}
