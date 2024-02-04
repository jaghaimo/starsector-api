package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HAHersperusTacBombardmentFactor extends BaseOneTimeFactor {
	
	public HAHersperusTacBombardmentFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Tactical bombardment of Hesperus";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A tactical bombardment of Hersperus - the military stronghold of the Knights of Ludd - "
						+ "has severely disrupted the efforts of the Luddic Church to pressure your colonies.",
						0f);
			}
			
		};
	}
	
}
