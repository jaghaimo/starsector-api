package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseOneTimeFactor extends BaseEventFactor {
	
	public static float SHOW_DURATION_DAYS = 30f;
	
	protected int points;
	protected long timestamp;
	
	public BaseOneTimeFactor(int points) {
		this.points = points;
		timestamp = Global.getSector().getClock().getTimestamp();
	}

	@Override
	public int getProgress(BaseEventIntel intel) {
		return points;
	}

	@Override
	public boolean isOneTime() {
		return true;
	}
	
	protected String getBulletPointText(BaseEventIntel intel) {
		return null;
	}
	
	public void addBulletPointForOneTimeFactor(BaseEventIntel intel, TooltipMakerAPI info, Color tc, float initPad) {
		String text = getBulletPointText(intel);
		if (text == null) text = getDesc(intel);
		if (text != null) {
			info.addPara(text + ": %s", initPad, tc, getProgressColor(intel), 
					getProgressStr(intel));		
		}
	}

	@Override
	public boolean isExpired() {
		return timestamp != 0 && Global.getSector().getClock().getElapsedDaysSince(timestamp) > SHOW_DURATION_DAYS;
	}
	
	public boolean hasOtherFactorsOfClass(BaseEventIntel intel, Class c) {
		for (EventFactor factor : intel.getFactors()) {
			if (factor != this && c.isInstance(factor)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getProgressStr(BaseEventIntel intel) {
		if (getProgress(intel) == 0) {
			return "";
		}
		return super.getProgressStr(intel);
	}

	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) == 0) {
			return Misc.getGrayColor();
		}
		return super.getDescColor(intel);
	}

}
