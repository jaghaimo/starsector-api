package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class KantasProtectionPirateActivityCause2 extends BaseHostileActivityCause2 {

	public KantasProtectionPirateActivityCause2(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	@Override
	public boolean shouldShow() {
		return KantaCMD.playerHasProtection();
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		return Misc.getPositiveHighlightColor();
	}
	
	@Override
	public String getProgressStr() {
		return "";
	}

	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara("You have %s, which is enough to dissuade most pirates from attacking your interests.",
						0f, Misc.getPositiveHighlightColor(), "Kanta's protection");
			}
		};
	}

	public int getProgress() {
		return 0;
	}
	
	public String getDesc() {
		return "Kanta's protection";
	}	

	public float getMagnitudeContribution(StarSystemAPI system) {
		return 0f;
	}

}
