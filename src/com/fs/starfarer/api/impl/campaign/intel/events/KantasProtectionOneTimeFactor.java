package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class KantasProtectionOneTimeFactor extends BaseOneTimeFactor {
	
	public KantasProtectionOneTimeFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Kanta's protection gained";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		return super.getDescColor(intel);
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("You've recently gained Kanta's protection, and it's had a chilling effect on "
						+ "hostile activity in and around your systems.",
						0f);
			}
			
		};
	}

}
