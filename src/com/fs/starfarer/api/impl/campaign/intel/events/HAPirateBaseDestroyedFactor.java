package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HAPirateBaseDestroyedFactor extends BaseOneTimeFactor {
	
	public HAPirateBaseDestroyedFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Pirate base destroyed";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A pirate base that was supporting pirate activity at your colonies was destroyed by "
						+ "your fleet.",
						0f);
			}
			
		};
	}
	
}
