package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HAPatherBaseDestroyedFactor extends BaseOneTimeFactor {
	
	public HAPatherBaseDestroyedFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Luddic Path base destroyed";
	}

	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A Luddic Path base that was supporting Pather cells at your colonies was destroyed by "
						+ "your fleet.",
						0f);
			}
			
		};
	}
	
}
