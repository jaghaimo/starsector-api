package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public interface EventFactor {

	public static String NEGATED_FACTOR_PROGRESS = "---";
	
	int getProgress(BaseEventIntel intel);
	String getDesc(BaseEventIntel intel);
	String getProgressStr(BaseEventIntel intel);
	
	Color getDescColor(BaseEventIntel intel);
	Color getProgressColor(BaseEventIntel intel);
	
	TooltipCreator getMainRowTooltip();
	
	boolean shouldShow(BaseEventIntel intel);
	
	boolean isOneTime();
	boolean isExpired();
	
	void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel);
	void notifyEventEnding();
	void notifyEventEnded();
	
	
	void addBulletPointForOneTimeFactor(BaseEventIntel intel, TooltipMakerAPI info, Color tc, float initPad);
}
