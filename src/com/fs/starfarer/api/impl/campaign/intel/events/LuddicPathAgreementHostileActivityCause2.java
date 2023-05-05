package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class LuddicPathAgreementHostileActivityCause2 extends BaseHostileActivityCause2 {

	public LuddicPathAgreementHostileActivityCause2(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	@Override
	public boolean shouldShow() {
		return HA_CMD.playerHasPatherAgreement();
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
				LuddicPathHostileActivityFactor.addAgreementStatus(tooltip, 0f);
			}
		};
	}

	public int getProgress() {
		return 0;
	}
	
	public String getDesc() {
		return "Pather agreement";
	}	

	public float getMagnitudeContribution(StarSystemAPI system) {
		return 0f;
	}

}
