package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class HABlowbackFactor extends BaseEventFactor {
	
	public static boolean ENABLED = true;
	
	public static float FRACTION = Global.getSettings().getFloat("blowbackFraction");
	public static float PER_MONTH = Global.getSettings().getFloat("blowbackPerMonth");
	public static float ON_RESET = Global.getSettings().getFloat("blowbackOnReset");
	
	public HABlowbackFactor() {
		
	}

	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		return ENABLED;
//		return true;
//		if (true) return true;
//		int p = Math.round(((HostileActivityEventIntel)intel).getBlowback());
//		return p > 0;
	}
	
	@Override
	public TooltipCreator getMainRowTooltip(final BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				Color h = Misc.getHighlightColor();
				
				tooltip.addPara("Actions that postpone a crisis often have unintended consequences and "
						+ "cause their own problems in the long run. Ultimately, crises can not be avoided, "
						+ "and must instead be dealt with and exploited for the opportunities they provide.", 0f);
				
				int p = Math.round(((HostileActivityEventIntel)intel).getBlowback());
				tooltip.addPara("Will contribute %s of the points per month to event progress, and will "
						+ "also increase the value that progress is reset to after a crisis.", opad, h,
						"" + Math.round(PER_MONTH * 100f) + "%");
				
				tooltip.addPara("Points remaining: %s", opad, h, "" + p);
			}
			
		};
	}

	@Override
	public String getProgressStr(BaseEventIntel intel) {
		if (getProgress(intel) <= 0) return "";
		return super.getProgressStr(intel);
	}
	
	@Override
	public int getProgress(BaseEventIntel intel) {
		if (!ENABLED) return 0;
		
		int p = Math.round(((HostileActivityEventIntel)intel).getBlowback());
		int amt = Math.round(p * PER_MONTH);
		if (amt <= 0 && p > 0) {
			amt = 1;
		}
		return amt;
	}

	@Override
	public String getDesc(BaseEventIntel intel) {
		return "Blowback";
	}
	
	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) > 0) return Misc.getTextColor();
		return Misc.getGrayColor();
	}

}





