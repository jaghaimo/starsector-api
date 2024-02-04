package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class HAKazeronTacBombardmentFactor extends BaseOneTimeFactor {
	
	public HAKazeronTacBombardmentFactor(int points) {
		super(points);
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Tactical bombardment of Kazeron";
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("A tactical bombardment of Kazeron - the nerve center and military hub of the Persean "
						+ "League - has severely disrupted its efforts to bring pressure on your colonies.",
						0f);
			}
			
		};
	}
	
}
