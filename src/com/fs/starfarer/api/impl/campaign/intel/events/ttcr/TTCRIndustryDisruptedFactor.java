package com.fs.starfarer.api.impl.campaign.intel.events.ttcr;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public class TTCRIndustryDisruptedFactor extends BaseOneTimeFactor {
	
	protected String desc;

	public TTCRIndustryDisruptedFactor(String desc, int points) {
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
				tooltip.addPara("Disrupted non-military industrial operations "
						+ "on Tri-Tachyon colonies, through raids or bombardment. More effective and longer lasting"
						+ " disruptions result in more event progress points.",
						0f);
				tooltip.addPara("Repeatedly disrupting the same industry will have no additional effect.",
						0f);
			}
			
		};
	}
	
}
